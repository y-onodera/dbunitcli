import type { Dispatch, SetStateAction } from "react";
import type { ResourcesSettings } from "../model/WorkspaceResources";
import { XlsxSchema, type XlsxSchemaBuilder } from "../model/XlsxSchema";
import { fetchData, handleFetchError } from "../utils/fetchUtils";
import { useEnviroment } from "./EnviromentProvider";
import { useSetResourcesSettings } from "./WorkspaceResourcesProvider";

type OperationResult = 'success' | 'failed';

// コンポーネントでの使用のためのラップ関数
export function useDeleteXlsxSchema() {
    const environment = useEnviroment();
    const setResourcesSettings = useSetResourcesSettings();
    return async (name: string) => {
        return deleteXlsxSchema(environment.apiUrl, name, setResourcesSettings);
    };
}

export function useSaveXlsxSchema() {
    const environment = useEnviroment();
    const setResourcesSettings = useSetResourcesSettings();
    return async (name: string, input: XlsxSchema) => {
        return saveXlsxSchema(environment.apiUrl, name, input, setResourcesSettings);
    };
}

export function useLoadXlsxSchema() {
    const environment = useEnviroment();
    return async (name: string) => {
        return loadXlsxSchema(environment.apiUrl, name);
    };
}

async function loadXlsxSchema(apiUrl: string, name: string): Promise<XlsxSchema> {
    if (name === "") {
        return XlsxSchema.create();
    }
    const fetchParams = {
        endpoint: `${apiUrl}xlsx-schema/load`,
        options: {
            method: "POST",
            headers: { "Content-Type": "text/plain" },
            body: name,
        },
    };
    return await fetchData(fetchParams)
        .then((response) => response.json())
        .then((schema: XlsxSchemaBuilder) => XlsxSchema.build(schema))
        .catch((ex) => {
            handleFetchError(ex, fetchParams);
            return XlsxSchema.create();
        });
}

async function saveXlsxSchema(
    apiUrl: string,
    name: string,
    input: XlsxSchema,
    setResourcesSettings: Dispatch<SetStateAction<ResourcesSettings>>
): Promise<OperationResult> {
    const fetchParams = {
        endpoint: `${apiUrl}xlsx-schema/save`,
        options: {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name, input }),
        },
    };

    return await fetchData(fetchParams)
        .then((response) => response.json())
        .then((schemas: string[]) => {
            setResourcesSettings(current => ({
                ...current,
                xlsxSchemas: schemas
            }));
            return 'success' as OperationResult;
        })
        .catch((ex) => {
            handleFetchError(ex, fetchParams);
            return 'failed' as OperationResult;
        });
}

async function deleteXlsxSchema(
    apiUrl: string,
    name: string,
    setResourcesSettings: Dispatch<SetStateAction<ResourcesSettings>>
): Promise<OperationResult> {
    const fetchParams = {
        endpoint: `${apiUrl}xlsx-schema/delete`,
        options: {
            method: "POST",
            headers: { "Content-Type": "text/plain" },
            body: name,
        },
    };

    return await fetchData(fetchParams)
        .then((response) => response.json())
        .then((schemas: string[]) => {
            setResourcesSettings(current => ({
                ...current,
                xlsxSchemas: schemas
            }));
            return 'success' as OperationResult;
        })
        .catch((ex) => {
            handleFetchError(ex, fetchParams);
            return 'failed' as OperationResult;
        });
}