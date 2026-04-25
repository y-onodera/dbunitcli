import { useEnvironment } from "../context/EnvironmentProvider";
import type {
	QueryDatasource,
	QueryDatasourceType,
} from "../model/QueryDatasource";
import {
	fetchData,
	getErrorMessage,
	handleFetchError,
	type OperationResult,
} from "../utils/fetchUtils";

export const useDeleteDataSource = (type: QueryDatasourceType) => {
	const environment = useEnvironment();
	return async (name: string) => {
		return postDataSource(environment.apiUrl, "query-datasource/delete", {
			type,
			name,
		});
	};
};
export const useSaveDataSource = () => {
	const environment = useEnvironment();
	return async (input: QueryDatasource) => {
		return postDataSource(environment.apiUrl, "query-datasource/save", input);
	};
};
export const useLoadDataSource = () => {
	const environment = useEnvironment();
	return async (name: string) => {
		return loadDataSource(environment.apiUrl, name);
	};
};
async function loadDataSource(apiUrl: string, name: string): Promise<string> {
	const fetchParams = {
		endpoint: `${apiUrl}query-datasource/load`,
		options: {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({ name }),
		},
	};
	return await fetchData(fetchParams)
		.then((response) => response.text())
		.catch((ex) => {
			handleFetchError(getErrorMessage(ex), fetchParams);
			return "";
		});
}
async function postDataSource(
	apiUrl: string,
	path: string,
	body: unknown,
): Promise<OperationResult> {
	const fetchParams = {
		endpoint: `${apiUrl}${path}`,
		options: {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(body),
		},
	};
	return await fetchData(fetchParams)
		.then(() => "success" as OperationResult)
		.catch((ex) => {
			handleFetchError(getErrorMessage(ex), fetchParams);
			return "failed" as OperationResult;
		});
}
