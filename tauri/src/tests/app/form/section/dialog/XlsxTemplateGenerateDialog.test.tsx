import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { XlsxTemplateGenerateButton } from "../../../../../app/form/section/dialog/XlsxTemplateGenerateDialog";
import type { FetchParams } from "../../../../../utils/fetchUtils";
import {
	environmentFixture,
	workspaceResourcesFixture,
} from "../../../../setup";
import { generateRefreshGenerateTypeXlsxResponseFixture } from "../../fixtures";

// Tauri API モック
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

// JDBC API フックのモック
vi.mock("../../../../../hooks/useJdbc", () => ({
	useJdbcConnectionTest: () => vi.fn(),
	useSaveJdbcProperties: () => vi.fn(),
}));

vi.mock("../../../../../hooks/useWorkspaceResources", () => ({
	useWorkspaceResourcesReload: () => vi.fn().mockResolvedValue(undefined),
	useResolveAbsolutePath: () => vi.fn(async (path: string) => path),
}));

const { mockFetchData, generateResponse } = vi.hoisted(() => {
	const generateResponse = { current: [] as unknown };
	const mockFetchData = vi.fn((params: FetchParams) => {
		if (params.endpoint.includes("generate/refresh")) {
			return Promise.resolve(new Response("{}"));
		}
		if (params.endpoint.includes("template/generate")) {
			if (generateResponse.current instanceof Error) {
				return Promise.reject(generateResponse.current);
			}
			return Promise.resolve(
				new Response(JSON.stringify(generateResponse.current)),
			);
		}
		return Promise.resolve(new Response("{}"));
	});
	return { mockFetchData, generateResponse };
});
vi.mock("../../../../../utils/fetchUtils", async (importOriginal) => {
	const actual =
		await importOriginal<typeof import("../../../../../utils/fetchUtils")>();
	return { ...actual, fetchData: mockFetchData };
});

async function openDialog(setPath: (value: string) => void = vi.fn()) {
	mockFetchData.mockImplementation((params: FetchParams) => {
		if (params.endpoint.includes("generate/refresh")) {
			return Promise.resolve(
				new Response(
					JSON.stringify(generateRefreshGenerateTypeXlsxResponseFixture),
				),
			);
		}
		if (params.endpoint.includes("template/generate")) {
			if (generateResponse.current instanceof Error) {
				return Promise.reject(generateResponse.current);
			}
			return Promise.resolve(
				new Response(JSON.stringify(generateResponse.current)),
			);
		}
		return Promise.resolve(new Response("{}"));
	});
	const user = userEvent.setup();
	render(<XlsxTemplateGenerateButton setPath={setPath} />);
	await user.click(screen.getByRole("button", { name: "generate" }));
	await waitFor(() => {
		expect(
			document.querySelector("#xlsxTemplateGenerateForm"),
		).toBeInTheDocument();
	});
	return user;
}

describe("XlsxTemplateGenerateDialogの描画テスト", () => {
	it("generateボタン押下でfileNameとdataset指定フォームが描画され、generateType=xlsxTemplateでrefreshされる", async () => {
		await openDialog();

		expect(
			document.querySelector('input[type="text"][name="-fileName"]'),
		).toBeRequired();
		expect(
			document.querySelector('select[name="-src.srcType"]'),
		).toBeInTheDocument();
		const refreshCall = mockFetchData.mock.calls.find(([params]) =>
			params.endpoint.includes("generate/refresh"),
		);
		const refreshBody = JSON.parse(String(refreshCall?.[0].options.body));
		expect(refreshBody["-generateType"]).toBe("xlsxTemplate");
	});
});

describe("XlsxTemplateGenerateDialogの実行テスト", () => {
	it("Executeでtemplate/generateへPOSTし、拡張子を補った名前をtemplate欄へ反映する", async () => {
		generateResponse.current = ["generated.xlsx"];
		const setPath = vi.fn();
		const user = await openDialog(setPath);

		await user.type(
			document.querySelector('input[name="-fileName"]') as HTMLElement,
			"generated",
		);
		await user.type(
			document.querySelector('input[name="-src.src"]') as HTMLElement,
			"data/src",
		);
		await user.click(screen.getByRole("button", { name: "Execute" }));

		await waitFor(() => {
			expect(setPath).toHaveBeenCalledWith("generated.xlsx");
		});
		const call = mockFetchData.mock.calls.find(([params]) =>
			params.endpoint.includes("template/generate"),
		);
		const body = JSON.parse(String(call?.[0].options.body));
		expect(body["-fileName"]).toBe("generated");
		expect(body["-src.src"]).toBe("data/src");
		expect(mockSetResourcesSettings).toHaveBeenCalled();
	});

	it("生成に失敗した場合はエラーを表示しtemplate欄は更新しない", async () => {
		generateResponse.current = new Error("generate failed");
		const setPath = vi.fn();
		const user = await openDialog(setPath);

		await user.type(
			document.querySelector('input[name="-fileName"]') as HTMLElement,
			"generated.xlsx",
		);
		await user.type(
			document.querySelector('input[name="-src.src"]') as HTMLElement,
			"data/src",
		);
		await user.click(screen.getByRole("button", { name: "Execute" }));

		await waitFor(() => {
			expect(screen.getByText(/generate failed/)).toBeInTheDocument();
		});
		expect(setPath).not.toHaveBeenCalled();
	});
});
