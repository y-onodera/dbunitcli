import { useEnviroment } from "../context/EnviromentProvider";
import {
	useSelectParameter,
	useSetSelectParameter,
} from "../context/SelectParameterProvider";
import {
	useSetParameterList,
	useSetResourcesSettings,
	useSetWorkspaceContext,
} from "../context/WorkspaceResourcesProvider";
import {
	ParameterList,
	ResourcesSettings,
	WorkspaceContext,
	type WorkspaceResources,
} from "../model/WorkspaceResources";
import { fetchData, handleFetchError } from "../utils/fetchUtils";

export const useWorkspaceUpdate = () => {
	const setContext = useSetWorkspaceContext();
	const setParameterList = useSetParameterList();
	const setResourcesSettings = useSetResourcesSettings();
	const environment = useEnviroment();
	return async (workspace: string, datasetBase: string, resultBase: string) => {
		const fetchParams = {
			endpoint: `${environment.apiUrl}workspace/update`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ workspace, datasetBase, resultBase }),
			},
		};
		await fetchData(fetchParams)
			.then((response) => response.json())
			.then((resources: WorkspaceResources) => {
				setContext(
					WorkspaceContext.from(resources.context).with({
						workspace,
						datasetBase,
						resultBase,
					}),
				);
				setParameterList(ParameterList.from(resources.parameterList));
				setResourcesSettings(new ResourcesSettings(resources.resources));
			})
			.catch((ex) => handleFetchError((ex as Error).message, fetchParams));
	};
};

export const useAddParameter = (command: string) => {
	const setParameter = useSetParameterList();
	const environment = useEnviroment();
	return async () => {
		const fetchParams = {
			endpoint: `${environment.apiUrl + command.toLowerCase()}/add`,
			options: {
				method: "GET",
				headers: { "Content-Type": "application/json" },
			},
		};
		await fetchData(fetchParams)
			.then((response) => response.json())
			.then((parameters: string[]) => {
				setParameter((current) =>
					current.replace(command.toLowerCase(), parameters),
				);
			})
			.catch((ex) => handleFetchError((ex as Error).message, fetchParams));
	};
};

export const useDeleteParameter = (command: string, name: string) => {
	const setParameter = useSetParameterList();
	const environment = useEnviroment();
	return async () => {
		const fetchParams = {
			endpoint: `${environment.apiUrl + command.toLowerCase()}/delete`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ name }),
			},
		};
		await fetchData(fetchParams)
			.then((response) => response.json())
			.then((parameters: string[]) => {
				setParameter((current) =>
					current.replace(command.toLowerCase(), parameters),
				);
			})
			.catch((ex) => handleFetchError((ex as Error).message, fetchParams));
	};
};

export const useCopyParameter = (command: string, name: string) => {
	const setParameter = useSetParameterList();
	const environment = useEnviroment();
	return async () => {
		const fetchParams = {
			endpoint: `${environment.apiUrl + command.toLowerCase()}/copy`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ name }),
			},
		};
		await fetchData(fetchParams)
			.then((response) => response.json())
			.then((parameters: string[]) => {
				setParameter((current) =>
					current.replace(command.toLowerCase(), parameters),
				);
			})
			.catch((ex) => handleFetchError((ex as Error).message, fetchParams));
	};
};

export const useRenameParameter = (command: string, name: string) => {
	const parameter = useSelectParameter();
	const setParameter = useSetSelectParameter();
	const setParameterList = useSetParameterList();
	const environment = useEnviroment();
	return async (newName: string) => {
		const fetchParams = {
			endpoint: `${environment.apiUrl + command.toLowerCase()}/rename`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ oldName: name, newName }),
			},
		};
		await fetchData(fetchParams)
			.then((response) => response.json())
			.then((parameters: string[]) => {
				setParameterList((current) =>
					current.replace(command.toLowerCase(), parameters),
				);
				if (
					parameter.command === command.toLowerCase() &&
					parameter.name === name
				) {
					setParameter(parameter.options, parameter.command, newName);
				}
			})
			.catch((ex) => handleFetchError((ex as Error).message, fetchParams));
	};
};
