import { useEnvironment } from "../context/EnvironmentProvider";
import { useSetResourcesSettings } from "../context/WorkspaceResourcesProvider";
import type { ResourcesSettingsBuilder } from "../model/WorkspaceResources";
import { fetchAndUpdate, type OperationResult } from "../utils/fetchUtils";

type ResourceFilesKey = keyof ResourcesSettingsBuilder;

// POSTの応答（更新後のファイル一覧）をResourcesSettingsのkeyへ書き戻す関数を返す（保存・削除で共通）
export const useUpdateResourceFiles = (key: ResourceFilesKey) => {
	const { apiUrl } = useEnvironment();
	const setResourcesSettings = useSetResourcesSettings();
	return async (
		endpoint: string,
		body: string,
		contentType = "application/json",
	): Promise<OperationResult> =>
		fetchAndUpdate<string[]>(
			{
				endpoint: `${apiUrl}${endpoint}`,
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

// 削除APIのエンドポイントと、応答を書き戻すResourcesSettingsのキー
const deleteTargets = {
	datasetSetting: {
		endpoint: "dataset-setting/delete",
		key: "datasetSettings",
	},
	xlsxSchema: { endpoint: "xlsx-schema/delete", key: "xlsxSchemas" },
	jdbc: { endpoint: "jdbc/delete", key: "jdbcFiles" },
	template: { endpoint: "template/delete", key: "templateFiles" },
	fixedColumnDef: {
		endpoint: "fixed-column-def/delete",
		key: "fixedColumnDefs",
	},
} as const satisfies Record<
	string,
	{ endpoint: string; key: ResourceFilesKey }
>;

export type ResourceKind = keyof typeof deleteTargets;

export const useDeleteResource = (kind: ResourceKind) => {
	const { endpoint, key } = deleteTargets[kind];
	const updateResourceFiles = useUpdateResourceFiles(key);
	return (name: string) => updateResourceFiles(endpoint, name, "text/plain");
};
