// 画面を一通り操作し、ブラウザのエラー（pageerror・console.error・エラー応答）と、操作できなかった箇所を集める。
// run.sh smoke から、sidecar と vite（screenshot/vite.config.ts）を起動した状態で呼ぶ。どちらかがあれば終了コード 1。
// 環境変数 SMOKE_ONLY にコマンド名（カンマ区切り）を渡すと、そのコマンドのパラメータだけを操作する。
// ファイルを変える操作（Exec・Save・Delete・Rename・Parameterize）はしない
const { chromium } = require("playwright");
const { button, openApp, openDialogs, topDialog } = require("./common.cjs");

const only = process.env.SMOKE_ONLY?.split(",");
// sample/option の各コマンドのパラメータ
const targets = [
	["convert", "csvToXlsx"],
	["compare", "csvCsv"],
	["generate", "csvToTxt"],
	["run", "runSql"],
	["parameterize", "csvConvert"],
].filter(([command]) => !only || only.includes(command));
// 値で編集ダイアログの種類が変わる選択欄。接頭辞（src・result 等）が同じ欄の ⋮ メニューを、選択肢ごとに開く
const typeSelect = /^(?:(.+)_)?(?:srcType|resultType)$/;

const errors = new Set();
const failures = [];
let step = "start";
let actions = 0;
let dialogsOpened = 0;
let pendingRequests = 0;

// 通信が終わり、その応答で描画し終えるまで待つ（応答後の描画で出るエラーも、次の操作の前に拾う）
async function settle(page) {
	await page.waitForTimeout(50);
	const deadline = Date.now() + 15000;
	while (pendingRequests > 0) {
		if (Date.now() > deadline) {
			throw new Error("requests did not finish");
		}
		await page.waitForTimeout(50);
	}
	await page.evaluate(
		() =>
			new Promise((resolve) =>
				requestAnimationFrame(() => requestAnimationFrame(resolve)),
			),
	);
}

// いちばん手前のダイアログ。無ければ画面全体
const scopeOf = async (page) =>
	(await openDialogs(page).count()) > 0
		? topDialog(page)
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
		await settle(page);
	}
	if ((await openDialogs(page).count()) > depth) {
		throw new Error("dialog did not close with Escape");
	}
}

async function expandAll(page) {
	const scope = await scopeOf(page);
	for (let i = 0; i < 20; i++) {
		const show = scope.locator("button:visible", { hasText: /^Show / });
		if ((await show.count()) === 0) {
			return;
		}
		await show.first().click({ timeout: 2000 });
		await settle(page);
	}
}

// 選択欄の選択肢を順に選び、最後に元の値へ戻す。eachType は種類の選択欄で値を選ぶたびに、欄の接頭辞を付けて呼ぶ
async function exerciseSelects(page, label, eachType) {
	const scope = await scopeOf(page);
	const ids = await scope
		.locator("select:visible")
		.evaluateAll((selects) => selects.map((s) => s.id));
	for (const id of ids.filter((it) => it)) {
		const select = scope.locator(`select[id="${id}"]`);
		if ((await select.count()) === 0) {
			continue;
		}
		const original = await select.inputValue();
		const values = await select
			.locator("option")
			.evaluateAll((options) => options.map((o) => o.value));
		const type = typeSelect.exec(id);
		for (const value of values) {
			await act(`${label} select ${id}=${value}`, async () => {
				await select.selectOption(value, { timeout: 3000 });
				await settle(page);
				await expandAll(page);
			});
			if (eachType && type) {
				await eachType(`${label} ${id}=${value}`, type[1]);
			}
		}
		await act(`${label} select ${id} restore`, async () => {
			await select.selectOption(original, { timeout: 3000 });
			await settle(page);
		});
	}
}

// open でダイアログを開き、開いたことを確かめて（inside なら中も操作して）閉じる。depth は開く前のダイアログの数
async function openAndExercise(page, label, depth, open, inside) {
	await open();
	await settle(page);
	if ((await openDialogs(page).count()) <= depth) {
		throw new Error("dialog did not open");
	}
	dialogsOpened++;
	if (inside) {
		await exerciseDialog(page, label, depth + 1);
	}
	await closeDialogsAbove(page, depth);
}

