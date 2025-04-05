import { type Dispatch, type ReactNode, type SetStateAction, createContext, useContext, useState } from "react";
import type { DataSource } from "../model/DataSource";
import { fetchData, handleFetchError } from "../utils/fetchUtils";

const dataSourceContext = createContext<DataSource | null>(null);
const setDataSourceContext = createContext<Dispatch<SetStateAction<DataSource | null>>>(() => undefined);

export default function DataSourceProvider(props: { children: ReactNode }) {
    const [dataSource, setDataSource] = useState<DataSource | null>(null);

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

export async function saveDataSource(apiUrl: string, input: DataSource): Promise<string> {
    const fetchParams = {
        endpoint: `${apiUrl}datasource/save`,
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