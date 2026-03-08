import type { Dispatch, SetStateAction } from "react";
import { useEnviroment } from "../context/EnviromentProvider";
import { useSetResourcesSettings } from "../context/WorkspaceResourcesProvider";
import {
	DatasetSettings,
	type DatasetSettingsBuilder,
} from "../model/DatasetSettings";
import type { ResourcesSettings } from "../model/WorkspaceResources";
import { fetchData, handleFetchError } from "../utils/fetchUtils";

type OperationResult = "success" | "failed";

export const useDeleteDatasetSettings = () => {
	const environment = useEnviroment();
	const setResourcesSettings = useSetResourcesSettings();
	return async (name: string) => {
		return deleteDatasetSettings(
			environment.apiUrl,
			name,
			setResourcesSettings,
		);
	};
};

export const useSaveDatasetSettings = () => {
	const environment = useEnviroment();
	const setResourcesSettings = useSetResourcesSettings();
	return async (name: string, input: DatasetSettings) => {
		return saveDatasetSettings(
			environment.apiUrl,
			name,
			input,
			setResourcesSettings,
		);
	};
};

export const useLoadDatasetSettings = () => {
	const environment = useEnviroment();
	return async (name: string) => {
		return loadDatasetSettings(environment.apiUrl, name);
	};
};

async function loadDatasetSettings(
	apiUrl: string,
	name: string,
): Promise<DatasetSettings> {
	if (name === "") {
		return DatasetSettings.create();
	}
	const fetchParams = {
		endpoint: `${apiUrl}dataset-setting/load`,
		options: {
			method: "POST",
			headers: { "Content-Type": "text/plain" },
			body: name,
		},
	};
	return await fetchData(fetchParams)
		.then((response) => response.json())
		.then((setting: DatasetSettingsBuilder) => DatasetSettings.build(setting))
		.catch((ex) => {
			handleFetchError((ex as Error).message, fetchParams);
			return DatasetSettings.create();
		});
}

async function saveDatasetSettings(
	apiUrl: string,
	name: string,
	input: DatasetSettings,
	setResourcesSettings: Dispatch<SetStateAction<ResourcesSettings>>,
): Promise<OperationResult> {
	const fetchParams = {
		endpoint: `${apiUrl}dataset-setting/save`,
		options: {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({ name, input }),
		},
	};

	return await fetchData(fetchParams)
		.then((response) => response.json())
		.then((settings: string[]) => {
			setResourcesSettings((current) =>
				current.with({ metadataSetting: settings }),
			);
			return "success" as OperationResult;
		})
		.catch((ex) => {
			handleFetchError((ex as Error).message, fetchParams);
			return "failed" as OperationResult;
		});
}

async function deleteDatasetSettings(
	apiUrl: string,
	name: string,
	setResourcesSettings: Dispatch<SetStateAction<ResourcesSettings>>,
): Promise<OperationResult> {
	const fetchParams = {
		endpoint: `${apiUrl}dataset-setting/delete`,
		options: {
			method: "POST",
			headers: { "Content-Type": "text/plain" },
			body: name,
		},
	};

	return await fetchData(fetchParams)
		.then((response) => response.json())
		.then((settings: string[]) => {
			setResourcesSettings((current) =>
				current.with({ metadataSetting: settings }),
			);
			return "success" as OperationResult;
		})
		.catch((ex) => {
			handleFetchError((ex as Error).message, fetchParams);
			return "failed" as OperationResult;
		});
}
