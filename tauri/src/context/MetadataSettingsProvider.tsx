import { fetch } from "@tauri-apps/plugin-http";
import { type Dispatch, type ReactNode, type SetStateAction, createContext, useContext, useState } from "react";
import { MetadataSettings, type MetadataSettingsBuilder } from "../model/MetadataSettings";

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
    return await fetch(`${apiUrl}metadata/load`, {
        method: "POST",
        headers: { "Content-Type": "text/plain" },
        body: name,
    }).then((response) => {
        if (!response.ok) {
            console.error("response.ok:", response.ok);
            console.error("esponse.status:", response.status);
            throw new Error(response.statusText);
        }
        return response.json().then((setteng: MetadataSettingsBuilder) => MetadataSettings.build(setteng));
    });
}
export async function saveMetadataSettings(apiUrl: string, name: string, input: MetadataSettings): Promise<string> {
    return await fetch(`${apiUrl}metadata/save`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, input }),
    }).then((response) => {
        if (!response.ok) {
            console.error("response.ok:", response.ok);
            console.error("esponse.status:", response.status);
            throw new Error(response.statusText);
        }
        return response.text().then((text) => text);
    });
}