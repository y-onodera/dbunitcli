import type { Dispatch, SetStateAction } from "react";
import { useEnviroment } from "../context/EnviromentProvider";
import { useSetResourcesSettings } from "../context/WorkspaceResourcesProvider";
import type { ResourcesSettings } from "../model/WorkspaceResources";
import { fetchData, handleFetchError } from "../utils/fetchUtils";

type OperationResult = "success" | "failed";

export const useJdbcSaveProperties = () => {
	const { apiUrl } = useEnviroment();
	const setResourcesSettings = useSetResourcesSettings();
	return async (
		name: string,
		jdbcValues: Record<string, string>,
	): Promise<OperationResult> => {
		return saveJdbcProperties(apiUrl, name, jdbcValues, setResourcesSettings);
	};
};

async function saveJdbcProperties(
	apiUrl: string,
	name: string,
	jdbcValues: Record<string, string>,
	setResourcesSettings: Dispatch<SetStateAction<ResourcesSettings>>,
): Promise<OperationResult> {
	const fetchParams = {
		endpoint: `${apiUrl}jdbc/save-properties`,
		options: {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({
				name,
				url: jdbcValues.jdbcUrl ?? "",
				user: jdbcValues.jdbcUser ?? "",
				pass: jdbcValues.jdbcPass ?? "",
				properties: jdbcValues.jdbcProperties ?? "",
			}),
		},
	};

	return await fetchData(fetchParams)
		.then((response) => response.json())
		.then((files: string[]) => {
			setResourcesSettings((current) => current.with({ jdbcFiles: files }));
			return "success" as OperationResult;
		})
		.catch((ex) => {
			handleFetchError((ex as Error).message, fetchParams);
			return "failed" as OperationResult;
		});
}
