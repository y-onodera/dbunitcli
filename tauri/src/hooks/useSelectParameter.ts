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

const useParameterAction = () => {
	const parameter = useSelectParameter();
	const environment = useEnviroment();
	return async (
		action: string,
		extraBody: Record<string, unknown>,
		toResult: (response: Response) => Promise<Running>,
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
		await fetchData(fetchParams)
			.then(toResult)
			.then(handleResult)
			.catch((ex) => {
				handleFetchError((ex as Error).message, fetchParams);
				handleResult({ command: "", resultMessage: ex.message, resultDir: "" });
			});
	};
};

export const useSaveParameter = () => {
	const execute = useParameterAction();
	return async (
		input: { [k: string]: FormDataEntryValue },
		handleResult: (result: Running) => void,
	) =>
		execute(
			"save",
			{ input },
			async () => ({ command: "", resultMessage: "Save Success", resultDir: "" }),
			handleResult,
		);
};

export const useExecParameter = () => {
	const execute = useParameterAction();
	return async (
		input: { [k: string]: FormDataEntryValue },
		handleResult: (result: Running) => void,
	) =>
		execute(
			"exec",
			{ input },
			(response) =>
				response
					.text()
					.then((resultDir) => ({
						command: "",
						resultMessage: "Execution Success",
						resultDir,
					})),
			handleResult,
		);
};

export const useSaveShell = () => {
	const execute = useParameterAction();
	return async (handleResult: (result: Running) => void) =>
		execute(
			"shell",
			{},
			(response) =>
				response
					.text()
					.then((resultDir) => ({
						command: "",
						resultMessage: "Save Shell Success",
						resultDir,
					})),
			handleResult,
		);
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
