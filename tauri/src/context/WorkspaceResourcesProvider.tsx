import type { Dispatch, ReactNode, SetStateAction } from "react";
import { createContext, Suspense, use, useState } from "react";
import SidecarLoading from "../app/startup/SidecarLoading";
import { StatusMessage } from "../components/dialog/DialogLayout";
import { BlueButton } from "../components/element/Button";
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

// 失敗したらエラーを通知してundefinedを返す
async function fetchWorkspaceResources(
	apiUrl: string,
): Promise<WorkspaceResources | undefined> {
	const fetchParams = {
		endpoint: `${apiUrl}workspace/resources`,
		options: {
			method: "GET",
		},
	};
	try {
		const response = await fetchData(fetchParams);
		return await response.json();
	} catch (ex) {
		handleFetchError(getErrorMessage(ex), fetchParams);
		return undefined;
	}
}

export default function WorkspaceResourcesProvider(props: {
	children: ReactNode;
}) {
	const { apiUrl } = useEnvironment();
	// use()に渡すPromiseは再描画で作り直すと再サスペンドするため、初回と再試行時だけ作る
	const [promise, setPromise] = useState(() => fetchWorkspaceResources(apiUrl));
	const retry = () => setPromise(fetchWorkspaceResources(apiUrl));
	return (
		<Suspense fallback={<SidecarLoading />}>
			<LoadResources promise={promise} retry={retry}>
				{props.children}
			</LoadResources>
		</Suspense>
	);
}
function LoadResources(props: {
	promise: Promise<WorkspaceResources | undefined>;
	retry: () => void;
	children: ReactNode;
}) {
	const resources = use(props.promise);
	if (!resources) {
		return (
			<div className="mx-auto max-w-4xl p-4">
				<StatusMessage kind="error">
					Failed to load workspace resources.
				</StatusMessage>
				<BlueButton title="Retry" handleClick={props.retry} />
			</div>
		);
	}
	// 状態は取得成功後に初めてマウントする子で持つ（初期値は一度しか読まれないため）
	return <CreateContext resources={resources}>{props.children}</CreateContext>;
}
function CreateContext(props: {
	resources: WorkspaceResources;
	children: ReactNode;
}) {
	const [context, setContext] = useState<WorkspaceContext>(() =>
		WorkspaceContext.from(props.resources.context),
	);
	const [parameterList, setParameterList] = useState<ParameterList>(() =>
		ParameterList.from(props.resources.parameterList),
	);
	const [resourcesSettings, setResourcesSettings] = useState<ResourcesSettings>(
		() => new ResourcesSettings(props.resources.resources),
	);
	return (
		<workspaceContext.Provider value={context}>
			<setWorkspaceContext.Provider value={setContext}>
				<parameterListContext.Provider value={parameterList}>
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
