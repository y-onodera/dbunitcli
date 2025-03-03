import { fetch } from "@tauri-apps/plugin-http";
import { type Dispatch, type ReactNode, type SetStateAction, createContext, useContext, useEffect, useState } from "react";
import { ParameterList, type ResourcesSettings, type WorkspaceContext, type WorkspaceResources } from "../model/WorkspaceResources";
import { useEnviroment } from "./EnviromentProvider";
import { useSelectParameter, useSetSelectParameter } from "./SelectParameterProvider";

// biome-ignore lint/suspicious/noExplicitAny: <explanation>
const handleFetchError = (ex: any, endpoint: string, method: string, requestBody?: any) => {
	const errorInfo = {
		message: ex.message || '不明なエラーが発生しました',
		endpoint: endpoint,
		method: method,
		status: ex.response?.status || 'N/A',
		details: ex.toString(),
		requestBody: requestBody ? JSON.stringify(requestBody, null, 2) : 'なし'
	};

	const errorMessage = `
エラーが発生しました
メッセージ: ${errorInfo.message}
エンドポイント: ${errorInfo.endpoint}
メソッド: ${errorInfo.method}
ステータス: ${errorInfo.status}
リクエストボディ: ${errorInfo.requestBody}
詳細: ${errorInfo.details}
    `.trim();

	alert(errorMessage);
	console.error('Fetch Error:', errorInfo);
};
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
	const [workspace, setWorkspace] = useState<string | null>(null);

	useEffect(() => {
		const workspaceReload = async () => {
			console.log(context);
			const endpoint = `${environment.apiUrl}workspace/resources`;
			await fetch(endpoint, {
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
						setResourcesSettings(resources.resources);
						setWorkspace(resources.context.workspace);
					});
				})
				.catch((ex) => handleFetchError(ex, endpoint, "GET"));
		};
		workspaceReload();
	}, [environment.apiUrl, context]);

	if (workspace === null) {
		return <div>Loading...</div>;
	}

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
export const useSetWorkspaceContext = () => useContext(setWorkspaceContext);
export const useParameterList = () => useContext(parameterListContext);
export const useSetParameterList = () => useContext(setParameterListContext);
export const useResourcesSettings = () => useContext(resourcesSettingsContext);
export const useSetResourcesSettings = () => useContext(setResourcesSettingsContext);
export const useAddParameter = (command: string) => {
	const setParameter = useSetParameterList();
	const environment = useEnviroment();
	return async () => {
		const endpoint = `${environment.apiUrl + command.toLowerCase()}/add`;
		await fetch(endpoint, {
			method: "GET",
		})
			.then(async (response) => {
				if (!response.ok) {
					throw new Error(response.statusText || 'パラメータの追加に失敗しました');
				}
				const parameters: string[] = await response.json();
				setParameter(current => current.replace(command.toLowerCase(), parameters));
			})
			.catch((ex) => handleFetchError(ex, endpoint, "GET"));
	};
};
export const useWorkspaceUpdate = () => {
	const setContext = useSetWorkspaceContext();
	const environment = useEnviroment();
	return async (workspace: string, datasetBase: string, resultBase: string) => {
		const endpoint = `${environment.apiUrl}workspace/update`;
		const requestBody = { workspace, datasetBase, resultBase };
		await fetch(endpoint, {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(requestBody),
		})
			.then(async (response) => {
				if (!response.ok) {
					throw new Error(response.statusText || 'ワークスペースの更新に失敗しました');
				}
				setContext(current => ({ ...current, ...requestBody }));
			})
			.catch((ex) => handleFetchError(ex, endpoint, "POST", requestBody));
	};
};
export const useDeleteParameter = (command: string, name: string) => {
	const setParameter = useSetParameterList();
	const environment = useEnviroment();
	return async () => {
		const endpoint = `${environment.apiUrl + command.toLowerCase()}/delete`;
		const requestBody = { name };
		await fetch(endpoint, {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(requestBody),
		})
			.then(async (response) => {
				if (!response.ok) {
					throw new Error(response.statusText || 'パラメータの削除に失敗しました');
				}
				const parameters: string[] = await response.json();
				setParameter(current => current.replace(command.toLowerCase(), parameters));
			})
			.catch((ex) => handleFetchError(ex, endpoint, "POST", requestBody));
	};
};
export const useCopyParameter = (command: string, name: string) => {
	const setParameter = useSetParameterList();
	const environment = useEnviroment();
	return async () => {
		const endpoint = `${environment.apiUrl + command.toLowerCase()}/copy`;
		const requestBody = { name };
		await fetch(endpoint, {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(requestBody),
		})
			.then(async (response) => {
				if (!response.ok) {
					throw new Error(response.statusText || 'パラメータのコピーに失敗しました');
				}
				const parameters: string[] = await response.json();
				setParameter(current => current.replace(command.toLowerCase(), parameters));
			})
			.catch((ex) => handleFetchError(ex, endpoint, "POST", requestBody));
	};
};
export const useRenameParameter = (command: string, name: string) => {
	const parameter = useSelectParameter();
	const setParameter = useSetSelectParameter();
	const setParameterList = useSetParameterList();
	const environment = useEnviroment();
	return async (newName: string) => {
		const endpoint = `${environment.apiUrl + command.toLowerCase()}/rename`;
		const requestBody = { oldName: name, newName };
		await fetch(endpoint, {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(requestBody),
		})
			.then(async (response) => {
				if (!response.ok) {
					throw new Error(response.statusText || 'パラメータの名前変更に失敗しました');
				}
				const parameters: string[] = await response.json();
				setParameterList(current => current.replace(command.toLowerCase(), parameters));
				if (parameter.command === command.toLowerCase() && parameter.name === name) {
					setParameter(parameter.currentParameter(), parameter.command, newName);
				}
			})
			.catch((ex) => handleFetchError(ex, endpoint, "POST", requestBody));
	};
};
