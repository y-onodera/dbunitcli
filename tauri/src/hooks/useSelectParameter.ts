import { useEnviroment } from "../context/EnviromentProvider";
import {
	useSelectParameter,
	useSetSelectParameterState,
} from "../context/SelectParameterProvider";
import { useSetParameterList } from "../context/WorkspaceResourcesProvider";
import {
	type Parameter,
	type ParameterizeParams,
	SelectParameter,
} from "../model/CommandParam";
import { fetchData, handleFetchError } from "../utils/fetchUtils";

export type Running = {
	command: string;
	resultMessage: string;
	resultDir: string;
};

export const useLoadSelectParameter = () => {
	const setParameter = useSetSelectParameterState();
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
			.then((parameter: Parameter) => {
				setParameter(
					(current) =>
						new SelectParameter(parameter, current.command, current.name),
				);
			})
			.catch((ex) => handleFetchError((ex as Error).message, fetchParams));
	};
};

export const useSaveParameter = () => {
	const parameter = useSelectParameter();
	const environment = useEnviroment();
	return async (
		input: { [k: string]: FormDataEntryValue },
		handleResult: (result: Running) => void,
	) => {
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
				handleFetchError((ex as Error).message, fetchParams);
				handleResult({ command: "", resultMessage: ex.message, resultDir: "" });
			});
	};
};

export const useExecParameter = () => {
	const parameter = useSelectParameter();
	const environment = useEnviroment();
	return async (
		input: { [k: string]: FormDataEntryValue },
		handleResult: (result: Running) => void,
	) => {
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
			.then((resultDir: string) =>
				handleResult({
					command: "",
					resultMessage: "Execution Success",
					resultDir,
				}),
			)
			.catch((ex) => {
				handleFetchError((ex as Error).message, fetchParams);
				handleResult({ command: "", resultMessage: ex.message, resultDir: "" });
			});
	};
};

export const useSaveShell = () => {
	const parameter = useSelectParameter();
	const environment = useEnviroment();
	return async (handleResult: (result: Running) => void) => {
		const fetchParams = {
			endpoint: `${environment.apiUrl + parameter.command}/shell`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ name: parameter.name }),
			},
		};
		await fetchData(fetchParams)
			.then((response) => response.text())
			.then((resultDir: string) =>
				handleResult({
					command: "",
					resultMessage: "Save Shell Success",
					resultDir,
				}),
			)
			.catch((ex) => {
				handleFetchError((ex as Error).message, fetchParams);
				handleResult({ command: "", resultMessage: ex.message, resultDir: "" });
			});
	};
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
			.then((parameter: ParameterizeParams) => {
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
