import type { Dispatch, SetStateAction } from "react";
import { DatasetSettings, type DatasetSettingsBuilder } from "../model/DatasetSettings";
import type { ResourcesSettings } from "../model/WorkspaceResources";
import { fetchData, handleFetchError } from "../utils/fetchUtils";
import { useEnviroment } from "./EnviromentProvider";
import { useSetResourcesSettings } from "./WorkspaceResourcesProvider";

type OperationResult = 'success' | 'failed';

// コンポーネントでの使用のためのラップ関数
export function useDeleteDatasetSettings() {
    const environment = useEnviroment();
    const setResourcesSettings = useSetResourcesSettings();
    return async (name: string) => {
        return deleteDatasetSettings(environment.apiUrl, name, setResourcesSettings);
    };
}

export function useSaveDatasetSettings() {
    const environment = useEnviroment();
    const setResourcesSettings = useSetResourcesSettings();
    return async (name: string, input: DatasetSettings) => {
        return saveDatasetSettings(environment.apiUrl, name, input, setResourcesSettings);
    };
}

export function useLoadDatasetSettings() {
    const environment = useEnviroment();
    return async (name: string) => {
        return loadDatasetSettings(environment.apiUrl, name);
    };
}

async function loadDatasetSettings(apiUrl: string, name: string): Promise<DatasetSettings> {
    if (name === "") {
        return DatasetSettings.create();
    }
    const fetchParams = {
        endpoint: `${apiUrl}dataset-setting/load`,
        options: {
            method: "POST",
            headers: { "Content-Type": "text/plain" },
            body: name,
        },
    };
    return await fetchData(fetchParams)
        .then((response) => response.json())
        .then((setting: DatasetSettingsBuilder) => DatasetSettings.build(setting))
        .catch((ex) => {
            handleFetchError(ex, fetchParams);
            return DatasetSettings.create();
        });
}

async function saveDatasetSettings(
    apiUrl: string,
    name: string,
    input: DatasetSettings,
    setResourcesSettings: Dispatch<SetStateAction<ResourcesSettings>>
): Promise<OperationResult> {
    const fetchParams = {
        endpoint: `${apiUrl}dataset-setting/save`,
        options: {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name, input }),
        },
    };

    return await fetchData(fetchParams)
        .then((response) => response.json())
        .then((settings: string[]) => {
            setResourcesSettings(current => ({
                ...current,
                datasetSettings: settings
            }));
            return 'success' as OperationResult;
        })
        .catch((ex) => {
            handleFetchError(ex, fetchParams);
            return 'failed' as OperationResult;
        });
}

async function deleteDatasetSettings(
    apiUrl: string,
    name: string,
    setResourcesSettings: Dispatch<SetStateAction<ResourcesSettings>>
): Promise<OperationResult> {
    const fetchParams = {
        endpoint: `${apiUrl}dataset-setting/delete`,
        options: {
            method: "POST",
            headers: { "Content-Type": "text/plain" },
            body: name,
        },
    };

    return await fetchData(fetchParams)
        .then((response) => response.json())
        .then((settings: string[]) => {
            setResourcesSettings(current => ({
                ...current,
                datasetSettings: settings
            }));
            return 'success' as OperationResult;
        })
        .catch((ex) => {
            handleFetchError(ex, fetchParams);
            return 'failed' as OperationResult;
        });
}