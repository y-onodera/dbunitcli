import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import DatasetSettingText from "../../../../../app/form/section/element/DatasetSettingText";
import type { CommandOption } from "../../../../../model/CommandOption";
import type { DefaultPath } from "../../../../../model/WorkspaceResources";
import {
	environmentFixture,
	workspaceResourcesFixture,
} from "../../../../setup";

vi.mock("@tauri-apps/plugin-dialog", () => ({ open: vi.fn() }));
vi.mock("@tauri-apps/api", () => ({ core: { invoke: vi.fn() } }));
vi.mock("../../../../../context/WorkspaceResourcesProvider", () => ({
	useWorkspaceContext: () => workspaceResourcesFixture.context,
	useResourcesSettings: () => workspaceResourcesFixture.resources,
	useSetResourcesSettings: () => vi.fn(),
}));
vi.mock("../../../../../context/EnvironmentProvider", () => ({
	useEnvironment: () => environmentFixture,
}));
vi.mock("../../../../../hooks/useWorkspaceResources", () => ({
	useResolveAbsolutePath: () => vi.fn(async (path: string) => path),
}));

// 受け取ったkind（読込・保存先）を表示するだけの編集ダイアログ
function KindDialog({ kind }: { kind: string }) {
	return <div data-testid="edit-dialog" data-kind={kind} />;
}

function settingElement(defaultPath: DefaultPath, value = ""): CommandOption {
	return {
		name: "setting",
		value,
		attribute: { type: "FILE", required: false, selectOption: [], defaultPath },
		optional: false,
	};
}

function datalistValues(): string[] {
	return Array.from(document.querySelectorAll("datalist option")).map(
		(option) => option.getAttribute("value") ?? "",
	);
}

describe("DatasetSettingTextの保存先の種類", () => {
	it.each([
		{ defaultPath: "SETTING", kind: "datasetSetting", files: ["setting1"] },
		{
			defaultPath: "COMPARE_SETTING",
			kind: "compareSetting",
			files: ["compareKey1"],
		},
		{
			defaultPath: "GENERATE_SETTING",
			kind: "generateSetting",
			files: ["unit1"],
		},
	] as const)(
		"defaultPath=$defaultPathなら$kindのファイルを候補にし、編集ダイアログへ$kindを渡す",
		async ({ defaultPath, kind, files }) => {
			render(
				<DatasetSettingText
					prefix=""
					element={settingElement(defaultPath)}
					editDialog={KindDialog}
					showGenerate={false}
					preview={null}
				/>,
			);
			expect(datalistValues()).toStrictEqual(files);

			await userEvent.click(screen.getByTitle("setting"));
			await userEvent.click(screen.getByTitle("edit"));

			expect(screen.getByTestId("edit-dialog")).toHaveAttribute(
				"data-kind",
				kind,
			);
		},
	);
});
