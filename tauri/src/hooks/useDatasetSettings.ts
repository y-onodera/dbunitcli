import { type Dispatch, type SetStateAction, useEffect, useState } from "react";
import { useEnvironment } from "../context/EnvironmentProvider";
import { useJdbcConnectionState } from "../context/JdbcConnectionProvider";
import { useParamInputs } from "../context/ParameterInputProvider";
import {
	type DatasetSrcInfo,
	isJdbcSrcType,
	srcTypeOf,
	withAllTables,
} from "../model/CommandOption";
import {
	DatasetSettings,
	type DatasetSettingsBuilder,
} from "../model/DatasetSettings";
import { fetchData, getErrorMessage } from "../utils/fetchUtils";
import type { FormValues } from "../utils/formValues";
import type { JobContext } from "./useJob";
import { appendLogLines } from "./useJobLog";
import {
	type DatasetSettingKind,
	type ResourceParser,
	useResourceLoad,
	useSaveResource,
} from "./useResourceFiles";

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
		inlineSource: info.inlineSource,
		...extra,
	});
}

// ソース未指定（inlineSourceならその内容が空）・none・JDBC未接続のJDBC系srcではsidecarへ問い合わせない
function isSrcReady(info: DatasetSrcInfo, connectionOk: boolean): boolean {
	const srcType = srcTypeOf(info);
	const hasSrc =
		info.inlineSource === undefined
			? !!info.values.src
			: info.inlineSource.trim() !== "";
	if (!hasSrc || !srcType || srcType === "none") {
		return false;
	}
	return !isJdbcSrcType(srcType) || connectionOk;
}

// Previewの読込状態。失敗したらdataは空にし、errorにメッセージを持つ。logsは読込中に届いたログ（失敗後も残す）
type Load<T> = { data: T; loading: boolean; error: string; logs: string[] };

// 読み込んでいない状態。dataは失敗したときの空の値にも使う
const idleLoad = <T>(data: T): Load<T> => ({
	data,
	loading: false,
	error: "",
	logs: [],
});

// 読込を始め、結果をstateへ反映する（中断後の結果は捨てる）。前回の結果は読込中も表示したまま、エラーとログだけ消す。
// 取得関数は失敗を投げ、扱いは呼び出し元が決める（Previewはエラーとログを表示し、入力候補は空にする）
function startLoad<T>(
	fetch: (onLog: (line: string) => void) => Promise<T>,
	signal: AbortSignal,
	idle: Load<T>,
	setLoad: Dispatch<SetStateAction<Load<T>>>,
) {
	setLoad((load) => ({ ...load, loading: true, error: "", logs: [] }));
	fetch((line) =>
		setLoad((load) => ({ ...load, logs: appendLogLines(load.logs, [line]) })),
	).then(
		(data) => {
			if (!signal.aborted) {
				setLoad((load) => ({ ...load, data, loading: false }));
			}
		},
		(ex) => {
			if (!signal.aborted) {
				setLoad((load) => ({
					...load,
					data: idle.data,
					loading: false,
					error: getErrorMessage(ex),
				}));
			}
		},
	);
}

// dataset-setting/配下のエンドポイントへsrcとextraを送り、JSONの応答を返す
async function fetchDatasetJson<T>(
	apiUrl: string,
	endpoint: string,
	info: DatasetSrcInfo,
	jdbcValues: Record<string, string>,
	paramInputs: Record<string, string>,
	extra: Record<string, string>,
	signal?: AbortSignal,
	onLog?: (line: string) => void,
): Promise<T> {
	const fetchParams = {
		endpoint: `${apiUrl}dataset-setting/${endpoint}`,
		options: {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: buildDatasetRequestBody(info, jdbcValues, {
				...extra,
				parameters: paramInputs,
			}),
		},
		signal,
		onLog,
	};
	return fetchData(fetchParams).then((r) => r.json());
}

// srcの読込準備ができていてenabledなら、endpointへextraを付けて読み込む。srcInfoやextraが変わるたびに読み直す。
// extraは文字列だけの値にする（毎回作り直されるオブジェクトでも読み直さないよう、JSON文字列にして比較する）。
// withLogs: 読込中のログも受け取る
function useDatasetLoad<T>(
	srcInfo: DatasetSrcInfo,
	endpoint: string,
	extra: Record<string, string>,
	enabled: boolean,
	idle: Load<T>,
	withLogs: boolean,
): Load<T> {
	const [load, setLoad] = useState<Load<T>>(idle);
	const { apiUrl } = useEnvironment();
	const { jdbcValues, connectionOk } = useJdbcConnectionState();
	const paramInputs = useParamInputs();

	const ready = enabled && isSrcReady(srcInfo, connectionOk);
	const extraKey = JSON.stringify(extra);

	useEffect(() => {
		if (!ready) {
			setLoad(idle);
			return;
		}
		const controller = new AbortController();
		startLoad(
			(onLog) =>
				fetchDatasetJson<T>(
					apiUrl,
					endpoint,
					srcInfo,
					jdbcValues,
					paramInputs,
					JSON.parse(extraKey),
					controller.signal,
					withLogs ? onLog : undefined,
				),
			controller.signal,
			idle,
			setLoad,
		);
		return () => {
			controller.abort();
		};
	}, [
		apiUrl,
		endpoint,
		srcInfo,
		ready,
		jdbcValues,
		paramInputs,
		extraKey,
		idle,
		withLogs,
	]);

	return load;
}

