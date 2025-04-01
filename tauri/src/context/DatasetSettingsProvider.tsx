import { type Dispatch, type ReactNode, type SetStateAction, createContext, useContext, useEffect, useState } from "react";
import { DatasetSettings, type DatasetSettingsBuilder } from "../model/DatasetSettings";
import { fetchData, handleFetchError } from "../utils/fetchUtils";
import { useEnviroment } from "./EnviromentProvider";

const dataSettingsContext = createContext<DatasetSettings>(DatasetSettings.create());
const setDataSettingsContext = createContext<Dispatch<SetStateAction<DatasetSettings>>>(() => undefined);

export default function DatasetSettingsProvider(props: { children: ReactNode }) {
    const [settings, setSettings] = useState<DatasetSettings>(DatasetSettings.create());
    const environment = useEnviroment();

    useEffect(() => {
        const loadSettings = async () => {
            const fetchParams = {
                endpoint: `${environment.apiUrl}metadata/list`,
                options: {
                    method: "GET"
                },
            };
            await fetchData(fetchParams)
                .then((response) => response.json())
                .then((data) => setSettings(data))
                .catch((ex) => handleFetchError(ex, fetchParams));
        };
        loadSettings();
    }, [environment.apiUrl]);

    return (
        <dataSettingsContext.Provider value={settings}>
            <setDataSettingsContext.Provider value={setSettings}>
                {props.children}
            </setDataSettingsContext.Provider>
        </dataSettingsContext.Provider>
    );
}

export const useDatasetSettings = () => useContext(dataSettingsContext);
export const useSetDatasetSettings = () => useContext(setDataSettingsContext);

export async function loadDatasetSettings(apiUrl: string, name: string): Promise<DatasetSettings> {
    if (name === "") {
        return DatasetSettings.create();
    }
    const fetchParams = {
        endpoint: `${apiUrl}metadata/load`,
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

export async function saveDatasetSettings(apiUrl: string, name: string, input: DatasetSettings): Promise<string> {
    const fetchParams = {
        endpoint: `${apiUrl}metadata/save`,
        options: {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name, input }),
        },
    };
    return await fetchData(fetchParams)
        .then((response) => response.text())
        .catch((ex) => {
            handleFetchError(ex, fetchParams);
            return "failed";
        });
}

export async function deleteDatasetSettings(apiUrl: string, name: string): Promise<string> {
    const fetchParams = {
        endpoint: `${apiUrl}metadata/delete`,
        options: {
            method: "POST",
            headers: { "Content-Type": "text/plain" },
            body: name,
        },
    };
    return await fetchData(fetchParams)
        .then((response) => response.text())
        .catch((ex) => {
            handleFetchError(ex, fetchParams);
            return "failed";
        });
}