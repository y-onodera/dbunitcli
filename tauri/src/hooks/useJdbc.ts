import { useEnvironment } from "../context/EnvironmentProvider";
import {
	type FetchParams,
	fetchData,
	getErrorMessage,
	handleFetchError,
	logFetchError,
	postJsonParams,
} from "../utils/fetchUtils";
import type { JobContext } from "./useJob";
import { useUpdateResourceFiles } from "./useResourceFiles";

// タイムアウト時はsidecarへcancelも送られ、sidecar側の処理も中断する
const JDBC_TIMEOUT_MS = 60_000;

function toJdbcRequestBody(jdbcValues: Record<string, string>) {
	return {
		url: jdbcValues.jdbcUrl ?? "",
		user: jdbcValues.jdbcUser ?? "",
		pass: jdbcValues.jdbcPass ?? "",
		properties: jdbcValues.jdbcProperties ?? "",
	};
}

// 一覧を取得する。失敗は呼び出し元がログと一緒に表示するため投げる
async function fetchJdbcList(params: FetchParams): Promise<string[]> {
	try {
		const response = await fetchData(params);
		return (await response.json()) as string[];
	} catch (e) {
		logFetchError(getErrorMessage(e), params);
		throw e;
	}
}

export const useJdbcTables = () => {
	const { apiUrl } = useEnvironment();
	return (
		jdbcValues: Record<string, string>,
		context: JobContext,
	): Promise<string[]> =>
		fetchJdbcList({
			...postJsonParams(`${apiUrl}jdbc/tables`, toJdbcRequestBody(jdbcValues)),
			timeoutMs: JDBC_TIMEOUT_MS,
			...context,
		});
};

export const useJdbcColumns = () => {
	const { apiUrl } = useEnvironment();
	return (
		jdbcValues: Record<string, string>,
		table: string,
		context: JobContext,
	): Promise<string[]> =>
		fetchJdbcList({
			...postJsonParams(`${apiUrl}jdbc/columns`, {
				...toJdbcRequestBody(jdbcValues),
				table,
			}),
			timeoutMs: JDBC_TIMEOUT_MS,
			...context,
		});
};

export const useJdbcConnectionTest = () => {
	const { apiUrl } = useEnvironment();
	return async (
		jdbcValues: Record<string, string>,
	): Promise<{ success: boolean; message: string } | null> => {
		const params = {
			...postJsonParams(`${apiUrl}jdbc/test`, toJdbcRequestBody(jdbcValues)),
			timeoutMs: JDBC_TIMEOUT_MS,
		};
		try {
			const response = await fetchData(params);
			return (await response.json()) as { success: boolean; message: string };
		} catch (e) {
			handleFetchError(getErrorMessage(e), params);
			return null;
		}
	};
};

export const useSaveJdbcProperties = () => {
	const updateResourceFiles = useUpdateResourceFiles("jdbc");
	// overwriteなら読込と同じ探索で見つかったファイルへ上書きし、そうでなければ規定フォルダ基準で新規保存する
	return (
		name: string,
		jdbcValues: Record<string, string>,
		overwrite: boolean,
	) =>
		updateResourceFiles(
			"save-properties",
			JSON.stringify({
				name,
				overwrite,
				input: toJdbcRequestBody(jdbcValues),
			}),
		);
};
