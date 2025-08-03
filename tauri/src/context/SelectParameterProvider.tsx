import { type Dispatch, type ReactNode, type SetStateAction, createContext, use, useState } from "react";
import { type Parameter, SelectParameter } from "../model/CommandParam";
import { fetchData, handleFetchError } from "../utils/fetchUtils";
import { useEnviroment } from "./EnviromentProvider";

const selectParameterContext = createContext<SelectParameter>({} as SelectParameter);
const setSelectParameterContext = createContext<Dispatch<SetStateAction<SelectParameter>>>(() => undefined);
export default function SelectParameterProvider(props: { children: ReactNode }) {
    const [parameter, setParameter] = useState<SelectParameter>({} as SelectParameter);
    return (
        <selectParameterContext.Provider value={parameter}>
            <setSelectParameterContext.Provider value={setParameter}>
                {props.children}
            </setSelectParameterContext.Provider>
        </selectParameterContext.Provider>
    );
}
export const useSelectParameter = () => use(selectParameterContext);
export const useSetSelectParameter = () => {
    const setParameter = use(setSelectParameterContext);
    return (response: Parameter, command: string, name: string) => {
        setParameter(new SelectParameter(response, command, name));
    }
};
export const useLoadSelectParameter = () => {
    const setParameter = use(setSelectParameterContext);
    const environment = useEnviroment();
    return async (command: string, name: string) => {
        const fetchParams = {
            endpoint: `${environment.apiUrl + command.toLowerCase()}/load`,
            options: {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ name }),
            },
        };
        await fetchData(fetchParams)
            .then((response) => response.json())
            .then((parameter: Parameter) => {
                setParameter(new SelectParameter(parameter, command, name));
            })
            .catch((ex) => handleFetchError(ex, fetchParams));
    };
};
export const useRefreshSelectParameter = (command: string) => {
    const setParameter = use(setSelectParameterContext);
    const environment = useEnviroment();
    return async (values: { [k: string]: FormDataEntryValue }) => {
        const fetchParams = {
            endpoint: `${environment.apiUrl + command.toLowerCase()}/refresh`,
            options: {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(values),
            },
        };
        await fetchData(fetchParams)
            .then((response) => response.json())
            .then((parameter: Parameter) => {
                setParameter(current => new SelectParameter(parameter, current.command, current.name));
            })
            .catch((ex) => handleFetchError(ex, fetchParams));
    };
};
export type Running = {
    command: string;
    resultMessage: string;
    resultDir: string;
};
export const useSaveParameter = () => {
    const parameter = useSelectParameter();
    const environment = useEnviroment();
    return async (input: { [k: string]: FormDataEntryValue }, handleResult: (result: Running) => void) => {
        const fetchParams = {
            endpoint: `${environment.apiUrl + parameter.command}/save`,
            options: {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ name: parameter.name, input }),
            },
        };
        await fetchData(fetchParams)
            .then(() => {
                handleResult({
                    command: "",
                    resultMessage: "Save Success",
                    resultDir: "",
                });
            })
            .catch((ex) => {
                handleFetchError(ex, fetchParams);
                handleResult({ command: "", resultMessage: ex.message, resultDir: "" });
            });
    }
}
export const useExecParameter = () => {
    const parameter = useSelectParameter();
    const environment = useEnviroment();
    return async (input: { [k: string]: FormDataEntryValue }, handleResult: (result: Running) => void) => {
        const fetchParams = {
            endpoint: `${environment.apiUrl + parameter.command}/exec`,
            options: {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ name: parameter.name, input }),
            },
        };
        await fetchData(fetchParams)
            .then((response) => response.text())
            .then((resultDir: string) => handleResult({
                command: "",
                resultMessage: "Execution Success",
                resultDir,
            }))
            .catch((ex) => {
                handleFetchError(ex, fetchParams);
                handleResult({ command: "", resultMessage: ex.message, resultDir: "" });
            });
    }
}
