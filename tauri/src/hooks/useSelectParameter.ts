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
import { fetchData, handleFetchError } from "../utils/fetchUtils";

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
		await fetchData(fetchParams)
			.then((response) => response.json())
			.then((parameter: Options) => {
				setParameter(new SelectParameter(parameter, command, name));
			})
			.catch((ex) => handleFetchError((ex as Error).message, fetchParams));
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
		await fetchData(fetchParams)
			.then((response) => response.json())
			.then((parameter: Options) => {
				setParameter(
					(current) =>
						new SelectParameter(parameter, current.command, current.name),
				);
			})
			.catch((ex) => handleFetchError((ex as Error).message, fetchParams));
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
	) => {
		const fetchParams = {
			endpoint: `${environment.apiUrl + parameter.command}/${action}`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ name: parameter.name, ...extraBody }),
			},
		};
		try {
			handleResult(await parseResponse(action, await fetchData(fetchParams)));
		} catch (ex) {
			handleFetchError((ex as Error).message, fetchParams);
			handleResult({ command: "", resultMessage: (ex as Error).message, resultDir: "" });
		}
	};
};

export const useSaveParameter = () => {
	const execute = useParameterAction();
	return async (
		input: { [k: string]: FormDataEntryValue },
		handleResult: (result: Running) => void,
	) => execute("save", { input }, handleResult);
};

export const useExecParameter = () => {
	const execute = useParameterAction();
	return async (
		input: { [k: string]: FormDataEntryValue },
		handleResult: (result: Running) => void,
	) => execute("exec", { input }, handleResult);
};

export const useSaveShell = () => {
	const execute = useParameterAction();
	return async (handleResult: (result: Running) => void) =>
		execute("shell", {}, handleResult);
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
		await fetchData(fetchParams)
			.then((response) => response.json())
			.then((parameter: ParameterizeOptions) => {
				setParameter(new SelectParameter(parameter, "parameterize", name));
				setParameterList((current) => {
					if (current.parameterize.includes(name)) {
						return current;
					}
					return current.replace("parameterize", [
						...current.parameterize,
						name,
					]);
				});
			})
			.catch((ex) => handleFetchError((ex as Error).message, fetchParams));
	};
};
