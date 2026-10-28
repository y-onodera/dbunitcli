import { useEffect, useState } from "react";
import { useEnvironment } from "../context/EnvironmentProvider";
import { useJdbcConnectionState } from "../context/JdbcConnectionProvider";
import { useParamInputs } from "../context/ParameterInputProvider";
import type { DatasetSrcInfo } from "../model/CommandOption";
import {
	DatasetSettings,
	type DatasetSettingsBuilder,
} from "../model/DatasetSettings";
import {
	type FetchParams,
	fetchData,
	getErrorMessage,
	handleFetchError,
} from "../utils/fetchUtils";
import type { FormValues } from "../utils/formValues";
import { appendLogLines } from "./useJobLog";
import { useUpdateResourceFiles } from "./useResourceFiles";

function buildDatasetRequestBody(
	info: DatasetSrcInfo,
	jdbcValues: Record<string, string>,
	extra?: Record<string, unknown>,
): string {
	return JSON.stringify({
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
		fixedLengthType: info.fixedLengthType,
		regHeaderSplit: info.regHeaderSplit,
		regDataSplit: info.regDataSplit,
		encoding: info.encoding,
		settingEncoding: info.settingEncoding,
		delimiter: info.delimiter,
		ignoreQuoted: info.ignoreQuoted,
		headerName: info.headerName,
		startRow: info.startRow,
		addFileInfo: info.addFileInfo,
		jdbcUrl: jdbcValues.jdbcUrl ?? "",
		jdbcUser: jdbcValues.jdbcUser ?? "",
		jdbcPass: jdbcValues.jdbcPass ?? "",
		jdbcProperties: jdbcValues.jdbcProperties ?? "",
		...extra,
	});
}

// ソース未指定・none・JDBC未接続のsqlではsidecarへ問い合わせない
function isSrcReady(info: DatasetSrcInfo, connectionOk: boolean): boolean {
	if (!info.srcPath || !info.srcType || info.srcType === "none") {
		return false;
	}
	return info.srcType !== "sql" || connectionOk;
}

async function fetchTableNames(
	apiUrl: string,
	info: DatasetSrcInfo,
	jdbcValues: Record<string, string>,
	paramInputs: Record<string, string>,
	signal?: AbortSignal,
	onLog?: (line: string) => void,
): Promise<string[]> {
	if (!info.srcPath) {
		return [];
	}
	const fetchParams = {
		endpoint: `${apiUrl}dataset-setting/table-names`,
		options: {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: buildDatasetRequestBody(info, jdbcValues, {
				parameters: paramInputs,
			}),
		},
		signal,
		onLog,
	};
	return fetchData(fetchParams)
		.then((r) => r.json())
		.catch(() => []);
}

// withLogs: 読込中のログも受け取る（読込中の表示に使う場合）
export const useDatasetTableNames = (
	srcInfo: DatasetSrcInfo,
	withLogs = false,
): { tableNames: string[]; loading: boolean; logs: string[] } => {
	const [tableNames, setTableNames] = useState<string[]>([]);
	const [loading, setLoading] = useState(false);
	const [logs, setLogs] = useState<string[]>([]);
	const { apiUrl } = useEnvironment();
	const { jdbcValues, connectionOk } = useJdbcConnectionState();
	const paramInputs = useParamInputs();

	const srcReady = isSrcReady(srcInfo, connectionOk);

	useEffect(() => {
		if (!srcReady) {
			setTableNames([]);
			setLoading(false);
			return;
		}
		const controller = new AbortController();
		setLoading(true);
		setLogs([]);
		fetchTableNames(
			apiUrl,
			srcInfo,
			jdbcValues,
			paramInputs,
			controller.signal,
			withLogs
				? (line) => setLogs((current) => appendLogLines(current, [line]))
				: undefined,
		).then((names) => {
			if (!controller.signal.aborted) {
				setTableNames(names);
				setLoading(false);
			}
		});
		return () => {
			controller.abort();
		};
	}, [apiUrl, srcInfo, srcReady, jdbcValues, paramInputs, withLogs]);

	return { tableNames, loading, logs };
};

// rowsは先頭の一部だけ（sidecarが件数を制限する）。totalRowsが対象テーブルの全行数
export type TablePreview = {
	headers: string[];
	rows: string[][];
	totalRows: number;
};

const EMPTY_PREVIEW: TablePreview = { headers: [], rows: [], totalRows: 0 };

async function fetchTablePreview(
	apiUrl: string,
	info: DatasetSrcInfo,
	tableName: string,
	jdbcValues: Record<string, string>,
	paramInputs: Record<string, string>,
	signal?: AbortSignal,
	onLog?: (line: string) => void,
): Promise<TablePreview> {
	if (!info.srcPath || !tableName) {
		return EMPTY_PREVIEW;
	}
	const fetchParams = {
		endpoint: `${apiUrl}dataset-setting/table-preview`,
		options: {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: buildDatasetRequestBody(info, jdbcValues, {
				tableName,
				parameters: paramInputs,
			}),
		},
		signal,
		onLog,
	};
	return fetchData(fetchParams)
		.then((r) => r.json())
		.catch(() => EMPTY_PREVIEW);
}

