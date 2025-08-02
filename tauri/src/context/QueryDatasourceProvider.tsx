import { type Dispatch, type ReactNode, type SetStateAction, Suspense, createContext, use, useState } from "react";
import { type QueryDatasource, type QueryDatasourceType, isSqlRelatedType } from "../model/QueryDatasource";
import { fetchData, handleFetchError } from "../utils/fetchUtils";
import { useEnviroment } from "./EnviromentProvider";

type QueryDatasourceResult = 'success' | 'failed';

const queryDatasourceContext = createContext<string[]>([]);
const setQueryDatasourceContext = createContext<Dispatch<SetStateAction<string[]>>>(() => undefined);

export const useQueryDatasource = () => use(queryDatasourceContext);
export const useSetQueryDatasource = () => use(setQueryDatasourceContext);
export function useDeleteDataSource(type: QueryDatasourceType) {
    const environment = useEnviroment();
    const setDatasources = useSetQueryDatasource();
    return async (name: string) => {
        return deleteDataSource(environment.apiUrl, type, name, setDatasources);
    };
}
export function useSaveDataSource() {
    const environment = useEnviroment();
    const setDatasources = useSetQueryDatasource();
    return async (input: QueryDatasource) => {
        return saveDataSource(environment.apiUrl, input, setDatasources);
    };
}
export function useLoadDataSource() {
    const environment = useEnviroment();
    return async (type: QueryDatasourceType, name: string) => {
        return loadDataSource(environment.apiUrl, type, name);
    };
}
export default function QueryDatasourceProvider(props: { type: string, children: ReactNode }) {
    const environment = useEnviroment();
    const srcType = props.type;
    const loadDatasources = async () => {
        if (isSqlRelatedType(srcType ?? "")) {
            return fetchDatasources(environment.apiUrl, srcType as QueryDatasourceType);
        }
        return new Promise<string[]>(resolve => resolve([]));
    };
    return (
        <Suspense fallback={<div>Loading...</div>}>
            <CreateContext promise={loadDatasources()}>
                {props.children}
            </CreateContext>
        </Suspense>
    );
}
function CreateContext(props: { promise: Promise<string[]>, children: ReactNode }) {
    const source = use(props.promise);
    const [datasources, setDatasources] = useState<string[]>(source);
    return (
        <queryDatasourceContext.Provider value={datasources}>
            <setQueryDatasourceContext.Provider value={setDatasources}>
                {props.children}
            </setQueryDatasourceContext.Provider>
        </queryDatasourceContext.Provider>
    );
}
async function fetchDatasources(apiUrl: string, type: QueryDatasourceType): Promise<string[]> {
    const fetchParams = {
        endpoint: `${apiUrl}query-datasource/list?type=${type}`,
        options: {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' },
        },
    };
    return await fetchData(fetchParams)
        .then((response) => response.json())
        .catch((ex) => handleFetchError(ex, fetchParams));
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
    setDatasources: (sources: string[]) => void
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
        .then((sources: string[]) => {
            setDatasources(sources);
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
    setDatasources: (sources: string[]) => void
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
        .then((sources: string[]) => {
            setDatasources(sources);
            return 'success' as QueryDatasourceResult;
        })
        .catch((ex) => {
            handleFetchError(ex, fetchParams);
            return 'failed' as QueryDatasourceResult;
        });
}