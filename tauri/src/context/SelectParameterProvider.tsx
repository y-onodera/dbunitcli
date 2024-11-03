import { Body, ResponseType, fetch } from "@tauri-apps/api/http";
import { type Dispatch, type ReactNode, type SetStateAction, createContext, useContext, useState } from "react";
import { type Parameter, SelectParameter } from "../model/CommandParam";
import { useEnviroment } from "./EnviromentProvider";
const selectParameterContext = createContext<SelectParameter>(
    {} as SelectParameter,
);
const setSelectParameterContext = createContext<
    Dispatch<SetStateAction<SelectParameter>>
>(() => undefined);
export default function SelectParameterProvider(props: {
    children: ReactNode;
}) {
    const [parameter, setParameter] = useState<SelectParameter>(
        {} as SelectParameter,
    );
    return (
        <selectParameterContext.Provider value={parameter}>
            <setSelectParameterContext.Provider value={setParameter}>
                {props.children}
            </setSelectParameterContext.Provider>
        </selectParameterContext.Provider>
    );
}
export const useSelectParameter = () => useContext(selectParameterContext);
export const useSetSelectParameter = () => {
    const setParameter = useContext(setSelectParameterContext);
    return (response: Parameter, command: string, name: string) => {
        setParameter(new SelectParameter(response, command, name));
    };
};
export const useLoadSelectParameter = () => {
    const setParameter = useContext(setSelectParameterContext);
    const environment = useEnviroment();
    return async (command: string, name: string) => {
        await fetch(`${environment.apiUrl + command.toLowerCase()}/load`, {
            method: "POST",
            responseType: ResponseType.JSON,
            headers: { "Content-Type": "application/json" },
            body: Body.json({ name }),
        })
            .then((response) => {
                if (!response.ok) {
                    console.error("response.ok:", response.ok);
                    console.error("esponse.status:", response.status);
                    throw new Error(response.data as string);
                }
                setParameter(new SelectParameter(response.data as Parameter, command, name));
            })
            .catch((ex) => alert(ex));
    }
}
export const useRefreshSelectParameter = (command: string) => {
    const setParameter = useContext(setSelectParameterContext);
    const environment = useEnviroment();
    return async (values: { [k: string]: FormDataEntryValue }) => {
        await fetch(`${environment.apiUrl + command.toLowerCase()}/refresh`, {
            method: "POST",
            responseType: ResponseType.JSON,
            headers: { "Content-Type": "application/json" },
            body: Body.json(values),
        })
            .then((response) =>
                setParameter(current => new SelectParameter(response.data as Parameter, current.command, current.name))
            )
            .catch((ex) => alert(ex));
    }
}
export type Running = {
    command: string;
    resultMessage: string;
    resultDir: string;
};
export const saveParameter = async (
    command: string
    , name: string
    , input: { [k: string]: FormDataEntryValue; }
    , handleResult: (result: Running) => void
) => {
    const environment = useEnviroment();
    await fetch(`${environment.apiUrl + command}/save`, {
        method: "POST",
        responseType: ResponseType.Text,
        headers: { "Content-Type": "application/json" },
        body: Body.json({ name, input }),
    })
        .then((response) => {
            if (!response.ok) {
                console.error("response.ok:", response.ok);
                console.error("esponse.status:", response.status);
                throw new Error(response.data as string);
            }
            handleResult({
                command: "",
                resultMessage: "Save Success",
                resultDir: "",
            });
        })
        .catch((ex) => {
            handleResult({ command: "", resultMessage: ex.message, resultDir: "" });
        });
}
export const execParameter = async (
    command: string
    , name: string
    , input: { [k: string]: FormDataEntryValue; }
    , handleResult: (result: Running) => void
) => {
    const environment = useEnviroment();
    await fetch(`${environment.apiUrl + command}/exec`, {
        method: "POST",
        responseType: ResponseType.Text,
        headers: { "Content-Type": "application/json" },
        body: Body.json({ name, input }),
    })
        .then((response) => {
            if (!response.ok) {
                console.error("esponse.status:", response.status);
                throw new Error(response.data as string);
            }
            handleResult({
                command: "",
                resultMessage: "Execution Success",
                resultDir: response.data as string,
            });
        })
        .catch((ex) => {
            handleResult({ command: "", resultMessage: ex.message, resultDir: "" });
        });
}