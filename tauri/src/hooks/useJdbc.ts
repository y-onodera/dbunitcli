import type { Dispatch, SetStateAction } from "react";
import { useCallback } from "react";
import { useEnviroment } from "../context/EnviromentProvider";
import { useSetResourcesSettings } from "../context/WorkspaceResourcesProvider";
import type { ResourcesSettings } from "../model/WorkspaceResources";
import { fetchData, handleFetchError } from "../utils/fetchUtils";

type OperationResult = "success" | "failed";

export const useDeleteJdbcProperties = () => {
	const { apiUrl } = useEnviroment();
	const setResourcesSettings = useSetResourcesSettings();
	return async (name: string): Promise<OperationResult> => {
		return deleteJdbcProperties(apiUrl, name, setResourcesSettings);
	};
};

async function deleteJdbcProperties(
	apiUrl: string,
	name: string,
	setResourcesSettings: Dispatch<SetStateAction<ResourcesSettings>>,
): Promise<OperationResult> {
	const fetchParams = {
		endpoint: `${apiUrl}jdbc/delete`,
		options: {
			method: "POST",
			headers: { "Content-Type": "text/plain" },
			body: name,
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

function toJdbcRequestBody(jdbcValues: Record<string, string>) {
	return {
		url: jdbcValues.jdbcUrl ?? "",
		user: jdbcValues.jdbcUser ?? "",
		pass: jdbcValues.jdbcPass ?? "",
		properties: jdbcValues.jdbcProperties ?? "",
	};
}

export const useJdbcReadContent = () => {
	const { apiUrl } = useEnviroment();
	return useCallback(
		async (path: string): Promise<Record<string, string>> => {
			const params = {
				endpoint: `${apiUrl}jdbc/read-content`,
				options: {
					method: "POST",
					headers: { "Content-Type": "text/plain" },
					body: path,
				},
			};
			try {
				const response = await fetchData(params);
				return (await response.json()) as Record<string, string>;
			} catch (e) {
				handleFetchError((e as Error).message, params);
				return {};
			}
		},
		[apiUrl],
	);
};

export const useJdbcTables = () => {
	const { apiUrl } = useEnviroment();
	return useCallback(
		async (jdbcValues: Record<string, string>): Promise<string[]> => {
			const params = {
				endpoint: `${apiUrl}jdbc/tables`,
				options: {
					method: "POST",
					headers: { "Content-Type": "application/json" },
					body: JSON.stringify(toJdbcRequestBody(jdbcValues)),
				},
			};
			try {
				const response = await fetchData(params);
				return (await response.json()) as string[];
			} catch (e) {
				handleFetchError((e as Error).message, params);
				return [];
			}
		},
		[apiUrl],
	);
};

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
