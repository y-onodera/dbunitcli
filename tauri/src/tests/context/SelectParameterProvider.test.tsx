import { act, renderHook, waitFor } from "@testing-library/react";
import type { ReactNode } from "react";
import { describe, expect, it, vi } from "vitest";
import {
	type Environment,
	environmentContext,
} from "../../context/EnvironmentProvider";
import SelectParameterProvider, {
	useSelectParameter,
	useSetSelectParameter,
} from "../../context/SelectParameterProvider";
import WorkspaceResourcesProvider, {
	useParameterList,
} from "../../context/WorkspaceResourcesProvider";
import {
	useAddParameter,
	useExecParameter,
	useLoadSelectParameter,
	useParameterActions,
	useRefreshSelectParameter,
	useSaveParameter,
} from "../../hooks/useSelectParameter";
import type { DatasetSource } from "../../model/CommandOption";
import type {
	Command,
	ConvertOptions,
	GenerateOptions,
} from "../../model/SelectParameter";
import { SelectParameter } from "../../model/SelectParameter";
import type { FetchParams } from "../../utils/fetchUtils";
import {
	environmentFixture,
	makeMinimalParam,
	workspaceResourcesFixture,
} from "../setup";

// モックデータ
const createDatasetSource = (prefix: string) =>
	({
		prefix,
	}) as DatasetSource;
const mockConvertParams = {
	srcData: createDatasetSource(""),
	convertResult: {
		prefix: "",
		elements: [
			"resultType",
			"result",
			"resultPath",
			"exportEmptyTable",
			"exportHeader",
		].map(makeMinimalParam),
		jdbc: undefined,
	},
} as unknown as ConvertOptions;
const mockRefreshConvertParams = {
	srcData: { ...mockConvertParams.srcData, name: "refresh-test-param" },
	convertResult: {
		...mockConvertParams.convertResult,
		name: "refresh-test-param",
	},
} as unknown as ConvertOptions;
const mockGenerateParams = {
	elements: [
		"generateType",
		"unit",
		"template",
		"result",
		"resultPath",
		"outputEncoding",
	].map(makeMinimalParam),
	srcData: createDatasetSource(""),
	templateOption: {
		prefix: "",
		elements: [
			"encoding",
			"templateGroup",
			"templateParameterAttribute",
			"templateVarStart",
			"templateVarStop",
		].map(makeMinimalParam),
	},
} as unknown as GenerateOptions;
const mockRefreshGenerateParams = {
	...mockGenerateParams,
	srcData: { ...mockGenerateParams.srcData, name: "refresh-test-param" },
	templateOption: {
		...mockGenerateParams.templateOption,
		name: "refresh-test-param",
	},
} as unknown as GenerateOptions;
const { mockFetchData } = vi.hoisted(() => {
	return {
		mockFetchData: vi.fn((params: FetchParams) => {
			if (params.endpoint.includes("/generate/load")) {
				return Promise.resolve(
					new Response(JSON.stringify(mockGenerateParams)),
				);
			}
			if (params.endpoint.includes("/generate/refresh")) {
				return Promise.resolve(
					new Response(JSON.stringify(mockRefreshGenerateParams)),
				);
			}
			if (params.endpoint.includes("/generate/save")) {
				return Promise.resolve(new Response("success"));
			}
			if (params.endpoint.includes("/generate/exec")) {
				return Promise.resolve(new Response("success"));
			}
			if (params.endpoint.includes("/convert/load")) {
				return Promise.resolve(new Response(JSON.stringify(mockConvertParams)));
			}
			if (params.endpoint.includes("/convert/refresh")) {
				return Promise.resolve(
					new Response(JSON.stringify(mockRefreshConvertParams)),
				);
			}
			if (params.endpoint.includes("/convert/save")) {
				return Promise.resolve(new Response("success"));
			}
			if (params.endpoint.includes("/convert/exec")) {
				return Promise.resolve(new Response("success"));
			}
			if (params.endpoint.includes("workspace/resources")) {
				return Promise.resolve(
					new Response(JSON.stringify(workspaceResourcesFixture)),
				);
			}
			if (params.endpoint.includes("/convert/add")) {
				return Promise.resolve(
					new Response(JSON.stringify(["convert1", "convert2", "add"])),
				);
			}
			if (params.endpoint.includes("/convert/delete")) {
				return Promise.resolve(new Response(JSON.stringify(["convert1"])));
			}
			if (params.endpoint.includes("/convert/copy")) {
				return Promise.resolve(
					new Response(JSON.stringify(["convert1", "convert2", "copy"])),
				);
			}
			if (params.endpoint.includes("/convert/rename")) {
				return Promise.resolve(
					new Response(JSON.stringify(["newName", "convert2"])),
				);
			}
			return Promise.resolve(new Response());
		}),
	};
});
vi.mock("../../utils/fetchUtils", () => ({
	fetchData: mockFetchData,
	handleFetchError: vi.fn(),
	getErrorMessage: vi.fn((error: unknown) =>
		error instanceof Error ? error.message : String(error),
	),
	isAbortError: vi.fn(() => false),
}));

