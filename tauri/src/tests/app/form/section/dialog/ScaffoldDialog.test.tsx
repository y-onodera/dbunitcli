import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { ScaffoldButton } from "../../../../../app/form/section/dialog/ScaffoldDialog";
import type { FetchParams } from "../../../../../utils/fetchUtils";
import {
	environmentFixture,
	workspaceResourcesFixture,
} from "../../../../setup";
import {
	scaffoldExecResponseFixture,
	scaffoldRefreshTargetDdlResponseFixture,
	scaffoldRefreshTargetFixedColumnDefResponseFixture,
} from "../../fixtures";

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

const { mockReloadResources } = vi.hoisted(() => ({
	mockReloadResources: vi.fn().mockResolvedValue(undefined),
}));
vi.mock("../../../../../hooks/useWorkspaceResources", () => ({
	useWorkspaceResourcesReload: () => mockReloadResources,
	useResolveAbsolutePath: () => vi.fn(async (path: string) => path),
}));

const { mockFetchData, refreshResponse, execResponse } = vi.hoisted(() => {
	const refreshResponse = { current: {} as unknown };
	const execResponse = { current: {} as unknown };
	const mockFetchData = vi.fn((params: FetchParams) => {
		if (params.endpoint.includes("scaffold/refresh")) {
			return Promise.resolve(
				new Response(JSON.stringify(refreshResponse.current)),
			);
		}
		if (params.endpoint.includes("scaffold/exec")) {
			return Promise.resolve(
				new Response(JSON.stringify(execResponse.current)),
			);
		}
		return Promise.resolve(new Response("{}"));
	});
	return { mockFetchData, refreshResponse, execResponse };
});
vi.mock("../../../../../utils/fetchUtils", async (importOriginal) => {
	const actual =
		await importOriginal<typeof import("../../../../../utils/fetchUtils")>();
	return { ...actual, fetchData: mockFetchData };
});

async function openDialog(
	handleReflect: (params: Record<string, string>) => Promise<void> = vi
		.fn()
		.mockResolvedValue(undefined),
) {
	const user = userEvent.setup();
	render(
		<ScaffoldButton
			scaffoldPrefill={() => ({ "-target": "ddl" })}
			handleReflect={handleReflect}
		/>,
	);
	await user.click(screen.getByRole("button", { name: "Scaffold" }));
	await waitFor(() => {
		expect(document.querySelector("#scaffoldForm")).toBeInTheDocument();
	});
	return user;
}

describe("ScaffoldDialogの描画テスト", () => {
	it("target=ddlのrefreshレスポンスでダイアログの各フィールドが描画される", async () => {
		refreshResponse.current = scaffoldRefreshTargetDdlResponseFixture;
		await openDialog();

		expect(
			document.querySelector('select[name="-target"]'),
		).toBeInTheDocument();
		expect(
			document.querySelector('input[type="text"][name="-template"]'),
		).toBeRequired();
		expect(
			document.querySelector('input[type="text"][name="-unitSetting"]'),
		).toBeInTheDocument();
		expect(
			document.querySelector('select[name="-datasetType"]'),
		).toBeInTheDocument();
		expect(
			document.querySelector('input[type="text"][name="-datasetEncoding"]'),
		).toBeInTheDocument();
		expect(
			document.querySelector('select[name="-dataset.srcType"]'),
		).toBeInTheDocument();
		expect(
			document.querySelector('input[type="text"][name="-dataset.src"]'),
		).toBeInTheDocument();
	});

	it("target選択肢にparameterは含まれない", async () => {
		refreshResponse.current = scaffoldRefreshTargetDdlResponseFixture;
		await openDialog();

		const options = Array.from(
			document.querySelectorAll('select[name="-target"] option'),
		).map((option) => option.getAttribute("value"));
		expect(options).toEqual([
			"ddl",
			"javaBean",
			"xlsxSchema",
			"fixedColumnDef",
		]);
	});

	it("target=ddlでは固定長固有のオプションは描画されない", async () => {
		refreshResponse.current = scaffoldRefreshTargetDdlResponseFixture;
		await openDialog();

		expect(
			document.querySelector('input[name="-fixedLength"]'),
		).not.toBeInTheDocument();
	});

	it("target=fixedColumnDefのrefreshレスポンスでは固有オプションが描画される", async () => {
		refreshResponse.current =
			scaffoldRefreshTargetFixedColumnDefResponseFixture;
		await openDialog();

		expect(
			document.querySelector('input[type="text"][name="-fixedLength"]'),
		).toBeInTheDocument();
		expect(
			document.querySelector('input[type="text"][name="-defaultLength"]'),
		).toBeInTheDocument();
		expect(
			document.querySelector('input[type="text"][name="-align"]'),
		).toBeInTheDocument();
	});
});

describe("ScaffoldDialogの実行テスト", () => {
	it("Executeで/scaffold/execを呼び、結果を反映してダイアログを閉じる", async () => {
		refreshResponse.current = scaffoldRefreshTargetDdlResponseFixture;
		execResponse.current = scaffoldExecResponseFixture;
		const handleReflect = vi.fn().mockResolvedValue(undefined);
		const user = await openDialog(handleReflect);

		const templateInput = document.querySelector(
			'input[type="text"][name="-template"]',
		) as HTMLInputElement;
		await user.type(templateInput, "scaffoldTpl");
		await user.click(screen.getByRole("button", { name: "Execute" }));

		await waitFor(() => {
			expect(handleReflect).toHaveBeenCalledWith(scaffoldExecResponseFixture);
		});
		const execCall = mockFetchData.mock.calls.find(([params]) =>
			params.endpoint.includes("scaffold/exec"),
		);
		expect(execCall).toBeDefined();
		const requestBody = JSON.parse(String(execCall?.[0].options.body));
		expect(requestBody["-target"]).toBe("ddl");
		expect(requestBody["-template"]).toBe("scaffoldTpl");
		expect(mockReloadResources).toHaveBeenCalled();
		await waitFor(() => {
			expect(document.querySelector("#scaffoldForm")).not.toBeInTheDocument();
		});
	});
});
