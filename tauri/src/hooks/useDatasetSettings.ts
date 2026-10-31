import { useEffect, useState } from "react";
import { useEnvironment } from "../context/EnvironmentProvider";
import { useJdbcConnectionState } from "../context/JdbcConnectionProvider";
import { useParamInputs } from "../context/ParameterInputProvider";
import {
	type DatasetSrcInfo,
	srcTypeOf,
	withAllTables,
} from "../model/CommandOption";
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

// jdbcは接続テストに成功した値を優先する
function buildDatasetRequestBody(
	info: DatasetSrcInfo,
	jdbcValues: Record<string, string>,
	extra?: Record<string, unknown>,
): string {
	return JSON.stringify({
		srcOptions: { ...info.values, ...jdbcValues },
		inlineSetting: info.inlineSetting,
		inlineXlsxSchema: info.inlineXlsxSchema,
		...extra,
	});
}

// ソース未指定・none・JDBC未接続のsqlではsidecarへ問い合わせない
function isSrcReady(info: DatasetSrcInfo, connectionOk: boolean): boolean {
	const srcType = srcTypeOf(info);
	if (!info.values.src || !srcType || srcType === "none") {
		return false;
	}
	return srcType !== "sql" || connectionOk;
}

// Previewの読込状態。失敗したらdataは空にし、errorにメッセージを持つ
type Load<T> = { data: T; loading: boolean; error: string };

// 読込の結果をstateへ反映する（中断後の結果は捨てる）。
// 取得関数は失敗を投げ、扱いは呼び出し元が決める（Previewはエラーとログを表示し、入力候補は空にする）
function settleLoad<T>(
	promise: Promise<T>,
	signal: AbortSignal,
	empty: T,
	setLoad: (load: Load<T>) => void,
) {
	promise.then(
		(data) => {
			if (!signal.aborted) {
				setLoad({ data, loading: false, error: "" });
			}
		},
		(ex) => {
			if (!signal.aborted) {
				setLoad({ data: empty, loading: false, error: getErrorMessage(ex) });
			}
		},
	);
}

// 読込を始める（前回の結果は読込中も表示したまま、エラーだけ消す）
const startLoad = <T>(load: Load<T>): Load<T> => ({
	...load,
	loading: true,
	error: "",
});

async function fetchTableNames(
	apiUrl: string,
	info: DatasetSrcInfo,
	jdbcValues: Record<string, string>,
	paramInputs: Record<string, string>,
	signal?: AbortSignal,
	onLog?: (line: string) => void,
): Promise<string[]> {
	if (!info.values.src) {
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
	return fetchData(fetchParams).then((r) => r.json());
}

const IDLE_TABLE_NAMES: Load<string[]> = {
	data: [],
	loading: false,
	error: "",
};

// withLogs: 読込中のログも受け取る（読込中の表示に使う場合）
export const useDatasetTableNames = (
	srcInfo: DatasetSrcInfo,
	withLogs = false,
): {
	tableNames: string[];
	loading: boolean;
	logs: string[];
	error: string;
} => {
	const [load, setLoad] = useState<Load<string[]>>(IDLE_TABLE_NAMES);
	const [logs, setLogs] = useState<string[]>([]);
	const { apiUrl } = useEnvironment();
	const { jdbcValues, connectionOk } = useJdbcConnectionState();
	const paramInputs = useParamInputs();

	const srcReady = isSrcReady(srcInfo, connectionOk);

	useEffect(() => {
		if (!srcReady) {
			setLoad(IDLE_TABLE_NAMES);
			return;
		}
		const controller = new AbortController();
		setLoad(startLoad);
		setLogs([]);
		settleLoad(
			fetchTableNames(
				apiUrl,
				srcInfo,
				jdbcValues,
				paramInputs,
				controller.signal,
				withLogs
					? (line) => setLogs((current) => appendLogLines(current, [line]))
					: undefined,
			),
			controller.signal,
			[],
			setLoad,
		);
		return () => {
			controller.abort();
		};
	}, [apiUrl, srcInfo, srcReady, jdbcValues, paramInputs, withLogs]);

	return {
		tableNames: load.data,
		loading: load.loading,
		logs,
		error: load.error,
	};
};

// rowsは先頭の一部だけ（sidecarが件数を制限する）。totalRowsが対象テーブルの全行数
export type TablePreview = {
	headers: string[];
	rows: string[][];
	totalRows: number;
};

const EMPTY_PREVIEW: TablePreview = { headers: [], rows: [], totalRows: 0 };
const IDLE_PREVIEW: Load<TablePreview | null> = {
	data: null,
	loading: false,
	error: "",
};

async function fetchTablePreview(
	apiUrl: string,
	info: DatasetSrcInfo,
	tableName: string,
	jdbcValues: Record<string, string>,
	paramInputs: Record<string, string>,
	signal?: AbortSignal,
	onLog?: (line: string) => void,
): Promise<TablePreview> {
	if (!info.values.src || !tableName) {
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
	return fetchData(fetchParams).then((r) => r.json());
}

export const useDatasetTablePreview = (
	srcInfo: DatasetSrcInfo,
	tableName: string,
): {
	preview: TablePreview | null;
	loading: boolean;
	logs: string[];
	error: string;
} => {
	const [load, setLoad] = useState<Load<TablePreview | null>>(IDLE_PREVIEW);
	const [logs, setLogs] = useState<string[]>([]);
	const { apiUrl } = useEnvironment();
	const { jdbcValues, connectionOk } = useJdbcConnectionState();
	const paramInputs = useParamInputs();

	const srcReady = isSrcReady(srcInfo, connectionOk);

	useEffect(() => {
		if (!srcReady || !tableName) {
			setLoad(IDLE_PREVIEW);
			return;
		}
		const controller = new AbortController();
		setLoad(startLoad);
		setLogs([]);
		settleLoad<TablePreview | null>(
			fetchTablePreview(
				apiUrl,
				srcInfo,
				tableName,
				jdbcValues,
				paramInputs,
				controller.signal,
				(line) => setLogs((current) => appendLogLines(current, [line])),
			),
			controller.signal,
			null,
			setLoad,
		);
		return () => {
			controller.abort();
		};
	}, [apiUrl, srcInfo, srcReady, jdbcValues, tableName, paramInputs]);

	return {
		preview: load.data,
		loading: load.loading,
		logs,
		error: load.error,
	};
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

// 入力候補なので、settingの対象外のテーブルのカラムも含める
async function loadAllColumnNames(
	apiUrl: string,
	srcInfo: DatasetSrcInfo,
	jdbcValues: Record<string, string>,
	paramInputs: Record<string, string>,
	signal: AbortSignal,
): Promise<string[]> {
	const info = withAllTables(srcInfo);
	// 入力候補なので、取得できなかったテーブルは候補なしとして続ける
	const tableNames: string[] = await fetchTableNames(
		apiUrl,
		info,
		jdbcValues,
		paramInputs,
		signal,
	).catch(() => []);
	const previews = await Promise.all(
		tableNames.map((tableName) =>
			fetchTablePreview(
				apiUrl,
				info,
				tableName,
				jdbcValues,
				paramInputs,
				signal,
			).catch(() => EMPTY_PREVIEW),
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
		// 失敗は投げる（呼び出し元がエラーと実行中のログを表示する）
		if (signal.aborted) {
			return [];
		}
		// 入力候補なので、settingの対象外のテーブルでも取得する
		const preview = await fetchTablePreview(
			apiUrl,
			withAllTables(srcInfo),
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
