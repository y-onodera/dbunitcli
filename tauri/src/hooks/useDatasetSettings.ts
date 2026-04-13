import type { Dispatch, SetStateAction } from "react";
import { useEffect, useState } from "react";
import { useEnviroment } from "../context/EnviromentProvider";
import { useJdbcConnectionState } from "../context/JdbcConnectionProvider";
import { useSetResourcesSettings } from "../context/WorkspaceResourcesProvider";
import type { DatasetSrcInfo } from "../model/CommandOption";
import {
	DatasetSettings,
	type DatasetSettingsBuilder,
} from "../model/DatasetSettings";
import type { ResourcesSettings } from "../model/WorkspaceResources";
import { fetchData, getErrorMessage, handleFetchError, type OperationResult } from "../utils/fetchUtils";

export const useDatasetTableNamesApi = () => {
	const { apiUrl } = useEnviroment();
	return async (
		info: DatasetSrcInfo,
		jdbcValues: Record<string, string>,
	): Promise<string[]> => {
		if (!info.srcPath) {
			return [];
		}
		const fetchParams = {
			endpoint: `${apiUrl}dataset-setting/table-names`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({
					setting: info.setting ?? "",
					srcType: info.srcType,
					src: info.srcPath,
					regTableInclude: info.regTableInclude,
					regTableExclude: info.regTableExclude,
					recursive: info.recursive === "true",
					regInclude: info.regInclude,
					regExclude: info.regExclude,
					extension: info.extension,
					xlsxSchema: info.xlsxSchema,
					fixedLength: info.fixedLength,
					regHeaderSplit: info.regHeaderSplit,
					regDataSplit: info.regDataSplit,
					encoding: info.encoding,
					delimiter: info.delimiter,
					ignoreQuoted: info.ignoreQuoted,
					headerName: info.headerName,
					startRow: info.startRow,
					addFileInfo: info.addFileInfo,
					jdbcUrl: jdbcValues.jdbcUrl ?? "",
					jdbcUser: jdbcValues.jdbcUser ?? "",
					jdbcPass: jdbcValues.jdbcPass ?? "",
					jdbcProperties: jdbcValues.jdbcProperties ?? "",
				}),
			},
		};
		return fetchData(fetchParams)
			.then((r) => r.json())
			.catch(() => []);
	};
};

export const useDatasetTableNames = (
	srcInfo: DatasetSrcInfo,
): { tableNames: string[]; loading: boolean } => {
	const [tableNames, setTableNames] = useState<string[]>([]);
	const [loading, setLoading] = useState(false);
	const { jdbcValues, connectionOk } = useJdbcConnectionState();
	const loadTableNames = useDatasetTableNamesApi();

	const srcPath = srcInfo.srcPath;
	const srcType = srcInfo.srcType;
	const sqlNotReady = srcType === "sql" && !connectionOk;

	useEffect(() => {
		if (!srcPath || !srcType || srcType === "none" || sqlNotReady) {
			setTableNames([]);
			setLoading(false);
			return;
		}
		setLoading(true);
		loadTableNames(srcInfo, jdbcValues).then((names) => {
			setTableNames(names);
			setLoading(false);
		});
	}, [srcPath, srcType, srcInfo, sqlNotReady, jdbcValues, loadTableNames]);

	return { tableNames, loading };
};

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

export const useDatasetSettingsData = (
	fileName: string,
): { settings: DatasetSettings; loading: boolean } => {
	const { apiUrl } = useEnviroment();
	const [settings, setSettings] = useState(DatasetSettings.create());
	const [loading, setLoading] = useState(fileName !== "");

	useEffect(() => {
		if (!fileName) {
			setSettings(DatasetSettings.create());
			setLoading(false);
			return;
		}
		let isMounted = true;
		setLoading(true);
		loadDatasetSettings(apiUrl, fileName).then((result) => {
			if (isMounted) {
				setSettings(result);
				setLoading(false);
			}
		});
		return () => {
			isMounted = false;
		};
	}, [fileName, apiUrl]);

	return { settings, loading };
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
			handleFetchError(getErrorMessage(ex), fetchParams);
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
				current.with({ datasetSettings: settings }),
			);
			return "success" as OperationResult;
		})
		.catch((ex) => {
			handleFetchError(getErrorMessage(ex), fetchParams);
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
				current.with({ datasetSettings: settings }),
			);
			return "success" as OperationResult;
		})
		.catch((ex) => {
			handleFetchError(getErrorMessage(ex), fetchParams);
			return "failed" as OperationResult;
		});
}
