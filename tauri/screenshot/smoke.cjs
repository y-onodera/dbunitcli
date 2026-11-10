// 画面を一通り操作し、ブラウザのエラー（pageerror・console.error）と、操作できなかった箇所を集める。
// run.sh smoke から、sidecar と vite（screenshot/vite.config.ts）を起動した状態で呼ぶ。どちらかがあれば終了コード 1。
// 環境変数 SMOKE_ONLY にコマンド名（カンマ区切り）を渡すと、そのコマンドのパラメータだけを操作する
// ファイルを変える操作（Exec・Save・Delete・Rename・Parameterize）はしない
const { chromium } = require("playwright");

const appUrl = "http://localhost:1430/";
// sample/option の各コマンドのパラメータ
const only = process.env.SMOKE_ONLY?.split(",");
const targets = [
	["convert", "csvToXlsx"],
	["compare", "csvCsv"],
	["generate", "csvToTxt"],
	["run", "runSql"],
	["parameterize", "csvConvert"],
].filter(([command]) => !only || only.includes(command));
// 選択肢ごとに ⋮ メニューの edit も開く選択欄（値で編集ダイアログの種類が変わる）
const typeSelect = /(^|_)(srcType|resultType)$/;

const errors = [];
const failures = [];
let step = "start";
let actions = 0;
let dialogsOpened = 0;

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const openDialogs = (page) => page.locator("dialog[open]");
// いちばん手前のダイアログ。無ければ画面全体
const scopeOf = async (page) =>
	(await openDialogs(page).count()) > 0
		? openDialogs(page).last()
		: page.locator("body");

// 失敗しても残りを続け、失敗した操作を記録する
async function act(name, fn) {
	step = name;
	try {
		await fn();
		actions++;
	} catch (e) {
		failures.push(`[${name}] ${String(e.message).split("\n")[0]}`);
	}
}

async function closeDialogsAbove(page, depth) {
	for (let i = 0; i < 6 && (await openDialogs(page).count()) > depth; i++) {
		await page.keyboard.press("Escape");
		await sleep(300);
	}
	if ((await openDialogs(page).count()) > depth) {
		throw new Error("dialog did not close with Escape");
	}
}

async function expandAll(scope) {
	for (let i = 0; i < 20; i++) {
		const show = scope.locator("button:visible", { hasText: /^Show / });
		if ((await show.count()) === 0) {
			return;
		}
		await show.first().click({ timeout: 2000 });
		await sleep(150);
	}
}

// 選択欄の選択肢を順に選び、最後に元の値へ戻す。eachValue は選ぶたびに呼ぶ
async function exerciseSelects(page, label, eachValue) {
	const scope = await scopeOf(page);
	const count = await scope.locator("select:visible").count();
	for (let i = 0; i < count; i++) {
		const select = () => scope.locator("select:visible").nth(i);
		if ((await select().count()) === 0) {
			break;
		}
		const id = (await select().getAttribute("id")) ?? `#${i}`;
		const original = await select().inputValue();
		const values = await select()
			.locator("option")
			.evaluateAll((options) => options.map((o) => o.value));
		for (const value of values) {
			await act(`${label} select ${id}=${value}`, async () => {
				await select().selectOption(value, { timeout: 3000 });
				await sleep(700);
				await expandAll(scope);
			});
			if (eachValue && typeSelect.test(id)) {
				await eachValue(`${label} ${id}=${value}`);
			}
		}
		await act(`${label} select ${id} restore`, async () => {
			await select().selectOption(original, { timeout: 3000 });
			await sleep(700);
		});
	}
}

// ダイアログの中: オプション欄を開き、選択欄を切り替え、設定一覧表の行の編集（入れ子のダイアログ）を開く
async function exerciseDialog(page, label, depth) {
	await act(`${label} expand`, async () => expandAll(await scopeOf(page)));
	await exerciseSelects(page, label);
	if (depth > 1) {
		return;
	}
	await act(`${label} row edit`, async () => {
		const edit = (await scopeOf(page))
			.locator('table button[aria-label="edit"]:visible')
			.first();
		if ((await edit.count()) === 0) {
			return;
		}
		await edit.click({ timeout: 2000 });
		await sleep(800);
		if ((await openDialogs(page).count()) <= depth) {
			throw new Error("nested dialog did not open");
		}
		dialogsOpened++;
		await exerciseDialog(page, `${label} > row`, depth + 1);
		await closeDialogsAbove(page, depth);
	});
}