const IDLE_TABLE_NAMES = idleLoad<string[]>([]);

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
	const { data, loading, logs, error } = useDatasetLoad(
		srcInfo,
		"table-names",
		{},
		true,
		IDLE_TABLE_NAMES,
		withLogs,
	);
	return { tableNames: data, loading, logs, error };
};

// rowsは先頭の一部だけ（sidecarが件数を制限する）。totalRowsが対象テーブルの全行数
export type TablePreview = {
	headers: string[];
	rows: string[][];
	totalRows: number;
};

const IDLE_PREVIEW = idleLoad<TablePreview | null>(null);

// generateの-unitSettingをsrcのテーブル1つに適用した結果のテーブル1つ分
export type UnitTablePreview = TablePreview & { tableName: string };

const IDLE_UNIT_PREVIEWS = idleLoad<UnitTablePreview[]>([]);

// srcのtableNameのテーブルにunitSettingを適用してできたテーブルごとのプレビュー（先頭がtableName自身）
export const useDatasetUnitTablePreview = (
	srcInfo: DatasetSrcInfo,
	tableName: string,
	unitSetting: string,
	unitSettingEncoding: string,
): {
	previews: UnitTablePreview[];
	loading: boolean;
	logs: string[];
	error: string;
} => {
	const { data, loading, logs, error } = useDatasetLoad(
		srcInfo,
		"unit-table-preview",
		{ tableName, unitSetting, unitSettingEncoding },
		!!tableName && !!unitSetting,
		IDLE_UNIT_PREVIEWS,
		true,
	);
	return { previews: data, loading, logs, error };
};

export const useDatasetTablePreview = (
	srcInfo: DatasetSrcInfo,
	tableName: string,
): {
	preview: TablePreview | null;
	loading: boolean;
	logs: string[];
	error: string;
} => {
	const { data, loading, logs, error } = useDatasetLoad(
		srcInfo,
		"table-preview",
		{ tableName },
		!!tableName,
		IDLE_PREVIEW,
		true,
	);
	return { preview: data, loading, logs, error };
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
		context: JobContext,
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
			...context,
		});
		return response.text();
	};
};

export const useSaveDatasetSettings = (
	kind: DatasetSettingKind = "datasetSetting",
) => {
	return useSaveResource<DatasetSettings>(kind);
};

const DATASET_SETTINGS_PARSER: ResourceParser<DatasetSettings> = {
	empty: () => DatasetSettings.create(),
	parse: (json) => DatasetSettings.build(json as DatasetSettingsBuilder),
};

export const useDatasetSettingsData = (
	fileName: string,
	kind: DatasetSettingKind = "datasetSetting",
): { settings: DatasetSettings; loading: boolean } => {
	const { data, loading } = useResourceLoad(
		kind,
		fileName,
		DATASET_SETTINGS_PARSER,
	);
	return { settings: data, loading };
};

const IDLE_ALL_COLUMNS = idleLoad<Record<string, string[]>>({});

// 入力候補なので、settingの対象外のテーブルのカラムも含め、取得できなければ候補なしとする
export const useSrcInfoColumnNames = (srcInfo: DatasetSrcInfo): string[] => {
	const { data } = useDatasetLoad(
		withAllTables(srcInfo),
		"table-columns",
		{ tableName: "" },
		true,
		IDLE_ALL_COLUMNS,
		false,
	);
	return [...new Set(Object.values(data).flat())];
};

// sidecarのDatasetTableNamesRequestDto.INLINE_SOURCE_TABLE_NAMEと揃える
export const INLINE_SOURCE_TABLE_NAME = "inlineSource";

// テーブル名→列名。tableNameを指定すればそのテーブルだけを読む。失敗は投げる
async function fetchTableColumns(
	apiUrl: string,
	info: DatasetSrcInfo,
	tableName: string,
	jdbcValues: Record<string, string>,
	paramInputs: Record<string, string>,
	{ signal, onLog }: JobContext,
): Promise<Record<string, string[]>> {
	return fetchDatasetJson(
		apiUrl,
		"table-columns",
		info,
		jdbcValues,
		paramInputs,
		{ tableName },
		signal,
		onLog,
	);
}

export const useColumnNamesFetcher = () => {
	const { apiUrl } = useEnvironment();
	const { jdbcValues } = useJdbcConnectionState();
	const paramInputs = useParamInputs();
	return async (
		srcInfo: DatasetSrcInfo,
		tableName: string,
		context: JobContext,
	): Promise<string[]> => {
		// 失敗は投げる（呼び出し元がエラーと実行中のログを表示する）。
		// 入力候補なので、settingの対象外のテーブルでも取得する
		const columns = await fetchTableColumns(
			apiUrl,
			withAllTables(srcInfo),
			tableName,
			jdbcValues,
			paramInputs,
			context,
		);
		return columns[tableName] ?? [];
	};
};

// csvqのCSVREADの式1つの列名（CSVREADの列の選択用）。実行時と同じくcsvqのSQLとして読むため、CSVREADのオプションも反映される。失敗は投げる
export const useCsvColumnNamesFetcher = () => {
	const { apiUrl } = useEnvironment();
	const paramInputs = useParamInputs();
	return async (
		csvReadExpression: string,
		context: JobContext,
	): Promise<string[]> => {
		const columns = await fetchTableColumns(
			apiUrl,
			{
				values: { srcType: "csvq" },
				inlineSource: `SELECT * FROM ${csvReadExpression}`,
			},
			INLINE_SOURCE_TABLE_NAME,
			{},
			paramInputs,
			context,
		);
		return columns[INLINE_SOURCE_TABLE_NAME] ?? [];
	};
};
