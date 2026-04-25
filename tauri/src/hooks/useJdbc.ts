import { useEnvironment } from "../context/EnvironmentProvider";
import { useSetResourcesSettings } from "../context/WorkspaceResourcesProvider";
import { fetchAndUpdate, fetchData, getErrorMessage, handleFetchError, type OperationResult } from "../utils/fetchUtils";

function toJdbcRequestBody(jdbcValues: Record<string, string>) {
	return {
		url: jdbcValues.jdbcUrl ?? "",
		user: jdbcValues.jdbcUser ?? "",
		pass: jdbcValues.jdbcPass ?? "",
		properties: jdbcValues.jdbcProperties ?? "",
	};
}

export const useDeleteJdbcProperties = () => {
	const { apiUrl } = useEnvironment();
	const setResourcesSettings = useSetResourcesSettings();
	return async (name: string): Promise<OperationResult> =>
		fetchAndUpdate<string[]>(
			{
				endpoint: `${apiUrl}jdbc/delete`,
				options: { method: "POST", headers: { "Content-Type": "text/plain" }, body: name },
			},
			(files) => setResourcesSettings((current) => current.with({ jdbcFiles: files })),
		);
};

export const useJdbcTables = () => {
	const { apiUrl } = useEnvironment();
	return async (jdbcValues: Record<string, string>): Promise<string[]> => {
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
			handleFetchError(getErrorMessage(e), params);
			return [];
		}
	};
};

export const useJdbcConnectionTest = () => {
	const { apiUrl } = useEnvironment();
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
			handleFetchError(getErrorMessage(e), params);
			return null;
		}
	};
};

export const useJdbcSaveProperties = () => {
	const { apiUrl } = useEnvironment();
	const setResourcesSettings = useSetResourcesSettings();
	return async (name: string, jdbcValues: Record<string, string>): Promise<OperationResult> =>
		fetchAndUpdate<string[]>(
			{
				endpoint: `${apiUrl}jdbc/save-properties`,
				options: { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ name, input: toJdbcRequestBody(jdbcValues) }) },
			},
			(files) => setResourcesSettings((current) => current.with({ jdbcFiles: files })),
		);
};
