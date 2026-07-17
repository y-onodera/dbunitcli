import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { XlsxSchemaGenerateButton } from "../../../../../app/form/section/dialog/XlsxSchemaGenerateDialog";
import type { FetchParams } from "../../../../../utils/fetchUtils";
import {
	environmentFixture,
	workspaceResourcesFixture,
} from "../../../../setup";
import { generateRefreshXlsxSchemaResponseFixture } from "../../fixtures";

// jsdomにはshowModal/closeが未実装のためモック
HTMLDialogElement.prototype.showModal = vi.fn(function (
	this: HTMLDialogElement,
) {
	this.setAttribute("open", "");
});
HTMLDialogElement.prototype.close = vi.fn(function (this: HTMLDialogElement) {
	this.removeAttribute("open");
});

// Tauri API モック
vi.mock("@tauri-apps/plugin-dialog", () => ({ open: vi.fn() }));
vi.mock("@tauri-apps/api", () => ({ core: { invoke: vi.fn() } }));
vi.mock("@tauri-apps/api/path", () => ({
	isAbsolute: vi.fn().mockResolvedValue(false),
	sep: vi.fn().mockReturnValue("/"),
}));

// コンテキストフックのモック
vi.mock("../../../../../context/WorkspaceResourcesProvider", () => ({
	useWorkspaceContext: () => workspaceResourcesFixture.context,
	useResourcesSettings: () => workspaceResourcesFixture.resources,
}));
vi.mock("../../../../../context/EnvironmentProvider", () => ({
	useEnvironment: () => environmentFixture,
}));

// JDBC API フックのモック
vi.mock("../../../../../hooks/useJdbc", () => ({
	useJdbcConnectionTest: () => vi.fn(),
	useJdbcSaveProperties: () => vi.fn(),
	useDeleteJdbcProperties: () => vi.fn(),
}));

vi.mock("../../../../../hooks/useWorkspaceResources", () => ({
	useWorkspaceResourcesReload: () => vi.fn().mockResolvedValue(undefined),
	useResolveAbsolutePath: () => vi.fn(async (path: string) => path),
}));

// 保存フローは編集ダイアログ内の既存フックが担うためモックで検証する
// （mockReset:trueで実装が毎回リセットされるため、resolve値は各テスト内で設定する）
const { mockSaveSchema } = vi.hoisted(() => ({
	mockSaveSchema: vi.fn(),
}));
vi.mock("../../../../../hooks/useXlsxSchema", () => ({
	useSaveXlsxSchema: () => mockSaveSchema,
	useDeleteXlsxSchema: () => vi.fn(),
	useXlsxSchemaData: () => ({ schema: null, loading: true }),
	useSrcInfoSheets: () => [],
}));

const { mockFetchData, refreshResponse, generateResponse } = vi.hoisted(() => {
	const refreshResponse = { current: {} as unknown };
	const generateResponse = { current: {} as unknown };
	const mockFetchData = vi.fn((params: FetchParams) => {
		if (params.endpoint.includes("generate/refresh")) {
			return Promise.resolve(
				new Response(JSON.stringify(refreshResponse.current)),
			);
		}
		if (params.endpoint.includes("xlsx-schema/generate")) {
			if (generateResponse.current instanceof Error) {
				return Promise.reject(generateResponse.current);
			}
			return Promise.resolve(
				new Response(JSON.stringify(generateResponse.current)),
			);
		}
		return Promise.resolve(new Response("{}"));
	});
	return { mockFetchData, refreshResponse, generateResponse };
});
vi.mock("../../../../../utils/fetchUtils", async (importOriginal) => {
	const actual =
		await importOriginal<typeof import("../../../../../utils/fetchUtils")>();
	return { ...actual, fetchData: mockFetchData };
});

const generatedSchemaFixture = {
	rows: [
		{
			sheetName: "multi1",
			tableName: "multi1",
			header: ["key", "column1"],
			dataStart: 1,
			columnIndex: [0, 1],
			breakKey: ["key"],
			addFileInfo: false,
		},
	],
	cells: [
		{
			sheetName: "multi1",
			tableName: "multi1",
			header: ["key", "column1"],
			rows: [{ cellAddress: ["A2", "B2"] }],
			addFileInfo: false,
		},
	],
};

