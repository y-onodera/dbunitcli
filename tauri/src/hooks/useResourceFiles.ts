import { useEffect, useState } from "react";
import { useEnvironment } from "../context/EnvironmentProvider";
import { useResourceEncoding } from "../context/ResourceEncodingProvider";
import { useSetResourcesSettings } from "../context/WorkspaceResourcesProvider";
import type { ResourceLocation } from "../model/ResourceLocation";
import type {
	ResourcesSettings,
	ResourcesSettingsBuilder,
} from "../model/WorkspaceResources";
import {
	fetchAndUpdate,
	fetchData,
	getErrorMessage,
	handleFetchError,
	type OperationResult,
	postJsonParams,
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

// `${base}/save`へ名前・内容と、ResourceEncodingProviderのその種類の文字コードを送る（保存で共通）。
// 保存先の選び方（overwrite）はsidecarのResourceFile.save
export const useSaveResource = <T>(kind: ResourceKind) => {
	const updateResourceFiles = useUpdateResourceFiles(kind);
	const encoding = useResourceEncoding(kind);
	return (name: string, input: T, overwrite: boolean) =>
		updateResourceFiles(
			"save",
			JSON.stringify({ name, encoding, input, overwrite }),
		);
};

// テキスト系リソースの内容を{content}形式で保存する
export const useSaveResourceContent = (kind: ResourceKind) => {
	const save = useSaveResource<{ content: string }>(kind);
	return (name: string, content: string, overwrite: boolean) =>
		save(name, { content }, overwrite);
};

// 名前が空・読込失敗・見つからないときの値を作る関数と、応答のcontentから値を作る関数。
// effectの依存に入るので、モジュールレベルの関数を渡す（hooks-design.md）
export type ResourceParser<T> = {
	empty: () => T;
	parse: (content: unknown) => T;
};

type Loaded<T> = { data: T; location: ResourceLocation | null };

// `${base}/load`へ名前と文字コード（コマンド実行時にそのファイルを読む文字コード）を送り、内容と開いたファイルの場所を読み込む。
// 名前が空なら問い合わせない。名前を変えたり離れたりしたら中断する。失敗はalertで知らせる
export const useResourceLoad = <T>(
	kind: ResourceKind,
	name: string,
	parser: ResourceParser<T>,
): Loaded<T> & { loading: boolean } => {
	const { apiUrl } = useEnvironment();
	const encoding = useResourceEncoding(kind);
	const [loaded, setLoaded] = useState(() => emptyLoaded(parser));
	const [loading, setLoading] = useState(name !== "");

	useEffect(() => {
		if (!name) {
			setLoaded(emptyLoaded(parser));
			setLoading(false);
			return;
		}
		const controller = new AbortController();
		setLoading(true);
		requestLoad(
			`${apiUrl}${resourceTargets[kind].base}/load`,
			{ name, encoding },
			parser,
			controller.signal,
		).then((result) => {
			if (!controller.signal.aborted) {
				setLoaded(result);
				setLoading(false);
			}
		});
		return () => {
			controller.abort();
		};
	}, [kind, name, apiUrl, encoding, parser]);

	return { ...loaded, loading };
};

function emptyLoaded<T>(parser: ResourceParser<T>): Loaded<T> {
	return { data: parser.empty(), location: null };
}

async function requestLoad<T>(
	endpoint: string,
	body: object,
	parser: ResourceParser<T>,
	signal: AbortSignal,
): Promise<Loaded<T>> {
	const params = { ...postJsonParams(endpoint, body), signal };
	try {
		const response = await fetchData(params);
		const json = (await response.json()) as {
			content: unknown;
			location: ResourceLocation | null;
		};
		if (json.location === null) {
			return emptyLoaded(parser);
		}
		return { data: parser.parse(json.content), location: json.location };
	} catch (e) {
		handleFetchError(getErrorMessage(e), params);
		return emptyLoaded(parser);
	}
}

// テキスト系リソースの内容（contentが文字列）
export const CONTENT_PARSER: ResourceParser<string> = {
	empty: () => "",
	parse: (content) => content as string,
};
