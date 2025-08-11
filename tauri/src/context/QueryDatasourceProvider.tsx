import type { Dispatch, SetStateAction } from "react";
import type { QueryDatasource, QueryDatasourceType } from "../model/QueryDatasource";
import type { ResourcesSettings } from "../model/WorkspaceResources";
import { fetchData, handleFetchError } from "../utils/fetchUtils";
import { useEnviroment } from "./EnviromentProvider";
import { useSetResourcesSettings } from "./WorkspaceResourcesProvider";

type QueryDatasourceResult = 'success' | 'failed';

export function useDeleteDataSource(type: QueryDatasourceType) {
    const environment = useEnviroment();
    const setResourcesSettings = useSetResourcesSettings();
    return async (name: string) => {
        return deleteDataSource(environment.apiUrl, type, name, setResourcesSettings);
    };
}
export function useSaveDataSource() {
    const environment = useEnviroment();
    const setResourcesSettings = useSetResourcesSettings();
    return async (input: QueryDatasource) => {
        return saveDataSource(environment.apiUrl, input, setResourcesSettings);
    };
}
export function useLoadDataSource() {
    const environment = useEnviroment();
    return async (type: QueryDatasourceType, name: string) => {
        return loadDataSource(environment.apiUrl, type, name);
    };
}
async function loadDataSource(apiUrl: string, type: QueryDatasourceType, name: string): Promise<string> {
    const fetchParams = {
        endpoint: `${apiUrl}query-datasource/load`,
        options: {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ type, name }),
        },
    };
    return await fetchData(fetchParams)
        .then((response) => response.text())
        .catch((ex) => {
            handleFetchError(ex, fetchParams);
            return '';
        });
}
async function deleteDataSource(
    apiUrl: string,
    type: QueryDatasourceType,
    name: string,
    setResourcesSettings: Dispatch<SetStateAction<ResourcesSettings>>
): Promise<QueryDatasourceResult> {
    const fetchParams = {
        endpoint: `${apiUrl}query-datasource/delete`,
        options: {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ type, name }),
        },
    };
    return await fetchData(fetchParams)
        .then((response) => response.json())
        .then((files: string[]) => {
            setResourcesSettings(current => current.with({ queryFiles: current.queryFiles.replace(type, files) }));
            return 'success' as QueryDatasourceResult;
        })
        .catch((ex) => {
            handleFetchError(ex, fetchParams);
            return 'failed' as QueryDatasourceResult;
        });
}
async function saveDataSource(
    apiUrl: string,
    input: QueryDatasource,
    setResourcesSettings: Dispatch<SetStateAction<ResourcesSettings>>
): Promise<QueryDatasourceResult> {
    const fetchParams = {
        endpoint: `${apiUrl}query-datasource/save`,
        options: {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(input),
        },
    };
    return await fetchData(fetchParams)
        .then((response) => response.json())
        .then((files: string[]) => {
            setResourcesSettings(current => current.with({ queryFiles: current.queryFiles.replace(input.type, files) }));
            return 'success' as QueryDatasourceResult;
        })
        .catch((ex) => {
            handleFetchError(ex, fetchParams);
            return 'failed' as QueryDatasourceResult;
        });
}