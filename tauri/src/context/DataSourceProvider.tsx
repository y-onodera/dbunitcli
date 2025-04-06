import { type Dispatch, type ReactNode, type SetStateAction, createContext, useContext, useState } from "react";
import type { QueryDatasource, QueryDatasourceType } from "../model/QueryDatasource";
import { fetchData, handleFetchError } from "../utils/fetchUtils";

const dataSourceContext = createContext<QueryDatasource | null>(null);
const setDataSourceContext = createContext<Dispatch<SetStateAction<QueryDatasource | null>>>(() => undefined);

export default function DataSourceProvider(props: { children: ReactNode }) {
    const [dataSource, setDataSource] = useState<QueryDatasource | null>(null);

    return (
        <dataSourceContext.Provider value={dataSource}>
            <setDataSourceContext.Provider value={setDataSource}>
                {props.children}
            </setDataSourceContext.Provider>
        </dataSourceContext.Provider>
    );
}

export const useDataSource = () => useContext(dataSourceContext);
export const useSetDataSource = () => useContext(setDataSourceContext);

export async function fetchDatasources(apiUrl: string, type: QueryDatasourceType): Promise<string[] | 'failed'> {
    const fetchParams = {
        endpoint: `${apiUrl}query-datasource/list?type=${type}`,
        options: {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' },
        },
    };

    return await fetchData(fetchParams)
        .then((response) => response.json())
        .catch((ex) => {
            handleFetchError(ex, fetchParams);
            return 'failed';
        });
}

export async function loadDataSource(apiUrl: string, type: QueryDatasourceType, name: string): Promise<string> {
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

export async function deleteDataSource(apiUrl: string, type: QueryDatasourceType, name: string): Promise<string> {
    const fetchParams = {
        endpoint: `${apiUrl}query-datasource/delete`,
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
            return 'failed';
        });
}

export async function saveDataSource(apiUrl: string, input: QueryDatasource): Promise<string> {
    const fetchParams = {
        endpoint: `${apiUrl}query-datasource/save`,
        options: {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(input),
        },
    };

    return await fetchData(fetchParams)
        .then((response) => response.text())
        .catch((ex) => {
            handleFetchError(ex, fetchParams);
            return "failed";
        });
}