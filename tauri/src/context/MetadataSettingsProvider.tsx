import { type Dispatch, type ReactNode, type SetStateAction, createContext, useContext, useEffect, useState } from "react";
import { MetadataSettings, type MetadataSettingsBuilder } from "../model/MetadataSettings";
import { fetchData, handleFetchError } from "../utils/fetchUtils";
import { useEnviroment } from "./EnviromentProvider";

const metadataSettingsContext = createContext<MetadataSettings>(MetadataSettings.create());
const setMetadataSettingsContext = createContext<Dispatch<SetStateAction<MetadataSettings>>>(() => undefined);

export default function MetadataSettingsProvider(props: { children: ReactNode }) {
    const [settings, setSettings] = useState<MetadataSettings>(MetadataSettings.create());
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
        <metadataSettingsContext.Provider value={settings}>
            <setMetadataSettingsContext.Provider value={setSettings}>
                {props.children}
            </setMetadataSettingsContext.Provider>
        </metadataSettingsContext.Provider>
    );
}

export const useMetadataSettings = () => useContext(metadataSettingsContext);
export const useSetMetadataSettings = () => useContext(setMetadataSettingsContext);

export async function loadMetadataSettings(apiUrl: string, name: string): Promise<MetadataSettings> {
    if (name === "") {
        return MetadataSettings.create();
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
        .then((setting: MetadataSettingsBuilder) => MetadataSettings.build(setting))
        .catch((ex) => {
            handleFetchError(ex, fetchParams);
            return MetadataSettings.create();
        });
}

export async function saveMetadataSettings(apiUrl: string, name: string, input: MetadataSettings): Promise<string> {
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