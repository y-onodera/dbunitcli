// ヘルプの画面イメージを撮る。run.sh から、sidecar と vite（screenshot/vite.config.ts）を起動した状態で呼ぶ
// 引数: 出力先ディレクトリ（public/help/images）
const { mkdirSync } = require("node:fs");
const { join } = require("node:path");
const { chromium } = require("playwright");

const outDir = process.argv[2];
const appUrl = "http://localhost:1430/";
const margin = 8;

async function boxesOf(file, locators) {
	const boxes = [];
	for (const locator of locators) {
		const elements = await locator.all();
		if (elements.length === 0) {
			throw new Error(`${file}: 要素が表示されていません`);
		}
		for (const element of elements) {
			boxes.push(await element.boundingBox());
		}
	}
	return boxes;
}

// 複数の要素をまとめて囲む範囲を、余白付きで撮る。within を渡すと、その要素（ダイアログなど）の外は撮らない
async function shot(page, file, locators, within) {
	const boxes = await boxesOf(file, locators);
	const [frame] = within
		? await boxesOf(file, [within])
		: [{ x: 0, y: 0, ...page.viewportSize() }];
	const x = Math.max(frame.x, Math.min(...boxes.map((b) => b.x)) - margin);
	const y = Math.max(frame.y, Math.min(...boxes.map((b) => b.y)) - margin);
	const right = Math.min(
		frame.x + frame.width,
		Math.max(...boxes.map((b) => b.x + b.width)) + margin,
	);
	const bottom = Math.min(
		frame.y + frame.height,
		Math.max(...boxes.map((b) => b.y + b.height)) + margin,
	);
	const path = join(outDir, file);
	mkdirSync(join(path, ".."), { recursive: true });
	await page.screenshot({
		path,
		clip: { x, y, width: right - x, height: bottom - y },
	});
	console.log(`saved ${file}`);
}

// ラベル・入力欄・下の表示・横のボタンをまとめた 1 項目分
const field = (page, id) => page.locator(`div:has(> label[for="${id}"])`);

const button = (scope, ...labels) =>
	scope.locator(
		labels.map((label) => `button[aria-label="${label}"]:visible`).join(","),
	);

// 入力欄横の ⋮ メニューに並ぶボタン
const menuButtons = (page) =>
	button(page, "edit", "generate", "open", "delete", "file", "dir");

// いちばん手前に開いているダイアログ
const topDialog = (page) => page.locator("dialog[open]").last();

async function openConvertForm(page) {
	await page.goto(appUrl);
	await page.getByText("confirm").click();
	await page.getByText("Convert", { exact: true }).click();
	await page.getByText("csvToXlsx").click();
	await page.locator("#src_setting").waitFor();
}

async function fill(scope, selector, value) {
	const input = scope.locator(selector);
	await input.fill(value);
	await input.blur();
}

async function captureInput(page) {
	await openConvertForm(page);
	await fill(page, "#src_setting", "test.json");
	await field(page, "src_setting").evaluate((e) =>
		e.scrollIntoView({ block: "center" }),
	);
	await shot(page, "common/input-field.png", [field(page, "src_setting")]);

	await button(field(page, "src_setting"), "setting").click();
	await menuButtons(page).first().waitFor();
	await shot(page, "common/menu.png", [
		field(page, "src_setting"),
		menuButtons(page),
	]);
	await page.mouse.click(5, 5);

	await fill(page, "#src_src", "notfound");
	await page.getByRole("button", { name: "Exec", exact: true }).click();
	await page.getByRole("button", { name: "Close" }).click();
	await field(page, "src_src").getByText("Invalid value").waitFor();
	await shot(page, "common/input-error.png", [field(page, "src_src")]);
}

async function captureDialog(page) {
	await openConvertForm(page);
	await fill(page, "#src_setting", "test.json");
	await button(field(page, "src_setting"), "setting").click();
	await button(page, "edit").click();
	const fileName = page.locator("#fileName");
	await fileName.waitFor();
	const settings = topDialog(page);

	const table = settings.locator("table").first();
	const add = button(settings, "add").first();
	await shot(page, "common/setting-table.png", [table, add], settings);

	await settings.evaluate((e) => {
		e.scrollTop = e.scrollHeight;
	});
	await shot(
		page,
		"common/dialog-footer.png",
		[
			settings.locator("div.grid-cols-5:has(#fileName)"),
			settings.getByRole("button", { name: "Close" }).locator(".."),
		],
		settings,
	);

	// 設定一覧表の行を編集するダイアログ（Dataset Setting）の name 欄に、2 つめの値を入れる
	await button(table, "edit").first().click();
	await page.locator("dialog[open]").nth(1).waitFor();
	const row = topDialog(page);
	const target = row.locator("fieldset", {
		has: page.locator("legend", { hasText: /^Target$/ }),
	});
	await button(target, "add").click();
	await fill(target, 'input[name="name"] >> nth=1', "TABLE_B");
	await button(target, "add").waitFor();
	await shot(
		page,
		"common/arrays.png",
		[
			target.locator('label[for="name"]'),
			target.locator('input[name="name"]'),
			button(target, "remove", "add"),
		],
		row,
	);

	// filter の式の編集ダイアログ（最大化できる）
	await row
		.locator("div.grid-cols-5:has(#filter)")
		.locator('button[aria-label="edit"]')
		.click();
	const maximize = page.locator('button:has(title:text-is("Maximize"))');
	await maximize.waitFor();
	// 式の編集ダイアログは <dialog> ではないので、最大化ボタンを含む枠を撮る
	const expression = maximize.locator("xpath=ancestor::*[.//h2][1]");
	await shot(page, "common/maximize.png", [expression]);
}

(async () => {
	const browser = await chromium.launch();
	const page = await browser.newPage({
		// ヘルプの本文の幅（ヘルプウィンドウ 900px）に収まり、縮小せずに表示される幅で撮る
		viewport: { width: 800, height: 900 },
		deviceScaleFactor: 2,
	});
	page.on("pageerror", (e) => console.error("pageerror:", e.message));
	try {
		await captureInput(page);
		await captureDialog(page);
	} catch (e) {
		await page.screenshot({ path: join(outDir, "failed.png") });
		throw e;
	} finally {
		await browser.close();
	}
})().catch((e) => {
	console.error(e);
	process.exit(1);
});
