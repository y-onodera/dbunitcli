import { useEnviroment } from "../context/EnviromentProvider";
import {
	useSelectParameter,
	useSetSelectParameterState,
} from "../context/SelectParameterProvider";
import { useSetParameterList } from "../context/WorkspaceResourcesProvider";
import type {
	Command,
	Options,
	ParameterizeOptions,
} from "../model/SelectParameter";
import { SelectParameter } from "../model/SelectParameter";
import { fetchData, getErrorMessage, handleFetchError, isAbortError } from "../utils/fetchUtils";

export type Running = {
	command: string;
	resultMessage: string;
	resultDir: string;
};

export const useLoadSelectParameter = () => {
	const setParameter = useSetSelectParameterState();
	const environment = useEnviroment();
	return async (command: Command, name: string) => {
		const fetchParams = {
			endpoint: `${environment.apiUrl + command.toLowerCase()}/load`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ name }),
			},
		};
		try {
			const response = await fetchData(fetchParams);
			setParameter(new SelectParameter(await response.json(), command, name));
		} catch (ex) {
			handleFetchError(getErrorMessage(ex), fetchParams);
		}
	};
};

export const useRefreshSelectParameter = (command: string) => {
	const setParameter = useSetSelectParameterState();
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
		try {
			const response = await fetchData(fetchParams);
			const parameter: Options = await response.json();
			setParameter(
				(current) => new SelectParameter(parameter, current.command, current.name),
			);
		} catch (ex) {
			handleFetchError(getErrorMessage(ex), fetchParams);
		}
	};
};

type ParameterAction = "save" | "exec" | "shell";

const parseResponse = async (
	action: ParameterAction,
	response: Response,
): Promise<Running> => {
	if (action === "save") {
		return { command: "", resultMessage: "Save Success", resultDir: "" };
	}
	const resultDir = await response.text();
	return {
		command: "",
		resultMessage: action === "exec" ? "Execution Success" : "Save Shell Success",
		resultDir,
	};
};

const useParameterAction = () => {
	const parameter = useSelectParameter();
	const environment = useEnviroment();
	return async (
		action: ParameterAction,
		extraBody: Record<string, unknown>,
		handleResult: (result: Running) => void,
		signal?: AbortSignal,
	) => {
		const fetchParams = {
			endpoint: `${environment.apiUrl + parameter.command}/${action}`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ name: parameter.name, ...extraBody }),
			},
			signal,
		};
		try {
			const response = await fetchData(fetchParams);
			handleResult(await parseResponse(action, response));
		} catch (ex) {
			if (isAbortError(ex)) {
				return;
			}
			const errorMessage = getErrorMessage(ex);
			handleFetchError(errorMessage, fetchParams);
			handleResult({ command: "", resultMessage: errorMessage, resultDir: "" });
		}
	};
};

export const useSaveParameter = () => {
	const execute = useParameterAction();
	return async (
		input: { [k: string]: FormDataEntryValue },
		handleResult: (result: Running) => void,
		signal?: AbortSignal,
	) => execute("save", { input }, handleResult, signal);
};

export const useExecParameter = () => {
	const execute = useParameterAction();
	return async (
		input: { [k: string]: FormDataEntryValue },
		handleResult: (result: Running) => void,
		signal?: AbortSignal,
	) => execute("exec", { input }, handleResult, signal);
};

export const useSaveShell = () => {
	const execute = useParameterAction();
	return async (handleResult: (result: Running) => void, signal?: AbortSignal) =>
		execute("shell", {}, handleResult, signal);
};

export const useParameterizeFrom = () => {
	const setParameter = useSetSelectParameterState();
	const setParameterList = useSetParameterList();
	const environment = useEnviroment();
	return async (sourceCommand: string, name: string) => {
		const fetchParams = {
			endpoint: `${environment.apiUrl + sourceCommand.toLowerCase()}/parameterize`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ name }),
			},
		};
		try {
			const response = await fetchData(fetchParams);
			const parameter: ParameterizeOptions = await response.json();
			setParameter(new SelectParameter(parameter, "parameterize", name));
			setParameterList((current) => {
				if (current.parameterize.includes(name)) {
					return current;
				}
				return current.replace("parameterize", [...current.parameterize, name]);
			});
		} catch (ex) {
			handleFetchError(getErrorMessage(ex), fetchParams);
		}
	};
};