export const useDatasetTablePreview = (
	srcInfo: DatasetSrcInfo,
	tableName: string,
): { preview: TablePreview | null; loading: boolean; logs: string[] } => {
	const [preview, setPreview] = useState<TablePreview | null>(null);
	const [loading, setLoading] = useState(false);
	const [logs, setLogs] = useState<string[]>([]);
	const { apiUrl } = useEnvironment();
	const { jdbcValues, connectionOk } = useJdbcConnectionState();
	const paramInputs = useParamInputs();

	const srcReady = isSrcReady(srcInfo, connectionOk);

	useEffect(() => {
		if (!srcReady || !tableName) {
			setPreview(null);
			setLoading(false);
			return;
		}
		const controller = new AbortController();
		setLoading(true);
		setLogs([]);
		fetchTablePreview(
			apiUrl,
			srcInfo,
			tableName,
			jdbcValues,
			paramInputs,
			controller.signal,
			(line) => setLogs((current) => appendLogLines(current, [line])),
		).then((result) => {
			if (!controller.signal.aborted) {
				setPreview(result);
				setLoading(false);
			}
		});
		return () => {
			controller.abort();
		};
	}, [apiUrl, srcInfo, srcReady, jdbcValues, tableName, paramInputs]);

	return { preview, loading, logs };
};

// 選んだテーブルだけをresultフォームの値（-result.*）でconvertし、出力先ディレクトリを返す（DB出力なら空文字）。
// 読込条件はプレビューと同じ。失敗時は例外を投げる（呼び出し側のダイアログで表示する）
export const useDatasetTableConvert = () => {
	const { apiUrl } = useEnvironment();
	const { jdbcValues } = useJdbcConnectionState();
	const paramInputs = useParamInputs();
	return async (
		srcInfo: DatasetSrcInfo,
		tableName: string,
		result: FormValues,
		fetchOptions?: Pick<FetchParams, "signal" | "onLog">,
	): Promise<string> => {
		const response = await fetchData({
			endpoint: `${apiUrl}dataset-setting/table-convert`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: buildDatasetRequestBody(srcInfo, jdbcValues, {
					tableName,
					parameters: paramInputs,
					result,
				}),
			},
			...fetchOptions,
		});
		return response.text();
	};
};

export const useSaveDatasetSettings = () => {
	const updateResourceFiles = useUpdateResourceFiles("datasetSetting");
	return (name: string, input: DatasetSettings) =>
		updateResourceFiles("save", JSON.stringify({ name, input }));
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
		const controller = new AbortController();
		setLoading(true);
		loadDatasetSettings(apiUrl, fileName).then((result) => {
			if (!controller.signal.aborted) {
				setSettings(result);
				setLoading(false);
			}
		});
		return () => {
			controller.abort();
		};
	}, [fileName, apiUrl]);

	return { settings, loading };
};

export const useSrcInfoColumnNames = (srcInfo: DatasetSrcInfo): string[] => {
	const [columnNames, setColumnNames] = useState<string[]>([]);
	const { apiUrl } = useEnvironment();
	const { jdbcValues, connectionOk } = useJdbcConnectionState();
	const paramInputs = useParamInputs();

	const srcReady = isSrcReady(srcInfo, connectionOk);

	useEffect(() => {
		if (!srcReady) {
			setColumnNames([]);
			return;
		}
		const controller = new AbortController();
		loadAllColumnNames(
			apiUrl,
			srcInfo,
			jdbcValues,
			paramInputs,
			controller.signal,
		).then((names) => {
			if (!controller.signal.aborted) {
				setColumnNames(names);
			}
		});
		return () => {
			controller.abort();
		};
	}, [apiUrl, srcInfo, srcReady, jdbcValues, paramInputs]);

	return columnNames;
};

async function loadAllColumnNames(
	apiUrl: string,
	info: DatasetSrcInfo,
	jdbcValues: Record<string, string>,
	paramInputs: Record<string, string>,
	signal: AbortSignal,
): Promise<string[]> {
	const tableNames = await fetchTableNames(
		apiUrl,
		info,
		jdbcValues,
		paramInputs,
		signal,
	);
	const previews = await Promise.all(
		tableNames.map((tableName) =>
			fetchTablePreview(
				apiUrl,
				info,
				tableName,
				jdbcValues,
				paramInputs,
				signal,
			),
		),
	);
	const seen = new Set<string>();
	const allColumns: string[] = [];
	for (const preview of previews) {
		for (const col of preview.headers) {
			if (!seen.has(col)) {
				seen.add(col);
				allColumns.push(col);
			}
		}
	}
	return allColumns;
}

export const useColumnNamesFetcher = () => {
	const { apiUrl } = useEnvironment();
	const { jdbcValues } = useJdbcConnectionState();
	const paramInputs = useParamInputs();
	return async (
		srcInfo: DatasetSrcInfo,
		tableName: string,
		signal: AbortSignal,
		onLog?: (line: string) => void,
	): Promise<string[]> => {
		if (signal.aborted) {
			return [];
		}
		const preview = await fetchTablePreview(
			apiUrl,
			srcInfo,
			tableName,
			jdbcValues,
			paramInputs,
			signal,
			onLog,
		);
		return preview.headers;
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
			handleFetchError(getErrorMessage(ex), fetchParams);
			return DatasetSettings.create();
		});
}
