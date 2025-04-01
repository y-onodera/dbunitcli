import { type Dispatch, type ReactNode, type SetStateAction, createContext, useContext, useEffect, useState } from "react";
import { XlsxSchema, type XlsxSchemaBuilder } from "../model/XlsxSchema";
import { fetchData, handleFetchError } from "../utils/fetchUtils";
import { useEnviroment } from "./EnviromentProvider";

const xlsxSchemaContext = createContext<XlsxSchema>(XlsxSchema.create());
const setXlsxSchemaContext = createContext<Dispatch<SetStateAction<XlsxSchema>>>(() => undefined);

export default function XlsxSchemaProvider(props: { children: ReactNode }) {
    const [schema, setSchema] = useState<XlsxSchema>(XlsxSchema.create());
    const environment = useEnviroment();

    useEffect(() => {
        const loadSchema = async () => {
            const fetchParams = {
                endpoint: `${environment.apiUrl}xlsx-schema/list`,
                options: {
                    method: "GET"
                },
            };
            await fetchData(fetchParams)
                .then((response) => response.json())
                .then((data) => setSchema(data))
                .catch((ex) => handleFetchError(ex, fetchParams));
        };
        loadSchema();
    }, [environment.apiUrl]);

    return (
        <xlsxSchemaContext.Provider value={schema}>
            <setXlsxSchemaContext.Provider value={setSchema}>
                {props.children}
            </setXlsxSchemaContext.Provider>
        </xlsxSchemaContext.Provider>
    );
}

export const useXlsxSchema = () => useContext(xlsxSchemaContext);
export const useSetXlsxSchema = () => useContext(setXlsxSchemaContext);

export async function loadXlsxSchema(apiUrl: string, name: string): Promise<XlsxSchema> {
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

export async function saveXlsxSchema(apiUrl: string, name: string, input: XlsxSchema): Promise<string> {
    const fetchParams = {
        endpoint: `${apiUrl}xlsx-schema/save`,
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

export async function deleteXlsxSchema(apiUrl: string, name: string): Promise<string> {
    const fetchParams = {
        endpoint: `${apiUrl}xlsx-schema/delete`,
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