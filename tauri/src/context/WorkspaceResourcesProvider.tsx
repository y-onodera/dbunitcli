import { Body, ResponseType, fetch } from "@tauri-apps/api/http";
import { type Dispatch, type ReactNode, type SetStateAction, createContext, useContext, useEffect, useState } from "react";
import { ParameterList, type ResourcesSettings, type WorkspaceResources } from "../model/WorkspaceResources";
import { useEnviroment } from "./EnviromentProvider";
import { useSelectParameter, useSetSelectParameter } from "./SelectParameterProvider";

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
	const [paramterList, setParameterList] = useState<ParameterList>(ParameterList.create());
	const [resourcesSettings, setResourcesSettings] = useState<ResourcesSettings>({} as ResourcesSettings);
	const environment = useEnviroment();
	useEffect(() => {
		const workspaceReload = async () => {
			await fetch(`${environment.apiUrl}workspace/resources`, {
				method: "GET",
				responseType: ResponseType.JSON,
			})
				.then((response) => {
					if (!response.ok) {
						console.error("response.ok:", response.ok);
						console.error("esponse.status:", response.status);
						throw new Error(response.data as string);
					}
					const resources = response.data as WorkspaceResources
					setParameterList(ParameterList.from(resources.parameterList));
					setResourcesSettings(resources.resources)
				})
				.catch((ex) => alert(ex));
		};
		workspaceReload();
	}, [environment]);
	return (
		<parameterListContext.Provider value={paramterList}>
			<setParameterListContext.Provider value={setParameterList}>
				<resourcesSettingsContext.Provider value={resourcesSettings}>
					<setResourcesSettingsContext.Provider value={setResourcesSettings}>
						{props.children}
					</setResourcesSettingsContext.Provider>
				</resourcesSettingsContext.Provider>
			</setParameterListContext.Provider>
		</parameterListContext.Provider>
	);
}
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
			responseType: ResponseType.JSON,
		})
			.then((response) => {
				if (!response.ok) {
					console.error("response.ok:", response.ok);
					console.error("esponse.status:", response.status);
					throw new Error(response.data as string);
				}
				setParameter(current => current.replace(command.toLowerCase(), response.data as string[]));
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
			responseType: ResponseType.JSON,
			headers: { "Content-Type": "application/json" },
			body: Body.json({ name: name }),
		})
			.then((response) => {
				if (!response.ok) {
					console.error("response.ok:", response.ok);
					console.error("esponse.status:", response.status);
					throw new Error(response.data as string);
				}
				setParameter(current => current.replace(command.toLowerCase(), response.data as string[]));
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
			responseType: ResponseType.JSON,
			headers: { "Content-Type": "application/json" },
			body: Body.json({ name: name }),
		})
			.then((response) => {
				if (!response.ok) {
					console.error("response.ok:", response.ok);
					console.error("esponse.status:", response.status);
					throw new Error(response.data as string);
				}
				setParameter(current => current.replace(command.toLowerCase(), response.data as string[]));
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
			responseType: ResponseType.JSON,
			headers: { "Content-Type": "application/json" },
			body: Body.json({ oldName: name, newName }),
		})
			.then((response) => {
				if (!response.ok) {
					console.error("response.ok:", response.ok);
					console.error("esponse.status:", response.status);
					throw new Error(response.data as string);
				}
				setParameterList(current => current.replace(command.toLowerCase(), response.data as string[]));
				if (parameter.command === command.toLowerCase() && parameter.name === name) {
					setParameter(parameter.currentParameter(), parameter.command, newName);
				}
			})
			.catch((ex) => alert(ex));
	}
}
