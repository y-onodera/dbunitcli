import { fetch } from "@tauri-apps/plugin-http";
import { type Dispatch, type ReactNode, type SetStateAction, createContext, useContext, useEffect, useState } from "react";
import { ParameterList, type ResourcesSettings, type WorkspaceContext, type WorkspaceResources } from "../model/WorkspaceResources";
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
	const [context, setContext] = useState<WorkspaceContext>({} as WorkspaceContext);
	const [paramterList, setParameterList] = useState<ParameterList>(ParameterList.create());
	const [resourcesSettings, setResourcesSettings] = useState<ResourcesSettings>({} as ResourcesSettings);
	const environment = useEnviroment();
	useEffect(() => {
		const workspaceReload = async () => {
			await fetch(`${environment.apiUrl}workspace/resources`, {
				method: "GET",
			})
				.then((response) => {
					if (!response.ok) {
						console.error("response.ok:", response.ok);
						console.error("esponse.status:", response.status);
						throw new Error(response.statusText);
					}
					response.json().then((resources: WorkspaceResources) => {
						setContext(resources.context);
						setParameterList(ParameterList.from(resources.parameterList));
						setResourcesSettings(resources.resources)
					});
				})
				.catch((ex) => alert(ex));
		};
		workspaceReload();
	}, [environment]);
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
export const useWorkspaceContext = () => useContext(workspaceContext);
export const useParameterList = () => useContext(parameterListContext);
export const useSetParameterList = () => useContext(setParameterListContext);
export const useResourcesSettings = () => useContext(resourcesSettingsContext);
export const useSetResourcesSettings = () => useContext(setResourcesSettingsContext);
export const useAddParameter = (command: string) => {
	const setParameter = useSetParameterList();
	const environment = useEnviroment();
	return async () => {
		await fetch(`${environment.apiUrl + command.toLowerCase()}/add`, {
			method: "GET",
		})
			.then((response) => {
				if (!response.ok) {
					console.error("response.ok:", response.ok);
					console.error("esponse.status:", response.status);
					throw new Error(response.statusText);
				}
				response.json().then((parameters: string[]) => {
					setParameter(current => current.replace(command.toLowerCase(), parameters))
				})
			})
			.catch((ex) => alert(ex));
	}
}
export const useDeleteParameter = (command: string, name: string) => {
	const setParameter = useSetParameterList();
	const environment = useEnviroment();
	return async () => {
		await fetch(`${environment.apiUrl + command.toLowerCase()}/delete`, {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({ name: name }),
		})
			.then((response) => {
				if (!response.ok) {
					console.error("response.ok:", response.ok);
					console.error("esponse.status:", response.status);
					throw new Error(response.statusText);
				}
				response.json().then((parameters: string[]) => {
					setParameter(current => current.replace(command.toLowerCase(), parameters))
				})
			})
			.catch((ex) => alert(ex));
	}
}
export const useCopyParameter = (command: string, name: string) => {
	const setParameter = useSetParameterList();
	const environment = useEnviroment();
	return async () => {
		await fetch(`${environment.apiUrl + command.toLowerCase()}/copy`, {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({ name: name }),
		})
			.then((response) => {
				if (!response.ok) {
					console.error("response.ok:", response.ok);
					console.error("esponse.status:", response.status);
					throw new Error(response.statusText);
				}
				response.json().then((parameters: string[]) => {
					setParameter(current => current.replace(command.toLowerCase(), parameters))
				})
			})
			.catch((ex) => alert(ex));
	}
}
export const useRenameParameter = (command: string, name: string) => {
	const parameter = useSelectParameter();
	const setParameter = useSetSelectParameter();
	const setParameterList = useSetParameterList();
	const environment = useEnviroment();
	return async (newName: string) => {
		await fetch(`${environment.apiUrl + command.toLowerCase()}/rename`, {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({ oldName: name, newName }),
		})
			.then((response) => {
				if (!response.ok) {
					console.error("response.ok:", response.ok);
					console.error("esponse.status:", response.status);
					throw new Error(response.statusText);
				}
				response.json().then((parameters: string[]) => {
					setParameterList(current => current.replace(command.toLowerCase(), parameters))
					if (parameter.command === command.toLowerCase() && parameter.name === name) {
						setParameter(parameter.currentParameter(), parameter.command, newName);
					}
				})
			})
			.catch((ex) => alert(ex));
	}
}
