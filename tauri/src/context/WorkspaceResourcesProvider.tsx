import type { Dispatch, ReactNode, SetStateAction } from "react";
import { createContext, Suspense, use, useState } from "react";
import {
	ParameterList,
	ResourcesSettings,
	WorkspaceContext,
	type WorkspaceResources,
} from "../model/WorkspaceResources";
import { fetchData, getErrorMessage, handleFetchError } from "../utils/fetchUtils";
import { useEnvironment } from "./EnvironmentProvider";

const workspaceContext = createContext<WorkspaceContext>(
	{} as WorkspaceContext,
);
const setWorkspaceContext = createContext<
	Dispatch<SetStateAction<WorkspaceContext>>
>(() => undefined);
const parameterListContext = createContext<ParameterList>({} as ParameterList);
const setParameterListContext = createContext<
	Dispatch<SetStateAction<ParameterList>>
>(() => undefined);
const resourcesSettingsContext = createContext<ResourcesSettings>(
	{} as ResourcesSettings,
);
const setResourcesSettingsContext = createContext<
	Dispatch<SetStateAction<ResourcesSettings>>
>(() => undefined);
export default function WorkspaceResourcesProvider(props: {
	children: ReactNode;
}) {
	const environment = useEnvironment();
	const workspaceReload = async () => {
		const fetchParams = {
			endpoint: `${environment.apiUrl}workspace/resources`,
			options: {
				method: "GET",
			},
		};
		return await fetchData(fetchParams)
			.then((response) => response.json())
			.catch((ex) => handleFetchError(getErrorMessage(ex), fetchParams));
	};
	return (
		<Suspense fallback={<div>Loading...</div>}>
			<CreateContext promise={workspaceReload()}>
				{props.children}
			</CreateContext>
		</Suspense>
	);
}
function CreateContext(props: {
	promise: Promise<WorkspaceResources>;
	children: ReactNode;
}) {
	const resources = use(props.promise);
	const [context, setContext] = useState<WorkspaceContext>(
		WorkspaceContext.from(resources.context),
	);
	const [paramterList, setParameterList] = useState<ParameterList>(
		ParameterList.from(resources.parameterList),
	);
	const [resourcesSettings, setResourcesSettings] = useState<ResourcesSettings>(
		new ResourcesSettings(resources.resources),
	);
	return (
		<workspaceContext.Provider value={context}>
			<setWorkspaceContext.Provider value={setContext}>
				<parameterListContext.Provider value={paramterList}>
					<setParameterListContext.Provider value={setParameterList}>
						<resourcesSettingsContext.Provider value={resourcesSettings}>
							<setResourcesSettingsContext.Provider
								value={setResourcesSettings}
							>
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
