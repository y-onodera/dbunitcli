import { act, renderHook } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { environmentContext } from "../../context/EnvironmentProvider";
import WorkspaceResourcesProvider, {
	useResourcesSettings,
} from "../../context/WorkspaceResourcesProvider";
import { useSaveDatasetSettings } from "../../hooks/useDatasetSettings";
import { useSaveFixedColumnDef } from "../../hooks/useFixedColumnDef";
import { useSaveJdbcProperties } from "../../hooks/useJdbc";
import {
	type ResourceKind,
	useDeleteResource,
} from "../../hooks/useResourceFiles";
import {
	useSaveParameterizeTemplate,
	useSaveTemplate,
} from "../../hooks/useTemplate";
import { useSaveXlsxSchema } from "../../hooks/useXlsxSchema";
import { DatasetSettings } from "../../model/DatasetSettings";
import { FixedColumnDef } from "../../model/FixedColumnDef";
import type { ResourcesSettingsBuilder } from "../../model/WorkspaceResources";
import { XlsxSchema } from "../../model/XlsxSchema";
import type { FetchParams, OperationResult } from "../../utils/fetchUtils";
import { environmentFixture, workspaceResourcesFixture } from "../setup";

const mockUpdatedFiles = ["updated"];

const wrapper = ({ children }: { children: React.ReactNode }) => (
	<environmentContext.Provider value={environmentFixture}>
		<WorkspaceResourcesProvider>{children}</WorkspaceResourcesProvider>
	</environmentContext.Provider>
);

const { mockFetchData, mockFetchAndUpdate } = vi.hoisted(() => {
	const mockFetchData = vi.fn((params: FetchParams) => {
		if (params.endpoint.includes("/workspace/resources")) {
			return Promise.resolve(
				new Response(JSON.stringify(workspaceResourcesFixture)),
			);
		}
		// 保存・削除の応答は更新後のファイル一覧
		return Promise.resolve(new Response(JSON.stringify(mockUpdatedFiles)));
	});
	const mockFetchAndUpdate = vi.fn(
		async (params: FetchParams, onSuccess: (data: unknown) => void) => {
			const response = await mockFetchData(params);
			const data = await response.json();
			onSuccess(data);
			return "success" as const;
		},
	);
	return { mockFetchData, mockFetchAndUpdate };
});

vi.mock("../../utils/fetchUtils", () => ({
	fetchData: mockFetchData,
	fetchAndUpdate: mockFetchAndUpdate,
}));

// 名前"target"で操作を実行し、結果・送ったリクエスト・更新後のResourcesSettingsを返す
async function runWithResources(
	useOperation: () => (name: string) => Promise<OperationResult>,
) {
	const { result, rerender } = renderHook(
		() => ({ operation: useOperation(), resources: useResourcesSettings() }),
		{ wrapper },
	);
	// Providerがworkspaceの読込で一度suspendするため、解決を待つ
	await act(async () => {
		rerender();
	});
	let operationResult: OperationResult | undefined;
	await act(async () => {
		operationResult = await result.current.operation("target");
	});
	const [params] = mockFetchAndUpdate.mock.lastCall ?? [];
	return { operationResult, params, resources: result.current.resources };
}

describe("useDeleteResourceのテスト", () => {
	it.each<{
		kind: ResourceKind;
		endpoint: string;
		key: keyof ResourcesSettingsBuilder;
	}>([
		{
			kind: "datasetSetting",
			endpoint: "dataset-setting/delete",
			key: "datasetSettings",
		},
		{ kind: "xlsxSchema", endpoint: "xlsx-schema/delete", key: "xlsxSchemas" },
		{ kind: "jdbc", endpoint: "jdbc/delete", key: "jdbcFiles" },
		{ kind: "template", endpoint: "template/delete", key: "templateFiles" },
		{
			kind: "parameterizeTemplate",
			endpoint: "parameterize/template/delete",
			key: "parameterizeTemplateFiles",
		},
		{
			kind: "fixedColumnDef",
			endpoint: "fixed-column-def/delete",
			key: "fixedColumnDefs",
		},
	])(
		"kind=$kindのとき、$endpointへ名前を送り、応答で$keyを更新する",
		async ({ kind, endpoint, key }) => {
			const { operationResult, params, resources } = await runWithResources(
				() => useDeleteResource(kind),
			);

			expect(operationResult).toBe("success");
			expect(params?.endpoint).toBe(`${environmentFixture.apiUrl}${endpoint}`);
			expect(params?.options.body).toBe("target");
			expect(resources[key]).toStrictEqual(mockUpdatedFiles);
		},
	);
});

const xlsxSchema = XlsxSchema.create();
const fixedColumnDef = FixedColumnDef.create();
const datasetSettings = DatasetSettings.create();
const jdbcValues = {
	jdbcUrl: "jdbc:test",
	jdbcUser: "user",
	jdbcPass: "pass",
	jdbcProperties: "",
};

describe("保存フックのテスト", () => {
	it.each<{
		name: string;
		useSave: () => (name: string) => Promise<OperationResult>;
		endpoint: string;
		input: unknown;
		key: keyof ResourcesSettingsBuilder;
	}>([
		{
			name: "useSaveXlsxSchema",
			useSave: () => {
				const save = useSaveXlsxSchema();
				return (name) => save(name, xlsxSchema);
			},
			endpoint: "xlsx-schema/save",
			input: xlsxSchema,
			key: "xlsxSchemas",
		},
		{
			name: "useSaveFixedColumnDef",
			useSave: () => {
				const save = useSaveFixedColumnDef();
				return (name) => save(name, fixedColumnDef);
			},
			endpoint: "fixed-column-def/save",
			input: fixedColumnDef,
			key: "fixedColumnDefs",
		},
		{
			name: "useSaveDatasetSettings",
			useSave: () => {
				const save = useSaveDatasetSettings();
				return (name) => save(name, datasetSettings);
			},
			endpoint: "dataset-setting/save",
			input: datasetSettings,
			key: "datasetSettings",
		},
		{
			name: "useSaveTemplate",
			useSave: () => {
				const save = useSaveTemplate();
				return (name) => save(name, "template content");
			},
			endpoint: "template/save",
			input: { content: "template content" },
			key: "templateFiles",
		},
		{
			name: "useSaveParameterizeTemplate",
			useSave: () => {
				const save = useSaveParameterizeTemplate();
				return (name) => save(name, "template content");
			},
			endpoint: "parameterize/template/save",
			input: { content: "template content" },
			key: "parameterizeTemplateFiles",
		},
		{
			name: "useSaveJdbcProperties",
			useSave: () => {
				const save = useSaveJdbcProperties();
				return (name) => save(name, jdbcValues);
			},
			endpoint: "jdbc/save-properties",
			input: { url: "jdbc:test", user: "user", pass: "pass", properties: "" },
			key: "jdbcFiles",
		},
	])(
		"$nameは$endpointへ名前と内容を送り、応答で$keyを更新する",
		async ({ useSave, endpoint, input, key }) => {
			const { operationResult, params, resources } =
				await runWithResources(useSave);

			expect(operationResult).toBe("success");
			expect(params?.endpoint).toBe(`${environmentFixture.apiUrl}${endpoint}`);
			expect(JSON.parse(String(params?.options.body))).toStrictEqual({
				name: "target",
				input: JSON.parse(JSON.stringify(input)),
			});
			expect(resources[key]).toStrictEqual(mockUpdatedFiles);
		},
	);
});
