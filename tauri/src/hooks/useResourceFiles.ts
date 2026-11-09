import { useEffect, useState } from "react";
import { useEnvironment } from "../context/EnvironmentProvider";
import { useResourceEncoding } from "../context/ResourceEncodingProvider";
import { useSetResourcesSettings } from "../context/WorkspaceResourcesProvider";
import type {
	ResourcesSettings,
	ResourcesSettingsBuilder,
} from "../model/WorkspaceResources";
import {
	type FetchParams,
	fetchAndUpdate,
	fetchData,
	getErrorMessage,
	handleFetchError,
	type OperationResult,
} from "../utils/fetchUtils";

// リソース種類ごとのAPIの接頭辞と、応答（更新後のファイル一覧）を書き戻すResourcesSettingsのキー
const resourceTargets = {
	datasetSetting: { base: "dataset-setting", key: "datasetSettings" },
	// compareの-setting（比較キー設定）とgenerateの-unitSetting。形式はdataset-settingと同じで、保存先だけ違う
	compareSetting: { base: "compare/setting", key: "compareSettings" },
	generateSetting: { base: "generate/setting", key: "generateSettings" },
	xlsxSchema: { base: "xlsx-schema", key: "xlsxSchemas" },
	jdbc: { base: "jdbc", key: "jdbcFiles" },
	template: { base: "template", key: "templateFiles" },
	parameterizeTemplate: {
		base: "parameterize/template",
		key: "parameterizeTemplateFiles",
	},
	fixedColumnDef: { base: "fixed-column-def", key: "fixedColumnDefs" },
	sql: { base: "sql", key: "sqlFiles" },
	table: { base: "table", key: "tableFiles" },
	csvq: { base: "csvq", key: "csvqFiles" },
} as const satisfies Record<
	string,
	{ base: string; key: keyof ResourcesSettingsBuilder }
>;

export type ResourceKind = keyof typeof resourceTargets;

// dataset-setting形式のJSONを保存するリソースの種類
export type DatasetSettingKind =
	| "datasetSetting"
	| "compareSetting"
	| "generateSetting";

// `${base}/load`へ名前と、コマンド実行時にそのファイルを読む文字コードを送るリクエスト（読込で共通）
export function resourceLoadParams(
	apiUrl: string,
	kind: ResourceKind,
	name: string,
	encoding: string,
): FetchParams {
	return {
		endpoint: `${apiUrl}${resourceTargets[kind].base}/load`,
		options: {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({ name, encoding }),
		},
	};
}

// 種類に対応するResourcesSettings上のファイル一覧（入力欄の候補）
export function resourceFilesOf(
	settings: ResourcesSettings,
	kind: ResourceKind,
): string[] {
	return settings[resourceTargets[kind].key];
}

// `${base}/${action}`へPOSTし、応答をResourcesSettingsへ書き戻す関数を返す（保存・削除で共通）
export const useUpdateResourceFiles = (kind: ResourceKind) => {
	const { apiUrl } = useEnvironment();
	const setResourcesSettings = useSetResourcesSettings();
	const { base, key } = resourceTargets[kind];
	return async (
		action: string,
		body: string,
		contentType = "application/json",
	): Promise<OperationResult> =>
		fetchAndUpdate<string[]>(
			{
				endpoint: `${apiUrl}${base}/${action}`,
				options: {
					method: "POST",
					headers: { "Content-Type": contentType },
					body,
				},
			},
			(files) =>
				setResourcesSettings((current) => current.with({ [key]: files })),
		);
};

export const useDeleteResource = (kind: ResourceKind) => {
	const updateResourceFiles = useUpdateResourceFiles(kind);
	return (name: string) => updateResourceFiles("delete", name, "text/plain");
};

// `${base}/save`へ名前・内容と、ResourceEncodingProviderのその種類の文字コードを送る（保存で共通）
export const useSaveResource = <T>(kind: ResourceKind) => {
	const updateResourceFiles = useUpdateResourceFiles(kind);
	const encoding = useResourceEncoding(kind);
	return (name: string, input: T) =>
		updateResourceFiles("save", JSON.stringify({ name, encoding, input }));
};

// テキスト系リソースの内容を{content}形式で保存する
export const useSaveResourceContent = (kind: ResourceKind) => {
	const save = useSaveResource<{ content: string }>(kind);
	return (name: string, content: string) => save(name, { content });
};

// 名前が空・読込失敗のときの値を作る関数と、応答のJSONから値を作る関数。
// effectの依存に入るので、モジュールレベルの関数を渡す（hooks-design.md）
export type ResourceParser<T> = {
	empty: () => T;
	parse: (json: unknown) => T;
};

// `${base}/load`へ名前を送り、応答をparserで変換して返す。名前を変えたり離れたりしたら読込を中断する
export const useResourceLoad = <T>(
	kind: ResourceKind,
	name: string,
	parser: ResourceParser<T>,
): { data: T; loading: boolean } => {
	const { apiUrl } = useEnvironment();
	const encoding = useResourceEncoding(kind);
	const [data, setData] = useState(parser.empty);
	const [loading, setLoading] = useState(name !== "");

	useEffect(() => {
		if (!name) {
			setData(parser.empty());
			setLoading(false);
			return;
		}
		const controller = new AbortController();
		setLoading(true);
		loadResource(apiUrl, kind, name, encoding, parser, controller.signal).then(
			(result) => {
				if (!controller.signal.aborted) {
					setData(result);
					setLoading(false);
				}
			},
		);
		return () => {
			controller.abort();
		};
	}, [kind, name, apiUrl, encoding, parser]);

	return { data, loading };
};

// 失敗はalertで知らせて空の値を返す（中断したときは知らせない）
async function loadResource<T>(
	apiUrl: string,
	kind: ResourceKind,
	name: string,
	encoding: string,
	parser: ResourceParser<T>,
	signal: AbortSignal,
): Promise<T> {
	const params = {
		...resourceLoadParams(apiUrl, kind, name, encoding),
		signal,
	};
	try {
		const response = await fetchData(params);
		return parser.parse(await response.json());
	} catch (e) {
		if (!signal.aborted) {
			handleFetchError(getErrorMessage(e), params);
		}
		return parser.empty();
	}
}

const CONTENT_PARSER: ResourceParser<string> = {
	empty: () => "",
	parse: (json) => (json as { content?: string }).content ?? "",
};

// {"content": ...}形式の応答から内容を読み込む（テキスト系リソースで共通）
export const useResourceContent = (
	kind: ResourceKind,
	name: string,
): { content: string; loading: boolean } => {
	const { data, loading } = useResourceLoad(kind, name, CONTENT_PARSER);
	return { content: data, loading };
};
