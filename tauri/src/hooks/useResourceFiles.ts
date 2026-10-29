import { useEffect, useState } from "react";
import { useEnvironment } from "../context/EnvironmentProvider";
import { useSetResourcesSettings } from "../context/WorkspaceResourcesProvider";
import type { ResourcesSettingsBuilder } from "../model/WorkspaceResources";
import {
	fetchAndUpdate,
	fetchData,
	getErrorMessage,
	handleFetchError,
	type OperationResult,
} from "../utils/fetchUtils";

// リソース種類ごとのAPIの接頭辞と、応答（更新後のファイル一覧）を書き戻すResourcesSettingsのキー
const resourceTargets = {
	datasetSetting: { base: "dataset-setting", key: "datasetSettings" },
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

// `${resource}/load`へ名前を送り、{"content": ...}形式の応答から内容を読み込む（テキスト系リソースで共通）
export const useResourceContent = (
	resource: string,
	name: string,
): { content: string; loading: boolean } => {
	const { apiUrl } = useEnvironment();
	const [content, setContent] = useState("");
	const [loading, setLoading] = useState(name !== "");

	useEffect(() => {
		if (!name) {
			setContent("");
			setLoading(false);
			return;
		}
		const controller = new AbortController();
		setLoading(true);
		loadResourceContent(apiUrl, resource, name).then((result) => {
			if (!controller.signal.aborted) {
				setContent(result);
				setLoading(false);
			}
		});
		return () => {
			controller.abort();
		};
	}, [resource, name, apiUrl]);

	return { content, loading };
};

async function loadResourceContent(
	apiUrl: string,
	resource: string,
	name: string,
): Promise<string> {
	const params = {
		endpoint: `${apiUrl}${resource}/load`,
		options: {
			method: "POST",
			headers: { "Content-Type": "text/plain" },
			body: name,
		},
	};
	try {
		const response = await fetchData(params);
		const data = (await response.json()) as { content?: string };
		return data.content ?? "";
	} catch (e) {
		handleFetchError(getErrorMessage(e), params);
		return "";
	}
}
