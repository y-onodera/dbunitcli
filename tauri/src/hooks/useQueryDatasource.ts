import { useEnviroment } from "../context/EnviromentProvider";
import type {
	QueryDatasource,
	QueryDatasourceType,
} from "../model/QueryDatasource";
import {
	fetchData,
	handleFetchError,
	type OperationResult,
} from "../utils/fetchUtils";

export const useDeleteDataSource = (type: QueryDatasourceType) => {
	const environment = useEnviroment();
	return async (name: string) => {
		return postDataSource(environment.apiUrl, "query-datasource/delete", {
			type,
			name,
		});
	};
};
export const useSaveDataSource = () => {
	const environment = useEnviroment();
	return async (input: QueryDatasource) => {
		return postDataSource(environment.apiUrl, "query-datasource/save", input);
	};
};
export const useLoadDataSource = () => {
	const environment = useEnviroment();
	return async (type: QueryDatasourceType, name: string) => {
		return loadDataSource(environment.apiUrl, type, name);
	};
};
async function loadDataSource(
	apiUrl: string,
	type: QueryDatasourceType,
	name: string,
): Promise<string> {
	const fetchParams = {
		endpoint: `${apiUrl}query-datasource/load`,
		options: {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({ type, name }),
		},
	};
	return await fetchData(fetchParams)
		.then((response) => response.text())
		.catch((ex) => {
			handleFetchError((ex as Error).message, fetchParams);
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
			handleFetchError((ex as Error).message, fetchParams);
			return "failed" as OperationResult;
		});
}
