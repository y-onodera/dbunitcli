import { act, renderHook } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { environmentContext } from "../../context/EnvironmentProvider";
import { ResourceEncodingProvider } from "../../context/ResourceEncodingProvider";
import WorkspaceResourcesProvider, {
	useResourcesSettings,
} from "../../context/WorkspaceResourcesProvider";
import { useSaveDatasetSettings } from "../../hooks/useDatasetSettings";
import { useSaveFixedColumnDef } from "../../hooks/useFixedColumnDef";
import { useSaveJdbcProperties } from "../../hooks/useJdbc";
import {
	CONTENT_PARSER,
	type ResourceKind,
	useDeleteResource,
	useResourceLoad,
	useSaveResourceContent,
} from "../../hooks/useResourceFiles";
import { useSaveXlsxSchema } from "../../hooks/useXlsxSchema";
import { DatasetSettings } from "../../model/DatasetSettings";
import { FixedColumnDef } from "../../model/FixedColumnDef";
import type { ResourcesSettingsBuilder } from "../../model/WorkspaceResources";
import { XlsxSchema } from "../../model/XlsxSchema";
import type { FetchParams, OperationResult } from "../../utils/fetchUtils";
import { environmentFixture, workspaceResourcesFixture } from "../setup";

const mockUpdatedFiles = ["updated"];

// 種類ごとに違う文字コードを与え、各フックが自分の種類の値を使うことを確かめる
function encodingOf(kind: ResourceKind): string {
	return `encoding-${kind}`;
}

const resourceEncodings = Object.fromEntries(
	(
		[
			"datasetSetting",
			"compareSetting",
			"generateSetting",
			"xlsxSchema",
			"fixedColumnDef",
			"template",
			"parameterizeTemplate",
			"sql",
			"table",
			"csvq",
		] as const
	).map((kind) => [kind, encodingOf(kind)]),
);

