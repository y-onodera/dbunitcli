// 撮影（capture.cjs）とスモークテスト（smoke.cjs）で共通の、画面の部品の探し方と起動手順
const appUrl = "http://localhost:1430/";

const button = (scope, ...labels) =>
	scope.locator(
		labels.map((label) => `button[aria-label="${label}"]:visible`).join(","),
	);

const openDialogs = (page) => page.locator("dialog[open]");

// いちばん手前に開いているダイアログ
const topDialog = (page) => openDialogs(page).last();

// 起動して Workspace の確認を閉じ、サイドバーが出るまで待つ
async function openApp(page) {
	await page.goto(appUrl);
	await page.getByText("confirm").click();
	await page.locator("#convert-list").waitFor({ state: "attached" });
}

module.exports = { button, openApp, openDialogs, topDialog };