// 入力欄横の ⋮ メニューから items のダイアログを開いて操作し、閉じる
async function exerciseMenus(page, label, items, inDialog) {
	const count = await page.locator('button[aria-label="setting"]:visible').count();
	for (let i = 0; i < count; i++) {
		for (const item of items) {
			await act(`${label} menu#${i} ${item}`, async () => {
				await page
					.locator('button[aria-label="setting"]:visible')
					.nth(i)
					.click({ timeout: 2000 });
				await sleep(200);
				const button = page
					.locator(`button[aria-label="${item}"]:visible`)
					.first();
				if ((await button.count()) === 0) {
					await page.mouse.click(5, 5);
					return;
				}
				await button.click({ timeout: 2000 });
				await sleep(1500);
				if ((await openDialogs(page).count()) === 0) {
					throw new Error("dialog did not open");
				}
				dialogsOpened++;
				if (inDialog) {
					await exerciseDialog(page, `${label} menu#${i} ${item}`, 1);
				}
				await closeDialogsAbove(page, 0);
			});
		}
	}
}

async function exerciseParameter(page, command, name) {
	const label = `${command}/${name}`;
	const heading = command.charAt(0).toUpperCase() + command.slice(1);
	await act(`${label} open`, async () => {
		const list = page.locator(`#${command}-list`);
		if (await list.isHidden()) {
			await page
				.locator(`li:has(> #${command}-list) > button`, { hasText: heading })
				.click();
		}
		await list.getByText(name, { exact: true }).click({ timeout: 5000 });
		await sleep(1500);
	});
	await act(`${label} expand`, () => expandAll(page.locator("body")));
	// 種類の選択肢ごとに、その種類の編集ダイアログを開く
	await exerciseSelects(page, label, (typeLabel) =>
		exerciseMenus(page, typeLabel, ["edit"], false),
	);
	// 元の値で、編集・生成のダイアログの中まで操作する
	await exerciseMenus(page, label, ["edit", "generate"], true);
	await act(`${label} scaffold`, async () => {
		const scaffold = page.getByRole("button", { name: "Scaffold" });
		if ((await scaffold.count()) === 0) {
			return;
		}
		await scaffold.first().click();
		await sleep(1500);
		dialogsOpened++;
		await exerciseDialog(page, `${label} scaffold`, 1);
		await closeDialogsAbove(page, 0);
	});
	await act(`${label} sidebar menu`, async () => {
		const row = page.locator(`#${command}-list li`, { hasText: name }).first();
		await row.locator("button").nth(1).click({ timeout: 3000 });
		await page
			.locator('button[aria-label="delete"]:visible')
			.first()
			.waitFor({ timeout: 2000 });
		await page.mouse.click(700, 5);
		await sleep(300);
	});
}

(async () => {
	const browser = await chromium.launch();
	const page = await browser.newPage({
		viewport: { width: 1400, height: 1000 },
	});
	page.on("pageerror", (e) => errors.push(`[${step}] pageerror: ${e.message}`));
	page.on("console", (message) => {
		if (message.type() === "error") {
			errors.push(`[${step}] console.error: ${message.text().split("\n")[0]}`);
		}
	});
	// confirm・alert は閉じる（削除などは呼ばない前提だが、出ても止まらないように）
	page.on("dialog", (dialog) => dialog.dismiss().catch(() => {}));
	// エラー応答は送った内容と一緒に残す（console.error だけでは要求が分からない）
	page.on("response", (response) => {
		if (response.status() >= 400) {
			const request = response.request();
			errors.push(
				`[${step}] ${response.status()} ${request.method()} ${request.url()} ${request.postData() ?? ""}`,
			);
		}
	});
	try {
		await act("startup", async () => {
			await page.goto(appUrl);
			await page.getByText("confirm").click();
			await sleep(1000);
		});
		for (const [command, name] of targets) {
			await exerciseParameter(page, command, name);
		}
	} finally {
		await browser.close();
	}
	console.log(`actions: ${actions}, dialogs opened: ${dialogsOpened}`);
	const uniqueErrors = [...new Set(errors)];
	console.log(`errors: ${uniqueErrors.length}`);
	for (const error of uniqueErrors) {
		console.log(`  ${error}`);
	}
	console.log(`failures: ${failures.length}`);
	for (const failure of failures) {
		console.log(`  ${failure}`);
	}
	process.exit(uniqueErrors.length + failures.length > 0 ? 1 : 0);
})();
