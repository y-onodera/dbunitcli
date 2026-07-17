import { isAbsolute, sep } from "@tauri-apps/api/path";
import { useEnvironment } from "../context/EnvironmentProvider";
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
import {
	fetchData,
	getErrorMessage,
	handleFetchError,
} from "../utils/fetchUtils";

// parameterList・resourcesSettingsへのWorkspaceResources反映（update/reload共通）
const useApplyWorkspaceResources = () => {
	const setParameterList = useSetParameterList();
	const setResourcesSettings = useSetResourcesSettings();
	return (resources: WorkspaceResources) => {
		setParameterList(ParameterList.from(resources.parameterList));
		setResourcesSettings(new ResourcesSettings(resources.resources));
	};
};

export const useWorkspaceUpdate = () => {
	const setContext = useSetWorkspaceContext();
	const applyResources = useApplyWorkspaceResources();
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
				applyResources(resources);
			})
			.catch((ex) => handleFetchError(getErrorMessage(ex), fetchParams));
	};
};

export const useWorkspaceResourcesReload = () => {
	const applyResources = useApplyWorkspaceResources();
	const environment = useEnvironment();
	return async () => {
		const fetchParams = {
			endpoint: `${environment.apiUrl}workspace/resources`,
			options: {
				method: "GET",
			},
		};
		await fetchData(fetchParams)
			.then((response) => response.json())
			.then((resources: WorkspaceResources) => applyResources(resources))
			.catch((ex) => handleFetchError(getErrorMessage(ex), fetchParams));
	};
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
