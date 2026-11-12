import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { XlsxCompareTemplateGenerateButton } from "../../../../../app/form/section/dialog/XlsxCompareTemplateGenerateDialog";
import type { FetchParams } from "../../../../../utils/fetchUtils";
import {
	environmentFixture,
	workspaceResourcesFixture,
} from "../../../../setup";
import { compareLoadResponseFixture } from "../../fixtures";

vi.mock("@tauri-apps/plugin-dialog", () => ({ open: vi.fn() }));
vi.mock("@tauri-apps/api", () => ({ core: { invoke: vi.fn() } }));
vi.mock("@tauri-apps/api/path", () => ({
	isAbsolute: vi.fn().mockResolvedValue(false),
	sep: vi.fn().mockReturnValue("/"),
}));

const { mockSetResourcesSettings } = vi.hoisted(() => ({
	mockSetResourcesSettings: vi.fn(),
}));
vi.mock("../../../../../context/WorkspaceResourcesProvider", () => ({
	useWorkspaceContext: () => workspaceResourcesFixture.context,
	useResourcesSettings: () => workspaceResourcesFixture.resources,
	useSetResourcesSettings: () => mockSetResourcesSettings,
}));
vi.mock("../../../../../context/EnvironmentProvider", () => ({
	useEnvironment: () => environmentFixture,
}));
vi.mock("../../../../../hooks/useJdbc", () => ({
	useJdbcConnectionTest: () => vi.fn(),
	useSaveJdbcProperties: () => vi.fn(),
}));
vi.mock("../../../../../hooks/useWorkspaceResources", () => ({
	useWorkspaceResourcesReload: () => vi.fn().mockResolvedValue(undefined),
	useResolveAbsolutePath: () => vi.fn(async (path: string) => path),
}));

const COLUMNS: Record<string, string[]> = {
	OLD_TABLE: ["ID", "SUB", "NAME"],
	NEW_TABLE: ["CODE", "SUB", "NAME2"],
};
vi.mock("../../../../../hooks/useDatasetSettings", () => ({
	useDatasetTableNames: () => ({
		tableNames: ["OLD_TABLE", "NEW_TABLE"],
		loading: false,
		logs: [],
		error: "",
	}),
	useColumnNamesFetcher:
		() =>
		async (_srcInfo: unknown, tableName: string): Promise<string[]> =>
			COLUMNS[tableName] ?? [],
}));

const { mockFetchData } = vi.hoisted(() => ({ mockFetchData: vi.fn() }));
vi.mock("../../../../../utils/fetchUtils", async (importOriginal) => {
	const actual =
		await importOriginal<typeof import("../../../../../utils/fetchUtils")>();
	return { ...actual, fetchData: mockFetchData };
});

async function openDialog(setPath: (value: string) => void = vi.fn()) {
	mockFetchData.mockImplementation((params: FetchParams) => {
		if (params.endpoint.includes("compare/refresh")) {
			return Promise.resolve(
				new Response(JSON.stringify(compareLoadResponseFixture)),
			);
		}
		if (params.endpoint.includes("template/generate-compare")) {
			return Promise.resolve(new Response(JSON.stringify(["cmp.xlsx"])));
		}
		return Promise.resolve(new Response("{}"));
	});
	const user = userEvent.setup();
	render(<XlsxCompareTemplateGenerateButton setPath={setPath} />);
	await user.click(
		screen.getByRole("button", { name: "generate compare template" }),
	);
	await waitFor(() => {
		expect(
			document.querySelector("#xlsxCompareTemplateGenerateForm"),
		).toBeInTheDocument();
	});
	return user;
}

const byId = (id: string) => document.getElementById(id) as HTMLElement;
const inputByName = (name: string) =>
	document.querySelector(`input[name="${name}"]`) as HTMLInputElement;

async function loadColumns(
	user: ReturnType<typeof userEvent.setup>,
	prefix: string,
	tableName: string,
	buttonIndex: number,
) {
	await user.selectOptions(byId(`${prefix}TableName`), tableName);
	await user.click(
		screen.getAllByRole("button", { name: "Load Columns" })[buttonIndex],
	);
	await waitFor(() => {
		expect(
			screen.getByText(new RegExp(`${COLUMNS[tableName].join(", ")}`)),
		).toBeInTheDocument();
	});
}

function postedBody() {
	const call = mockFetchData.mock.calls.find(([params]) =>
		params.endpoint.includes("template/generate-compare"),
	);
	return JSON.parse(String(call?.[0].options.body));
}

