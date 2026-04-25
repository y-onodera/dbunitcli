import { isAbsolute, sep } from "@tauri-apps/api/path";
import { useEnvironment } from "../context/EnvironmentProvider";
import {
	useSelectParameter,
	useSetSelectParameter,
} from "../context/SelectParameterProvider";
import {
	useSetParameterList,
	useSetResourcesSettings,
	useSetWorkspaceContext,
	useWorkspaceContext,
} from "../context/WorkspaceResourcesProvider";
import type { Attribute } from "../model/CommandOption";
import {
	ParameterList,
	ResourcesSettings,
	WorkspaceContext,
	type WorkspaceResources,
} from "../model/WorkspaceResources";
import { fetchData, getErrorMessage, handleFetchError } from "../utils/fetchUtils";

export const useWorkspaceUpdate = () => {
	const setContext = useSetWorkspaceContext();
	const setParameterList = useSetParameterList();
	const setResourcesSettings = useSetResourcesSettings();
	const environment = useEnvironment();
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
			.catch((ex) => handleFetchError(getErrorMessage(ex), fetchParams));
	};
};

export const useAddParameter = (command: string) => {
	const setParameter = useSetParameterList();
	const environment = useEnvironment();
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
			.catch((ex) => handleFetchError(getErrorMessage(ex), fetchParams));
	};
};

export const useParameterActions = (command: string, name: string) => {
	const setParameterList = useSetParameterList();
	const { apiUrl } = useEnvironment();
	const parameter = useSelectParameter();
	const setParameter = useSetSelectParameter();

	const postAndUpdateList = async (action: string) => {
		const fetchParams = {
			endpoint: `${apiUrl + command.toLowerCase()}/${action}`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ name }),
			},
		};
		await fetchData(fetchParams)
			.then((response) => response.json())
			.then((parameters: string[]) => {
				setParameterList((current) =>
					current.replace(command.toLowerCase(), parameters),
				);
			})
			.catch((ex) => handleFetchError(getErrorMessage(ex), fetchParams));
	};

	const handleDelete = () => postAndUpdateList("delete");
	const handleCopy = () => postAndUpdateList("copy");

	const handleRename = async (newName: string) => {
		const fetchParams = {
			endpoint: `${apiUrl + command.toLowerCase()}/rename`,
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
			.catch((ex) => handleFetchError(getErrorMessage(ex), fetchParams));
	};

	return { handleDelete, handleCopy, handleRename };
};

export const useResolveAbsolutePath = () => {
	const { apiUrl } = useEnvironment();
	const context = useWorkspaceContext();
	return async (path: string, attribute: Attribute): Promise<string> => {
		const fetchParams = {
			endpoint: `${apiUrl}workspace/resolve-path`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ path, defaultPath: attribute.defaultPath }),
			},
		};
		try {
			const response = await fetchData(fetchParams);
			const sidecarResult = await response.text();
			if (sidecarResult) {
				return sidecarResult;
			}
		} catch (e) {
			console.error(
				"workspace/resolve-path request failed, falling back to frontend:",
				e,
			);
		}
		if (await isAbsolute(path)) {
			return path;
		}
		const basePath = context.getPath(attribute.defaultPath);
		if (path) {
			return basePath + sep() + path;
		}
		return basePath;
	};
};
