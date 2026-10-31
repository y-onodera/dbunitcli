import { useEnvironment } from "../context/EnvironmentProvider";
import {
	type FetchParams,
	fetchData,
	getErrorMessage,
	handleFetchError,
	logFetchError,
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

// 一覧を取得する。失敗は呼び出し元がログと一緒に表示するため投げる（自分で中断したときはコンソールにも出さない）
async function fetchJdbcList(params: FetchParams): Promise<string[]> {
	try {
		const response = await fetchData(params);
		return (await response.json()) as string[];
	} catch (e) {
		if (!params.signal?.aborted) {
			logFetchError(getErrorMessage(e), params);
		}
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
			endpoint: `${apiUrl}jdbc/tables`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify(toJdbcRequestBody(jdbcValues)),
			},
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
			endpoint: `${apiUrl}jdbc/columns`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ ...toJdbcRequestBody(jdbcValues), table }),
			},
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
			endpoint: `${apiUrl}jdbc/test`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify(toJdbcRequestBody(jdbcValues)),
			},
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
	return (name: string, jdbcValues: Record<string, string>) =>
		updateResourceFiles(
			"save-properties",
			JSON.stringify({ name, input: toJdbcRequestBody(jdbcValues) }),
		);
};