describe("XlsxCompareTemplateGenerateDialogの描画テスト", () => {
	it("compare/refreshで2つのデータセット欄を描画する", async () => {
		await openDialog();
		expect(inputByName("-fileName")).toBeRequired();
		expect(inputByName("compare sheetName").value).toBe("compare");
		expect(
			document.querySelector('select[name="-old.srcType"]'),
		).toBeInTheDocument();
		expect(
			document.querySelector('select[name="-new.srcType"]'),
		).toBeInTheDocument();
	});

	it("Use second datasetを外すとnewのデータセット欄の代わりにシート名だけを入力させる", async () => {
		const user = await openDialog();
		await user.click(byId("useSecondDataset"));
		expect(
			document.querySelector('select[name="-new.srcType"]'),
		).not.toBeInTheDocument();
		expect(inputByName("new sheetName")).toBeInTheDocument();
	});
});

describe("XlsxCompareTemplateGenerateDialogの実行テスト", () => {
	it("2データセット: 選んだキー・比較ペアでtemplate/generate-compareへPOSTし、template欄へ反映する", async () => {
		const setPath = vi.fn();
		const user = await openDialog(setPath);
		await user.type(inputByName("-fileName"), "cmp");
		await loadColumns(user, "old", "OLD_TABLE", 0);
		await loadColumns(user, "new", "NEW_TABLE", 1);
		expect(inputByName("old sheetName").value).toBe("OLD_TABLE");
		await user.clear(inputByName("new sheetName"));
		await user.type(inputByName("new sheetName"), "after");
		await user.selectOptions(byId("keysFirst0"), "ID");
		await user.selectOptions(byId("keysSecond0"), "CODE");
		await user.click(screen.getByRole("button", { name: "add keys" }));
		await user.selectOptions(byId("keysFirst1"), "SUB");
		await user.selectOptions(byId("keysSecond1"), "SUB");
		await user.selectOptions(byId("comparesFirst0"), "NAME");
		await user.selectOptions(byId("comparesSecond0"), "NAME2");
		await user.click(screen.getByRole("button", { name: "Execute" }));

		await waitFor(() => {
			expect(setPath).toHaveBeenCalledWith("cmp.xlsx");
		});
		expect(postedBody()).toEqual({
			fileName: "cmp",
			compareSheetName: "compare",
			first: { sheetName: "OLD_TABLE", columns: ["ID", "SUB", "NAME"] },
			second: { sheetName: "after", columns: ["CODE", "SUB", "NAME2"] },
			keys: [
				{ first: "ID", second: "CODE" },
				{ first: "SUB", second: "SUB" },
			],
			compares: [{ first: "NAME", second: "NAME2" }],
		});
		expect(mockSetResourcesSettings).toHaveBeenCalled();
	});

	it("1データセット: キー以外の列を同名で比較し、2枚目は同じ列で入力したシート名にする", async () => {
		const setPath = vi.fn();
		const user = await openDialog(setPath);
		await user.click(byId("useSecondDataset"));
		await user.type(inputByName("-fileName"), "cmp.xlsx");
		await loadColumns(user, "old", "OLD_TABLE", 0);
		await user.type(inputByName("new sheetName"), "after");
		await user.click(screen.getByRole("cell", { name: "ID" }));
		await user.click(screen.getByRole("button", { name: "Execute" }));

		await waitFor(() => {
			expect(setPath).toHaveBeenCalledWith("cmp.xlsx");
		});
		expect(postedBody()).toEqual({
			fileName: "cmp.xlsx",
			compareSheetName: "compare",
			first: { sheetName: "OLD_TABLE", columns: ["ID", "SUB", "NAME"] },
			second: { sheetName: "after", columns: ["ID", "SUB", "NAME"] },
			keys: [{ first: "ID", second: "ID" }],
			compares: [
				{ first: "SUB", second: "SUB" },
				{ first: "NAME", second: "NAME" },
			],
		});
	});

	it("列を読み込まずに実行するとエラーを表示し、POSTしない", async () => {
		const setPath = vi.fn();
		const user = await openDialog(setPath);
		await user.type(inputByName("-fileName"), "cmp");
		await user.selectOptions(byId("oldTableName"), "OLD_TABLE");
		await user.selectOptions(byId("newTableName"), "NEW_TABLE");
		await user.click(screen.getByRole("button", { name: "Execute" }));

		await waitFor(() => {
			expect(
				screen.getByText(/Load the columns of the old dataset/),
			).toBeInTheDocument();
		});
		expect(setPath).not.toHaveBeenCalled();
		expect(
			mockFetchData.mock.calls.some(([params]) =>
				params.endpoint.includes("template/generate-compare"),
			),
		).toBe(false);
	});
});
