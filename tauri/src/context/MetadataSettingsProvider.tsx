import { type Dispatch, type ReactNode, type SetStateAction, createContext, useContext, useState } from "react";
import { MetadataSettings, type MetadataSettingsBuilder } from "../model/MetadataSettings";
import { fetchData, handleFetchError } from "../utils/fetchUtils";

const metadataSettingsContext = createContext<MetadataSettings>(MetadataSettings.create());
const setMetadataSettingsContext = createContext<Dispatch<SetStateAction<MetadataSettings>>>(() => undefined);
export default function MetadataSettingsProvider(props: { children: ReactNode }) {
    const [settings, setSettings] = useState<MetadataSettings>(
        MetadataSettings.create()
    );
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
    const endpoint = `${apiUrl}metadata/load`;
    const requestBody = name;
    return await fetchData(endpoint, {
        method: "POST",
        headers: { "Content-Type": "text/plain" },
        body: requestBody,
    })
        .then((response) => response.json())
        .then((setting: MetadataSettingsBuilder) => MetadataSettings.build(setting))
        .catch((ex) => {
            handleFetchError(ex);
            return MetadataSettings.create();
        });
}
export async function saveMetadataSettings(apiUrl: string, name: string, input: MetadataSettings): Promise<string> {
    const endpoint = `${apiUrl}metadata/save`;
    const requestBody = { name, input };
    return await fetchData(endpoint, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestBody),
    })
        .then((response) => response.text())
        .catch((ex) => {
            handleFetchError(ex);
            return "failed";
        });
}