const wrapper = ({ children }: { children: React.ReactNode }) => (
	<environmentContext.Provider value={environmentFixture}>
		<WorkspaceResourcesProvider>
			<ResourceEncodingProvider encodings={resourceEncodings}>
				{children}
			</ResourceEncodingProvider>
		</WorkspaceResourcesProvider>
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

vi.mock("../../utils/fetchUtils", async (importOriginal) => ({
	...(await importOriginal<typeof import("../../utils/fetchUtils")>()),
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
		{
			kind: "compareSetting",
			endpoint: "compare/setting/delete",
			key: "compareSettings",
		},
		{
			kind: "generateSetting",
			endpoint: "generate/setting/delete",
			key: "generateSettings",
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
		{ kind: "sql", endpoint: "sql/delete", key: "sqlFiles" },
		{ kind: "table", endpoint: "table/delete", key: "tableFiles" },
		{ kind: "csvq", endpoint: "csvq/delete", key: "csvqFiles" },
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

describe("useResourceLoadのテスト", () => {
	const location = {
		folder: "/ws/other",
		defaultFolder: "/ws/resources/sql",
		fileName: "q.sql",
		defaultFolderHasFile: true,
	};

	it("名前と、ResourceEncodingProviderのその種類の文字コードを送って読み込むことを確認", async () => {
		mockFetchData.mockClear();
		const { rerender } = renderHook(
			() => useResourceLoad("sql", "target.sql", CONTENT_PARSER),
			{ wrapper },
		);
		await act(async () => {
			rerender();
		});
		const loads = mockFetchData.mock.calls.filter(([params]) =>
			params.endpoint.endsWith("/sql/load"),
		);
		expect(JSON.parse(String(loads[0][0].options.body))).toStrictEqual({
			name: "target.sql",
			encoding: encodingOf("sql"),
		});
	});

	it("内容と開いたファイルの場所を受け取る", async () => {
		mockFetchData.mockImplementation((params: FetchParams) =>
			Promise.resolve(
				new Response(
					params.endpoint.endsWith("/sql/load")
						? JSON.stringify({ content: "select 1", location })
						: JSON.stringify(workspaceResourcesFixture),
				),
			),
		);
		const { result, rerender } = renderHook(
			() => useResourceLoad("sql", "other/q.sql", CONTENT_PARSER),
			{ wrapper },
		);
		await act(async () => {
			rerender();
		});
		expect(result.current).toStrictEqual({
			data: "select 1",
			location,
			loading: false,
		});
	});

	it("見つからない（locationがnull）なら空の値とnull", async () => {
		mockFetchData.mockImplementation((params: FetchParams) =>
			Promise.resolve(
				new Response(
					params.endpoint.endsWith("/sql/load")
						? JSON.stringify({ content: "", location: null })
						: JSON.stringify(workspaceResourcesFixture),
				),
			),
		);
		const { result, rerender } = renderHook(
			() => useResourceLoad("sql", "missing.sql", CONTENT_PARSER),
			{ wrapper },
		);
		await act(async () => {
			rerender();
		});
		expect(result.current).toStrictEqual({
			data: "",
			location: null,
			loading: false,
		});
	});

	it("名前が空なら問い合わせず空の値とnull", async () => {
		mockFetchData.mockClear();
		const { result, rerender } = renderHook(
			() => useResourceLoad("sql", "", CONTENT_PARSER),
			{ wrapper },
		);
		await act(async () => {
			rerender();
		});
		expect(result.current).toStrictEqual({
			data: "",
			location: null,
			loading: false,
		});
		expect(
			mockFetchData.mock.calls.some(([params]) =>
				params.endpoint.endsWith("/load"),
			),
		).toBe(false);
	});
});

describe("上書き保存のテスト", () => {
	it("overwriteを指定するとそのまま送る", async () => {
		const { params } = await runWithResources(() => {
			const save = useSaveResourceContent("sql");
			return (name: string) => save(name, "content", true);
		});
		expect(JSON.parse(String(params?.options.body)).overwrite).toBe(true);
	});
});

describe("保存フックのテスト", () => {
	it.each<{
		name: string;
		useSave: () => (name: string) => Promise<OperationResult>;
		endpoint: string;
		input: unknown;
		key: keyof ResourcesSettingsBuilder;
		// 送る文字コード（ResourceEncodingProviderのその種類の値）
		encoding?: string;
	}>([
		{
			name: "useSaveXlsxSchema",
			useSave: () => {
				const save = useSaveXlsxSchema();
				return (name) => save(name, xlsxSchema, false);
			},
			endpoint: "xlsx-schema/save",
			input: xlsxSchema,
			key: "xlsxSchemas",
			encoding: encodingOf("xlsxSchema"),
		},
		{
			name: "useSaveFixedColumnDef",
			useSave: () => {
				const save = useSaveFixedColumnDef();
				return (name) => save(name, fixedColumnDef, false);
			},
			endpoint: "fixed-column-def/save",
			input: fixedColumnDef,
			key: "fixedColumnDefs",
			encoding: encodingOf("fixedColumnDef"),
		},
		{
			name: "useSaveDatasetSettings",
			useSave: () => {
				const save = useSaveDatasetSettings();
				return (name) => save(name, datasetSettings, false);
			},
			endpoint: "dataset-setting/save",
			input: datasetSettings,
			key: "datasetSettings",
			encoding: encodingOf("datasetSetting"),
		},
		...(
			[
				{
					kind: "compareSetting",
					endpoint: "compare/setting/save",
					key: "compareSettings",
				},
				{
					kind: "generateSetting",
					endpoint: "generate/setting/save",
					key: "generateSettings",
				},
			] as const
		).map(({ kind, endpoint, key }) => ({
			name: `useSaveDatasetSettings(${kind})`,
			useSave: () => {
				const save = useSaveDatasetSettings(kind);
				return (name: string) => save(name, datasetSettings, false);
			},
			endpoint,
			input: datasetSettings,
			key,
			encoding: encodingOf(kind),
		})),
		...(
			[
				{ kind: "template", endpoint: "template/save", key: "templateFiles" },
				{
					kind: "parameterizeTemplate",
					endpoint: "parameterize/template/save",
					key: "parameterizeTemplateFiles",
				},
				{ kind: "sql", endpoint: "sql/save", key: "sqlFiles" },
				{ kind: "table", endpoint: "table/save", key: "tableFiles" },
				{ kind: "csvq", endpoint: "csvq/save", key: "csvqFiles" },
			] as const
		).map(({ kind, endpoint, key }) => ({
			name: `useSaveResourceContent(${kind})`,
			useSave: () => {
				const save = useSaveResourceContent(kind);
				return (name: string) => save(name, "content", false);
			},
			endpoint,
			input: { content: "content" },
			key,
			encoding: encodingOf(kind),
		})),
		{
			name: "useSaveJdbcProperties",
			useSave: () => {
				const save = useSaveJdbcProperties();
				return (name) => save(name, jdbcValues, false);
			},
			endpoint: "jdbc/save-properties",
			input: { url: "jdbc:test", user: "user", pass: "pass", properties: "" },
			key: "jdbcFiles",
		},
	])(
		"$nameは$endpointへ名前と内容を送り、応答で$keyを更新する",
		async ({ useSave, endpoint, input, key, encoding }) => {
			const { operationResult, params, resources } =
				await runWithResources(useSave);

			expect(operationResult).toBe("success");
			expect(params?.endpoint).toBe(`${environmentFixture.apiUrl}${endpoint}`);
			expect(JSON.parse(String(params?.options.body))).toStrictEqual({
				name: "target",
				...(encoding === undefined ? {} : { encoding }),
				input: JSON.parse(JSON.stringify(input)),
				overwrite: false,
			});
			expect(resources[key]).toStrictEqual(mockUpdatedFiles);
		},
	);
});
