import { type Dispatch, type ReactNode, type SetStateAction, Suspense, createContext, use, useState } from "react";
import { ParameterList, ResourcesSettings, WorkspaceContext, type WorkspaceResources } from "../model/WorkspaceResources";
import { fetchData, handleFetchError } from "../utils/fetchUtils";
import { useEnviroment } from "./EnviromentProvider";
import { useSelectParameter, useSetSelectParameter } from "./SelectParameterProvider";

const workspaceContext = createContext<WorkspaceContext>({} as WorkspaceContext);
const setWorkspaceContext = createContext<Dispatch<SetStateAction<WorkspaceContext>>>(
	() => undefined,
);
const parameterListContext = createContext<ParameterList>({} as ParameterList);
const setParameterListContext = createContext<Dispatch<SetStateAction<ParameterList>>>(
	() => undefined,
);
const resourcesSettingsContext = createContext<ResourcesSettings>({} as ResourcesSettings);
const setResourcesSettingsContext = createContext<Dispatch<SetStateAction<ResourcesSettings>>>(
	() => undefined,
);
export default function WorkspaceResourcesProvider(props: {
	children: ReactNode;
}) {
	const environment = useEnviroment();
	const workspaceReload = async () => {
		const fetchParams = {
			endpoint: `${environment.apiUrl}workspace/resources`,
			options: {
				method: "GET"
			},
		};
		return await fetchData(fetchParams)
			.then((response) => response.json())
			.catch((ex) => handleFetchError(ex, fetchParams));
	}
	return (
		<Suspense fallback={<div>Loading...</div>}>
			<CreateContext promise={workspaceReload()}>
				{props.children}
			</CreateContext>
		</Suspense>
	);
}
function CreateContext(props: { promise: Promise<WorkspaceResources>, children: ReactNode }) {
	const resources = use(props.promise);
	const [context, setContext] = useState<WorkspaceContext>(WorkspaceContext.from(resources.context));
	const [paramterList, setParameterList] = useState<ParameterList>(ParameterList.from(resources.parameterList));
	const [resourcesSettings, setResourcesSettings] = useState<ResourcesSettings>(new ResourcesSettings(resources.resources));
	return (
		<workspaceContext.Provider value={context}>
			<setWorkspaceContext.Provider value={setContext}>
				<parameterListContext.Provider value={paramterList}>
					<setParameterListContext.Provider value={setParameterList}>
						<resourcesSettingsContext.Provider value={resourcesSettings}>
							<setResourcesSettingsContext.Provider value={setResourcesSettings}>
								{props.children}
							</setResourcesSettingsContext.Provider>
						</resourcesSettingsContext.Provider>
					</setParameterListContext.Provider>
				</parameterListContext.Provider>
			</setWorkspaceContext.Provider>
		</workspaceContext.Provider>
	);
}
export const useWorkspaceContext = () => use(workspaceContext);
export const useSetWorkspaceContext = () => use(setWorkspaceContext);
export const useParameterList = () => use(parameterListContext);
export const useSetParameterList = () => use(setParameterListContext);
export const useResourcesSettings = () => use(resourcesSettingsContext);
export const useSetResourcesSettings = () => use(setResourcesSettingsContext);
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
				setContext(WorkspaceContext.from(resources.context));
				setParameterList(ParameterList.from(resources.parameterList));
				setResourcesSettings(new ResourcesSettings(resources.resources));
			})
			.catch((ex) => handleFetchError(ex, fetchParams));
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
				setParameter(current => current.replace(command.toLowerCase(), parameters));
			})
			.catch((ex) => handleFetchError(ex, fetchParams));
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
				setParameter(current => current.replace(command.toLowerCase(), parameters));
			})
			.catch((ex) => handleFetchError(ex, fetchParams));
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
				setParameter(current => current.replace(command.toLowerCase(), parameters));
			})
			.catch((ex) => handleFetchError(ex, fetchParams));
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
				setParameterList(current => current.replace(command.toLowerCase(), parameters));
				if (parameter.command === command.toLowerCase() && parameter.name === name) {
					setParameter(parameter.currentParameter(), parameter.command, newName);
				}
			})
			.catch((ex) => handleFetchError(ex, fetchParams));
	};
};
