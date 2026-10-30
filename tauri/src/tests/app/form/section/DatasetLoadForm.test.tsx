import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { DatasetLoadForm } from "../../../../app/form/section/DatasetLoadForm";
import type { DatasetSrcInfo } from "../../../../model/CommandOption";
import { environmentFixture, workspaceResourcesFixture } from "../../../setup";
import { convertLoadResponseFixture } from "../fixtures";

vi.mock("@tauri-apps/plugin-dialog", () => ({ open: vi.fn() }));
vi.mock("@tauri-apps/api", () => ({ core: { invoke: vi.fn() } }));
vi.mock("@tauri-apps/api/path", () => ({
	isAbsolute: vi.fn().mockResolvedValue(false),
	sep: vi.fn().mockReturnValue("/"),
}));
vi.mock("../../../../context/WorkspaceResourcesProvider", () => ({
	useWorkspaceContext: () => workspaceResourcesFixture.context,
	useResourcesSettings: () => workspaceResourcesFixture.resources,
}));
vi.mock("../../../../context/EnvironmentProvider", () => ({
	useEnvironment: () => environmentFixture,
}));
const { mockTableNames } = vi.hoisted(() => ({ mockTableNames: vi.fn() }));
vi.mock("../../../../hooks/useDatasetSettings", () => ({
	useDatasetTableNames: (srcInfo: DatasetSrcInfo) => {
		mockTableNames(srcInfo);
		return { tableNames: [], loading: false, logs: [] };
	},
	useDatasetTablePreview: () => ({ preview: null, loading: false, logs: [] }),
	useDatasetTableConvert: () => vi.fn(),
}));

function renderForm() {
	render(
		<DatasetLoadForm
			handleTypeSelect={vi.fn().mockResolvedValue(undefined)}
			name="test"
			srcData={convertLoadResponseFixture.srcData}
		/>,
	);
	return userEvent.setup();
}

describe("DatasetLoadFormのテスト", () => {
	it("Preview Apply Settingsはsrc要素の入力どおりの値でテーブル一覧を取得する", async () => {
		const user = renderForm();
		await user.click(document.getElementById("src_recursive") as Element);
		await user.click(
			document.getElementById("src_settingTargetOnly") as Element,
		);
		await user.type(
			document.getElementById("src_setting") as Element,
			"setting.json",
		);
		await user.click(
			screen.getByRole("button", { name: "Preview Apply Settings" }),
		);

		expect(mockTableNames).toHaveBeenLastCalledWith(
			expect.objectContaining({
				values: expect.objectContaining({
					srcType: "csv",
					recursive: "true",
					setting: "setting.json",
					settingTargetOnly: "true",
				}),
			}),
		);
	});

	// settingが無ければsettingTargetOnlyでは絞られない（sidecar側で判断する）
	it("Preview Before Settingsはsettingを外し、ほかの値はフォームのまま渡す", async () => {
		const user = renderForm();
		await user.click(
			document.getElementById("src_settingTargetOnly") as Element,
		);
		await user.click(
			screen.getByRole("button", { name: "Preview Before Settings" }),
		);

		expect(mockTableNames).toHaveBeenLastCalledWith(
			expect.objectContaining({
				values: expect.objectContaining({
					setting: "",
					settingTargetOnly: "true",
				}),
			}),
		);
	});
});