// ダイアログの中: オプション欄を開き、選択欄を切り替え、設定一覧表の行の編集（入れ子のダイアログ）を開く
async function exerciseDialog(page, label, depth) {
	await act(`${label} expand`, () => expandAll(page));
	await exerciseSelects(page, label);
	// 入れ子は1段だけ
	if (depth > 1) {
		return;
	}
	await act(`${label} row edit`, async () => {
		const edit = button(topDialog(page).locator("table"), "edit").first();
		if ((await edit.count()) === 0) {
			return;
		}
		await openAndExercise(
			page,
			`${label} > row`,
			depth,
			() => edit.click({ timeout: 2000 }),
			true,
		);
	});
}

// 入力欄横の ⋮ メニューから items のダイアログを開く。fieldPrefix を渡すと、id がその接頭辞の欄のメニューだけ
async function exerciseMenus(page, label, items, inside, fieldPrefix) {
	const fields =
		fieldPrefix === undefined
			? page.locator("body")
			: page.locator(`div:has(> label[for^="${fieldPrefix}_"])`);
	const menus = button(fields, "setting");
	const count = await menus.count();
	for (let i = 0; i < count; i++) {
		for (const item of items) {
			await act(`${label} menu#${i} ${item}`, async () => {
				await menus.nth(i).click({ timeout: 2000 });
				await settle(page);
				const target = button(page.locator("body"), item).first();
				if ((await target.count()) === 0) {
					await page.mouse.click(5, 5);
					return;
				}
				await openAndExercise(
					page,
					`${label} menu#${i} ${item}`,
					0,
					() => target.click({ timeout: 2000 }),
					inside,
				);
			});
		}
	}
}

async function exerciseParameter(page, command, name) {
	const label = `${command}/${name}`;
	const heading = command.charAt(0).toUpperCase() + command.slice(1);
	const list = page.locator(`#${command}-list`);
	await act(`${label} open`, async () => {
		if (await list.isHidden()) {
			await page
				.locator(`li:has(> #${command}-list) > button`, { hasText: heading })
				.click();
		}
		await list.getByText(name, { exact: true }).click({ timeout: 5000 });
		await settle(page);
	});
	await act(`${label} expand`, () => expandAll(page));
	// 種類の選択肢ごとに、その種類の編集ダイアログを開く
	await exerciseSelects(page, label, (typeLabel, prefix) =>
		exerciseMenus(page, typeLabel, ["edit"], false, prefix),
	);
	// 元の値で、編集・生成のダイアログの中まで操作する
	await exerciseMenus(page, label, ["edit", "generate"], true);
	await act(`${label} scaffold`, async () => {
		const scaffold = page.getByRole("button", { name: "Scaffold" });
		if ((await scaffold.count()) === 0) {
			return;
		}
		await openAndExercise(
			page,
			`${label} scaffold`,
			0,
			() => scaffold.first().click(),
			true,
		);
	});
	await act(`${label} sidebar menu`, async () => {
		const row = list.locator("li", { hasText: name }).first();
		await row.locator("button").nth(1).click({ timeout: 3000 });
		await button(page.locator("body"), "delete")
			.first()
			.waitFor({ timeout: 2000 });
		await page.mouse.click(700, 5);
		await settle(page);
	});
}

function report(title, list) {
	console.log(`${title}: ${list.length}`);
	for (const item of list) {
		console.log(`  ${item}`);
	}
}

(async () => {
	const browser = await chromium.launch();
	const page = await browser.newPage({
		viewport: { width: 1400, height: 1000 },
	});
	page.on("pageerror", (e) => errors.add(`[${step}] pageerror: ${e.message}`));
	page.on("console", (message) => {
		if (message.type() === "error") {
			errors.add(`[${step}] console.error: ${message.text().split("\n")[0]}`);
		}
	});
	page.on("request", () => pendingRequests++);
	page.on("requestfinished", () => pendingRequests--);
	page.on("requestfailed", () => pendingRequests--);
	// エラー応答は送った内容と一緒に残す（console.error だけでは要求が分からない）
	page.on("response", (response) => {
		if (response.status() >= 400) {
			const request = response.request();
			errors.add(
				`[${step}] ${response.status()} ${request.method()} ${request.url()} ${request.postData() ?? ""}`,
			);
		}
	});
	// confirm・alert は閉じる（削除などは呼ばない前提だが、出ても止まらないように）
	page.on("dialog", (dialog) => dialog.dismiss().catch(() => {}));
	try {
		await act("startup", () => openApp(page));
		for (const [command, name] of targets) {
			await exerciseParameter(page, command, name);
		}
	} finally {
		await browser.close();
	}
	console.log(`actions: ${actions}, dialogs opened: ${dialogsOpened}`);
	report("errors", [...errors]);
	report("failures", failures);
	process.exit(errors.size + failures.length > 0 ? 1 : 0);
})();
