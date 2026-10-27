import { useEnvironment } from "../context/EnvironmentProvider";
import { useSetResourcesSettings } from "../context/WorkspaceResourcesProvider";
import type { ResourcesSettingsBuilder } from "../model/WorkspaceResources";
import { fetchAndUpdate, type OperationResult } from "../utils/fetchUtils";

// 削除APIのエンドポイントと、応答（残ったファイル一覧）を書き戻すResourcesSettingsのキー
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
	{ endpoint: string; key: keyof ResourcesSettingsBuilder }
>;

export type ResourceKind = keyof typeof deleteTargets;

export const useDeleteResource = (kind: ResourceKind) => {
	const { apiUrl } = useEnvironment();
	const setResourcesSettings = useSetResourcesSettings();
	const { endpoint, key } = deleteTargets[kind];
	return async (name: string): Promise<OperationResult> =>
		fetchAndUpdate<string[]>(
			{
				endpoint: `${apiUrl}${endpoint}`,
				options: {
					method: "POST",
					headers: { "Content-Type": "text/plain" },
					body: name,
				},
			},
			(files) =>
				setResourcesSettings((current) => current.with({ [key]: files })),
		);
};
