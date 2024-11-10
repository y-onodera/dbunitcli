import { Body, ResponseType, fetch } from "@tauri-apps/api/http";
import { type Dispatch, type ReactNode, type SetStateAction, createContext, useContext, useState } from "react";
import { MetadataSettings, type MetadataSettingsBuilder } from "../model/MetadataSettings";

const metadataSettingsContext = createContext<MetadataSettings>({} as MetadataSettings);
const setMetadataSettingsContext = createContext<Dispatch<SetStateAction<MetadataSettings>>>(() => undefined);
export default function MetadataSettingsProvider(props: { children: ReactNode }) {
    const [settings, setSettings] = useState<MetadataSettings>(
        {} as MetadataSettings,
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
    return await fetch(`${apiUrl}metadata/load`, {
        method: "POST",
        responseType: ResponseType.JSON,
        headers: { "Content-Type": "text/plain" },
        body: Body.text(name),
    }).then((response) => {
        if (!response.ok) {
            console.error("response.ok:", response.ok);
            console.error("esponse.status:", response.status);
            throw new Error(response.data as string);
        }
        return MetadataSettings.build(response.data as MetadataSettingsBuilder);
    });
}
export async function saveMetadataSettings(apiUrl: string, name: string, input: MetadataSettings): Promise<string> {
    return await fetch(`${apiUrl}metadata/save`, {
        method: "POST",
        responseType: ResponseType.Text,
        headers: { "Content-Type": "application/json" },
        body: Body.json({ name, input }),
    }).then((response) => {
        if (!response.ok) {
            console.error("response.ok:", response.ok);
            console.error("esponse.status:", response.status);
            throw new Error(response.data as string);
        }
        return response.data as string;
    });
}