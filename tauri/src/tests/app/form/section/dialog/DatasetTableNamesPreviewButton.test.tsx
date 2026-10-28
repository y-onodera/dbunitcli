import { core } from "@tauri-apps/api";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import DatasetTableNamesPreviewButton from "../../../../../app/form/section/dialog/DatasetTableNamesPreviewButton";
import type { TablePreview } from "../../../../../hooks/useDatasetSettings";
import {
	environmentFixture,
	workspaceResourcesFixture,
} from "../../../../setup";
import { convertRefreshSrcTypeXlsxResponseFixture } from "../../fixtures";

// jsdomにはshowModalが未実装のためモック
HTMLDialogElement.prototype.showModal = vi.fn(function (
	this: HTMLDialogElement,
) {
	this.setAttribute("open", "");
});

// Tauri API モック
vi.mock("@tauri-apps/plugin-dialog", () => ({ open: vi.fn() }));
vi.mock("@tauri-apps/api", () => ({ core: { invoke: vi.fn() } }));
vi.mock("@tauri-apps/api/path", () => ({
	isAbsolute: vi.fn().mockResolvedValue(false),
	sep: vi.fn().mockReturnValue("/"),
}));

vi.mock("../../../../../context/WorkspaceResourcesProvider", () => ({
	useWorkspaceContext: () => workspaceResourcesFixture.context,
	useResourcesSettings: () => workspaceResourcesFixture.resources,
}));
vi.mock("../../../../../context/EnvironmentProvider", () => ({
	useEnvironment: () => environmentFixture,
}));
vi.mock("../../../../../hooks/useWorkspaceResources", () => ({
	useWorkspaceResourcesReload: () => vi.fn().mockResolvedValue(undefined),
	useResolveAbsolutePath: () => vi.fn(async (path: string) => path),
}));

// （mockReset:trueで実装が毎回リセットされるため、resolve値は各テスト内で設定する）
const { previewFixture, mockConvert } = vi.hoisted(() => ({
	previewFixture: { current: null as TablePreview | null },
	mockConvert: vi.fn(),
}));
vi.mock("../../../../../hooks/useDatasetSettings", () => ({
	useDatasetTableNames: () => ({
		tableNames: ["big"],
		loading: false,
		logs: [],
	}),
	useDatasetTablePreview: () => ({
		preview: previewFixture.current,
		loading: false,
		logs: [],
	}),
	useDatasetTableConvert: () => mockConvert,
}));

// convert自体はuseDatasetTableConvertのモックで受けるため、fetchDataに届くのはconvert/refreshだけ
const { mockFetchData } = vi.hoisted(() => ({
	mockFetchData: vi.fn(() =>
		Promise.resolve(
			new Response(JSON.stringify(convertRefreshSrcTypeXlsxResponseFixture)),
		),
	),
}));
vi.mock("../../../../../utils/fetchUtils", async (importOriginal) => {
	const actual =
		await importOriginal<typeof import("../../../../../utils/fetchUtils")>();
	return { ...actual, fetchData: mockFetchData };
});

async function openPreview() {
	const user = userEvent.setup();
	render(<DatasetTableNamesPreviewButton title="Preview Before Settings" />);
	await user.click(
		screen.getByRole("button", { name: "Preview Before Settings" }),
	);
	await user.click(screen.getByRole("button", { name: "preview" }));
	return user;
}

async function openExport() {
	previewFixture.current = { headers: ["key"], rows: [["1"]], totalRows: 1 };
	const user = await openPreview();
	await user.click(screen.getByRole("button", { name: "Export" }));
	await waitFor(() => {
		expect(document.querySelector("#tableConvertForm")).toBeInTheDocument();
	});
	return user;
}

describe("DatasetTableNamesPreviewButtonのテスト", () => {
	it("全行数より表示行が少ないときは部分表示であることを示す", async () => {
		previewFixture.current = {
			headers: ["key"],
			rows: [["1"], ["2"]],
			totalRows: 150,
		};
		await openPreview();
		expect(
			screen.getByText("Showing first 2 of 150 rows (partial preview)"),
		).toBeInTheDocument();
	});

	it("全行を表示しているときは行数だけを示す", async () => {
		previewFixture.current = {
			headers: ["key"],
			rows: [["1"], ["2"]],
			totalRows: 2,
		};
		await openPreview();
		expect(screen.getByText("2 rows")).toBeInTheDocument();
		expect(screen.queryByText(/partial preview/)).not.toBeInTheDocument();
	});

	it("ExportボタンでresultフォームのダイアログをConvertの既定値で表示する", async () => {
		await openExport();
		expect(
			document.querySelector('select[name="-result.resultType"]'),
		).toBeInTheDocument();
		expect(document.querySelector('[name^="-src."]')).not.toBeInTheDocument();
	});

	it("Executeで対象テーブルとresultの値でconvertし、完了後に出力先を開ける", async () => {
		mockConvert.mockResolvedValue("/work/result");
		const user = await openExport();
		await user.click(screen.getByRole("button", { name: "Execute" }));

		expect(await screen.findByText("Export completed.")).toBeInTheDocument();
		expect(mockConvert).toHaveBeenCalledTimes(1);
		const [srcInfo, tableName, result] = mockConvert.mock.calls[0];
		expect(srcInfo).toEqual({});
		expect(tableName).toBe("big");
		expect(result).toHaveProperty("-result.resultType");

		await user.click(screen.getByRole("button", { name: "Open Directory" }));
		expect(core.invoke).toHaveBeenCalledWith("open_directory", {
			path: "/work/result",
		});
	});

	it("convertが失敗したらダイアログ内にエラーを表示する", async () => {
		mockConvert.mockRejectedValue(new Error("boom"));
		const user = await openExport();
		await user.click(screen.getByRole("button", { name: "Execute" }));

		expect(await screen.findByText("Error: boom")).toBeInTheDocument();
		expect(screen.queryByText("Export completed.")).not.toBeInTheDocument();
	});
});