async function openDialog(setPath: (value: string) => void = vi.fn()) {
	const user = userEvent.setup();
	render(<XlsxSchemaGenerateButton path="" setPath={setPath} />);
	await user.click(screen.getByRole("button", { name: "generate" }));
	await waitFor(() => {
		expect(
			document.querySelector("#xlsxSchemaGenerateForm"),
		).toBeInTheDocument();
	});
	return user;
}

describe("XlsxSchemaGenerateDialogの描画テスト", () => {
	it("generateボタン押下でrefreshレスポンスのdataset指定フォームが描画される", async () => {
		refreshResponse.current = generateRefreshXlsxSchemaResponseFixture;
		await openDialog();

		expect(
			document.querySelector('select[name="-src.srcType"]'),
		).toBeInTheDocument();
		expect(
			document.querySelector('input[type="text"][name="-src.src"]'),
		).toBeRequired();
		const refreshCall = mockFetchData.mock.calls.find(([params]) =>
			params.endpoint.includes("generate/refresh"),
		);
		const refreshBody = JSON.parse(String(refreshCall?.[0].options.body));
		expect(refreshBody["-generateType"]).toBe("xlsxSchema");
	});
});

describe("XlsxSchemaGenerateDialogの実行テスト", () => {
	it("Executeで生成した内容が編集ダイアログに表示され、保存でテキストボックスへ反映される", async () => {
		refreshResponse.current = generateRefreshXlsxSchemaResponseFixture;
		generateResponse.current = generatedSchemaFixture;
		mockSaveSchema.mockResolvedValue("success");
		const setPath = vi.fn();
		const user = await openDialog(setPath);

		const srcInput = document.querySelector(
			'input[type="text"][name="-src.src"]',
		) as HTMLInputElement;
		await user.type(srcInput, "resources/src/csv");
		await user.click(screen.getByRole("button", { name: "Execute" }));

		// 生成結果が既存の編集ダイアログにプリフィルされる
		await waitFor(() => {
			expect(screen.getAllByText("multi1→multi1").length).toBeGreaterThan(0);
		});
		const generateCall = mockFetchData.mock.calls.find(([params]) =>
			params.endpoint.includes("xlsx-schema/generate"),
		);
		expect(generateCall).toBeDefined();
		const requestBody = JSON.parse(String(generateCall?.[0].options.body));
		expect(requestBody["-generateType"]).toBe("xlsxSchema");
		expect(requestBody["-src.src"]).toBe("resources/src/csv");
		expect(requestBody["-src.srcType"]).toBe("csv");

		// 名前を付けて保存するとsetPathで反映されダイアログが閉じる
		const fileNameInput = document.querySelector(
			'input[name="fileName"]',
		) as HTMLInputElement;
		await user.type(fileNameInput, "generated.json");
		await user.click(screen.getByRole("button", { name: "Save" }));

		await waitFor(() => {
			expect(mockSaveSchema).toHaveBeenCalledWith(
				"generated.json",
				expect.anything(),
			);
		});
		await waitFor(() => {
			expect(setPath).toHaveBeenCalledWith("generated.json");
		});
		await waitFor(() => {
			expect(screen.queryByText("multi1→multi1")).not.toBeInTheDocument();
		});
	});

	it("生成失敗時はエラーメッセージを表示してダイアログを維持する", async () => {
		refreshResponse.current = generateRefreshXlsxSchemaResponseFixture;
		generateResponse.current = new Error("generate failed");
		const user = await openDialog();

		const srcInput = document.querySelector(
			'input[type="text"][name="-src.src"]',
		) as HTMLInputElement;
		await user.type(srcInput, "resources/src/csv");
		await user.click(screen.getByRole("button", { name: "Execute" }));

		await waitFor(() => {
			expect(screen.getByText(/generate failed/)).toBeInTheDocument();
		});
		expect(
			document.querySelector("#xlsxSchemaGenerateForm"),
		).toBeInTheDocument();
	});
});