const mockEnvironment: Environment = { ...environmentFixture };

// カスタムラッパーコンポーネント
const wrapper = ({ children }: { children: ReactNode }) => (
	<environmentContext.Provider value={mockEnvironment}>
		<SelectParameterProvider>{children}</SelectParameterProvider>
	</environmentContext.Provider>
);
const workspaceWrapper = ({ children }: { children: ReactNode }) => (
	<environmentContext.Provider value={mockEnvironment}>
		<WorkspaceResourcesProvider>
			<SelectParameterProvider>{children}</SelectParameterProvider>
		</WorkspaceResourcesProvider>
	</environmentContext.Provider>
);

describe("SelectParameterProviderのテスト", () => {
	describe("useSelectParameter", () => {
		it("初期状態が正しいことを確認", () => {
			const { result } = renderHook(() => useSelectParameter(), { wrapper });
			waitFor(() => {
				expect(result.current).toEqual({} as SelectParameter);
			});
		});
	});
	describe("useSetSelectParameter", () => {
		it("パラメータを設定できることを確認", async () => {
			const { result } = renderHook(
				() => ({
					parameter: useSelectParameter(),
					setParameter: useSetSelectParameter(),
				}),
				{ wrapper },
			);
			await waitFor(() => {
				expect(result.current.parameter).toEqual({} as SelectParameter);
			});

			result.current.setParameter(mockConvertParams, "convert", "test-param");
			await waitFor(() => {
				expect(result.current.parameter.name).toBe("test-param");
				expect(result.current.parameter.options.command).toBe("convert");
			});
		});
	});

	describe("useLoadSelectParameter", () => {
		it.each([
			{ command: "convert", response: mockConvertParams },
			{ command: "generate", response: mockGenerateParams },
		])("指定したコマンドのパラメータを読み込めることを確認", async ({
			command,
			response,
		}) => {
			const { result } = renderHook(
				() => ({
					parameter: useSelectParameter(),
					loadParameter: useLoadSelectParameter(),
				}),
				{ wrapper },
			);
			await waitFor(() => {
				expect(result.current.parameter).toEqual({} as SelectParameter);
			});

			result.current.loadParameter(command as Command, "test-param");
			await waitFor(() => {
				expect(result.current.parameter.name).toBe("test-param");
				expect(result.current.parameter.options.command).toBe(command);
				expect(result.current.parameter).toEqual(
					new SelectParameter(response, command as Command, "test-param"),
				);
			});
		});
	});

	describe("useRefreshSelectParameter", () => {
		it.each([
			{
				command: "convert",
				loadResponse: mockConvertParams,
				refreshResponse: mockRefreshConvertParams,
			},
			{
				command: "generate",
				loadResponse: mockGenerateParams,
				refreshResponse: mockRefreshGenerateParams,
			},
		])("パラメータを更新できることを確認：convert", async ({
			command,
			loadResponse,
			refreshResponse,
		}) => {
			const { result } = renderHook(
				() => ({
					parameter: useSelectParameter(),
					loadParameter: useLoadSelectParameter(),
					refreshParameter: useRefreshSelectParameter(command),
				}),
				{ wrapper },
			);
			await waitFor(() => {
				expect(result.current.parameter).toEqual({} as SelectParameter);
			});
			result.current.loadParameter(command as Command, "test-param");
			await waitFor(() => {
				expect(result.current.parameter).toEqual(
					new SelectParameter(loadResponse, command as Command, "test-param"),
				);
			});
			const mockData = { test: "value" };
			result.current.refreshParameter(mockData);
			await waitFor(() => {
				expect(result.current.parameter.name).toBe("test-param");
				expect(result.current.parameter.options.command).toBe(command);
				expect(result.current.parameter).toEqual(
					new SelectParameter(
						refreshResponse,
						command as Command,
						"test-param",
					),
				);
			});
		});
	});

	describe("useSaveParameter", () => {
		it.each([
			{ command: "convert", params: mockConvertParams },
			{ command: "generate", params: mockGenerateParams },
		])("パラメータを保存できることを確認", async ({ command, params }) => {
			const mockHandleResult = vi.fn();
			const { result } = renderHook(
				() => ({
					parameter: useSelectParameter(),
					setParameter: useSetSelectParameter(),
					saveParameter: useSaveParameter(),
				}),
				{ wrapper },
			);
			await waitFor(() => {
				expect(result.current.parameter).toEqual({} as SelectParameter);
			});

			result.current.setParameter(params, command as Command, "test-param");
			const mockInput = { test: "value" };
			await result.current.saveParameter(mockInput, mockHandleResult);

			await waitFor(() => {
				expect(mockHandleResult).toHaveBeenCalledWith({
					command: "",
					resultMessage: "Save Success",
					resultDir: "",
				});
			});
		});

		it.each([
			{ command: "convert", params: mockConvertParams },
			{ command: "generate", params: mockGenerateParams },
		])("エラー時の処理を確認", async ({ command, params }) => {
			const mockError = new Error("Save Failed");
			mockFetchData.mockRejectedValueOnce(mockError);

			const mockHandleResult = vi.fn();
			const { result } = renderHook(
				() => ({
					parameter: useSelectParameter(),
					setParameter: useSetSelectParameter(),
					saveParameter: useSaveParameter(),
				}),
				{ wrapper },
			);

			result.current.setParameter(params, command as Command, "test-param");
			const mockInput = { test: "value" };
			await result.current.saveParameter(mockInput, mockHandleResult);

			await waitFor(() => {
				expect(mockHandleResult).toHaveBeenCalledWith({
					command: "",
					resultMessage: mockError.message,
					resultDir: "",
				});
			});
		});
	});

	describe("useExecParameter", () => {
		it.each([
			{ command: "convert", params: mockConvertParams },
			{ command: "generate", params: mockGenerateParams },
		])("パラメータを実行できることを確認", async ({ command, params }) => {
			const mockHandleResult = vi.fn();
			const { result } = renderHook(
				() => ({
					parameter: useSelectParameter(),
					setParameter: useSetSelectParameter(),
					execParameter: useExecParameter(),
				}),
				{ wrapper },
			);
			await waitFor(() => {
				expect(result.current.parameter).toEqual({} as SelectParameter);
			});

			result.current.setParameter(params, command as Command, "test-param");
			const mockInput = { test: "value" };
			await result.current.execParameter(mockInput, mockHandleResult);

			await waitFor(() => {
				expect(mockHandleResult).toHaveBeenCalledWith({
					command: "",
					resultMessage: "Execution Success",
					resultDir: "",
				});
			});
		});

		it.each([
			{ command: "convert", params: mockConvertParams },
			{ command: "generate", params: mockGenerateParams },
		])("エラー時の処理を確認", async ({ command, params }) => {
			const mockError = new Error("Execution Failed");
			mockFetchData.mockRejectedValueOnce(mockError);

			const mockHandleResult = vi.fn();
			const { result } = renderHook(
				() => ({
					parameter: useSelectParameter(),
					setParameter: useSetSelectParameter(),
					execParameter: useExecParameter(),
				}),
				{ wrapper },
			);

			result.current.setParameter(params, command as Command, "test-param");
			const mockInput = { test: "value" };
			await result.current.execParameter(mockInput, mockHandleResult);

			await waitFor(() => {
				expect(mockHandleResult).toHaveBeenCalledWith({
					command: "",
					resultMessage: mockError.message,
					resultDir: "",
				});
			});
		});
	});

	describe("パラメータ操作のテスト", () => {
		it("useAddParameterが正常に動作することを確認", async () => {
			const { result, rerender } = renderHook(
				() => ({
					addConvert: useAddParameter("convert"),
					parameterList: useParameterList(),
				}),
				{ wrapper: workspaceWrapper },
			);
			await act(async () => {
				rerender();
			});
			expect(result.current.parameterList.convert).toEqual([
				"convert1",
				"convert2",
			]);
			await act(async () => {
				result.current.addConvert();
			});
			expect(result.current.parameterList.convert).toEqual([
				"convert1",
				"convert2",
				"add",
			]);
		});

		it("useParameterActionsが正常に動作することを確認", async () => {
			const { result, rerender } = renderHook(
				() => ({
					deleteActions: useParameterActions("convert", "convert2"),
					copyActions: useParameterActions("convert", "convert1"),
					renameActions: useParameterActions("convert", "convert1"),
					parameterList: useParameterList(),
				}),
				{ wrapper: workspaceWrapper },
			);
			await act(async () => {
				rerender();
			});
			expect(result.current.parameterList.convert).toEqual([
				"convert1",
				"convert2",
			]);

			await act(async () => {
				result.current.deleteActions.handleDelete();
			});
			expect(result.current.parameterList.convert).toEqual(["convert1"]);

			await act(async () => {
				result.current.copyActions.handleCopy();
			});
			expect(result.current.parameterList.convert).toEqual([
				"convert1",
				"convert2",
				"copy",
			]);

			await act(async () => {
				result.current.renameActions.handleRename("newName");
			});
			expect(result.current.parameterList.convert).toEqual([
				"newName",
				"convert2",
			]);
		});
	});
});
