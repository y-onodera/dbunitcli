import { useEffect, useState } from "react";
import { useEnvironment } from "../context/EnvironmentProvider";
import { useJdbcConnectionState } from "../context/JdbcConnectionProvider";
import { useSetResourcesSettings } from "../context/WorkspaceResourcesProvider";
import type { DatasetSrcInfo } from "../model/CommandOption";
import {
	DatasetSettings,
	type DatasetSettingsBuilder,
} from "../model/DatasetSettings";
import { fetchAndUpdate, fetchData, getErrorMessage, handleFetchError, type OperationResult } from "../utils/fetchUtils";

async function fetchTableNames(
	apiUrl: string,
	info: DatasetSrcInfo,
	jdbcValues: Record<string, string>,
): Promise<string[]> {
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
}

export const useDatasetTableNames = (
	srcInfo: DatasetSrcInfo,
): { tableNames: string[]; loading: boolean } => {
	const [tableNames, setTableNames] = useState<string[]>([]);
	const [loading, setLoading] = useState(false);
	const { apiUrl } = useEnvironment();
	const { jdbcValues, connectionOk } = useJdbcConnectionState();

	const srcPath = srcInfo.srcPath;
	const srcType = srcInfo.srcType;
	const sqlNotReady = srcType === "sql" && !connectionOk;

	useEffect(() => {
		if (!srcPath || !srcType || srcType === "none" || sqlNotReady) {
			setTableNames([]);
			setLoading(false);
			return;
		}
		let isMounted = true;
		setLoading(true);
		fetchTableNames(apiUrl, srcInfo, jdbcValues).then((names) => {
			if (isMounted) {
				setTableNames(names);
				setLoading(false);
			}
		});
		return () => {
			isMounted = false;
		};
	}, [apiUrl, srcPath, srcType, srcInfo, sqlNotReady, jdbcValues]);

	return { tableNames, loading };
};

export const useDeleteDatasetSettings = () => {
	const { apiUrl } = useEnvironment();
	const setResourcesSettings = useSetResourcesSettings();
	return async (name: string): Promise<OperationResult> =>
		fetchAndUpdate<string[]>(
			{
				endpoint: `${apiUrl}dataset-setting/delete`,
				options: { method: "POST", headers: { "Content-Type": "text/plain" }, body: name },
			},
			(settings) => setResourcesSettings((current) => current.with({ datasetSettings: settings })),
		);
};

export const useSaveDatasetSettings = () => {
	const { apiUrl } = useEnvironment();
	const setResourcesSettings = useSetResourcesSettings();
	return async (name: string, input: DatasetSettings): Promise<OperationResult> =>
		fetchAndUpdate<string[]>(
			{
				endpoint: `${apiUrl}dataset-setting/save`,
				options: { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ name, input }) },
			},
			(settings) => setResourcesSettings((current) => current.with({ datasetSettings: settings })),
		);
};

export const useDatasetSettingsData = (
	fileName: string,
): { settings: DatasetSettings; loading: boolean } => {
	const { apiUrl } = useEnvironment();
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
