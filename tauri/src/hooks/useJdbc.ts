import type { Dispatch, SetStateAction } from "react";
import { useEnviroment } from "../context/EnviromentProvider";
import { useSetResourcesSettings } from "../context/WorkspaceResourcesProvider";
import type { ResourcesSettings } from "../model/WorkspaceResources";
import { fetchData, handleFetchError } from "../utils/fetchUtils";

type OperationResult = "success" | "failed";

function toJdbcRequestBody(jdbcValues: Record<string, string>) {
	return {
		url: jdbcValues.jdbcUrl ?? "",
		user: jdbcValues.jdbcUser ?? "",
		pass: jdbcValues.jdbcPass ?? "",
		properties: jdbcValues.jdbcProperties ?? "",
	};
}

export const useJdbcConnectionTest = () => {
	const { apiUrl } = useEnviroment();
	return async (
		jdbcValues: Record<string, string>,
	): Promise<{ success: boolean; message: string } | null> => {
		const params = {
			endpoint: `${apiUrl}jdbc/test`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify(toJdbcRequestBody(jdbcValues)),
			},
		};
		try {
			const response = await fetchData(params);
			return (await response.json()) as { success: boolean; message: string };
		} catch (e) {
			handleFetchError((e as Error).message, params);
			return null;
		}
	};
};

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
			body: JSON.stringify({ name, input: toJdbcRequestBody(jdbcValues) }),
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
