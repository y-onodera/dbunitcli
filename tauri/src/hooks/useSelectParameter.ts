import { useEffect, useState } from "react";
import { useEnvironment } from "../context/EnvironmentProvider";
import {
	type FieldErrors,
	useSetFieldErrors,
	useVisibleFields,
} from "../context/FieldErrorProvider";
import {
	useSelectParameter,
	useSetSelectParameterState,
} from "../context/SelectParameterProvider";
import { useSetParameterList } from "../context/WorkspaceResourcesProvider";
import type {
	Command,
	Options,
	ParameterizeOptions,
} from "../model/SelectParameter";
import { SelectParameter } from "../model/SelectParameter";
import {
	fetchAndUpdate,
	fetchData,
	getErrorMessage,
	handleFetchError,
	isAbortError,
	logFetchError,
	OptionValidationError,
	type OptionViolation,
} from "../utils/fetchUtils";

export type Running = {
	resultMessage: string;
	resultDir: string;
	// 入力欄に表示できなかった検証エラー
	resultDetails?: string[];
	// 失敗したとき（resultMessageはエラーメッセージ）
	failed?: boolean;
};

const postJsonParams = (endpoint: string, body: object) => ({
	endpoint,
	options: {
		method: "POST",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify(body),
	},
});

export const useAddParameter = (command: string) => {
	const setParameterList = useSetParameterList();
	const { apiUrl } = useEnvironment();
	const key = command.toLowerCase();
	return () =>
		fetchAndUpdate<string[]>(
			{ endpoint: `${apiUrl + key}/add`, options: { method: "GET" } },
			(parameters) =>
				setParameterList((current) => current.replace(key, parameters)),
		);
};

export const useParameterActions = (command: string, name: string) => {
	const setParameterList = useSetParameterList();
	const { apiUrl } = useEnvironment();
	const setParameter = useSetSelectParameterState();
	const key = command.toLowerCase();

	const postAndUpdateList = (
		action: string,
		body: object,
		onUpdated?: () => void,
	) =>
		fetchAndUpdate<string[]>(
			postJsonParams(`${apiUrl + key}/${action}`, body),
			(parameters) => {
				setParameterList((current) => current.replace(key, parameters));
				onUpdated?.();
			},
		);

	const handleDelete = () => postAndUpdateList("delete", { name });
	const handleCopy = () => postAndUpdateList("copy", { name });
	// 応答を待つ間に選択が変わりうるので、選択中のパラメータは更新時点の値で判定する
	const handleRename = (newName: string) =>
		postAndUpdateList("rename", { oldName: name, newName }, () =>
			setParameter((current) =>
				current.command === key && current.name === name
					? new SelectParameter(
							current.options,
							current.command,
							newName,
							current.inputParam,
						)
					: current,
			),
		);

	return { handleDelete, handleCopy, handleRename };
};
// sidecarのParameterizeFileDto
export type ParameterizeFile = {
	option: string;
	path: string;
	usedBy: string[];
};

export const useLoadParameterizeFiles = () => {
	const { apiUrl } = useEnvironment();
	return (name: string, onLoad: (files: ParameterizeFile[]) => void) =>
		fetchAndUpdate<ParameterizeFile[]>(
			postJsonParams(`${apiUrl}parameterize/files`, { name }),
			onLoad,
		);
};

export const useDeleteParameterize = () => {
	const { apiUrl } = useEnvironment();
	const setParameterList = useSetParameterList();
	return (name: string, deleteFiles: string[]) =>
		fetchAndUpdate<string[]>(
			postJsonParams(`${apiUrl}parameterize/delete`, { name, deleteFiles }),
			(parameters) =>
				setParameterList((current) =>
					current.replace("parameterize", parameters),
				),
		);
};

export type ParameterizeCandidate = { key: string; value: string };

// parameterizeで作るパラメータデータの形式。encodingはcsvのときだけ使われる
export type ParamSource = { type: "csv" | "xlsx"; encoding: string };

// 取得前はnull（取得に失敗したときは空の一覧にする）
export const useParameterizeCandidates = (
	command: string,
	name: string,
): ParameterizeCandidate[] | null => {
	const { apiUrl } = useEnvironment();
	const [candidates, setCandidates] = useState<ParameterizeCandidate[] | null>(
		null,
	);
	useEffect(() => {
		const controller = new AbortController();
		// 別のパラメータの候補を出したままにしない
		setCandidates(null);
		fetchAndUpdate<ParameterizeCandidate[]>(
			{
				...postJsonParams(
					`${apiUrl + command.toLowerCase()}/parameterize/candidates`,
					{ name },
				),
				signal: controller.signal,
			},
			(data) => {
				if (!controller.signal.aborted) {
					setCandidates(data);
				}
			},
		).then((result) => {
			if (result === "failed" && !controller.signal.aborted) {
				setCandidates([]);
			}
		});
		return () => controller.abort();
	}, [apiUrl, command, name]);
	return candidates;
};

export const useParameterizeFrom = () => {
	const setParameter = useSetSelectParameterState();
	const setParameterList = useSetParameterList();
	const environment = useEnvironment();
	return async (
		sourceCommand: string,
		name: string,
		parameterizeKeys: string[],
		paramSource: ParamSource,
	) => {
		const fetchParams = {
			endpoint: `${environment.apiUrl + sourceCommand.toLowerCase()}/parameterize`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({
					name,
					parameterizeKeys,
					paramType: paramSource.type,
					paramEncoding: paramSource.encoding,
				}),
			},
		};
		try {
			const response = await fetchData(fetchParams);
			const parameter: ParameterizeOptions = await response.json();
			setParameter(new SelectParameter(parameter, "parameterize", name));
			setParameterList((current) => {
				if (current.parameterize.includes(name)) {
					return current;
				}
				return current.replace("parameterize", [...current.parameterize, name]);
			});
		} catch (ex) {
			handleFetchError(getErrorMessage(ex), fetchParams);
		}
	};
};
export const useLoadSelectParameter = () => {
	const setParameter = useSetSelectParameterState();
	const environment = useEnvironment();
	return async (command: Command, name: string) => {
		const fetchParams = {
			endpoint: `${environment.apiUrl + command.toLowerCase()}/load`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ name }),
			},
		};
		try {
			const response = await fetchData(fetchParams);
			setParameter(new SelectParameter(await response.json(), command, name));
		} catch (ex) {
			handleFetchError(getErrorMessage(ex), fetchParams);
		}
	};
};

export const useRefreshSelectParameter = (command: string) => {
	const setParameter = useSetSelectParameterState();
	const environment = useEnvironment();
	return async (values: { [k: string]: FormDataEntryValue }) => {
		const fetchParams = {
			endpoint: `${environment.apiUrl + command.toLowerCase()}/refresh`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify(values),
			},
		};
		try {
			const response = await fetchData(fetchParams);
			const parameter: Options = await response.json();
			setParameter(
				(current) =>
					new SelectParameter(
						parameter,
						current.command,
						current.name,
						current.inputParam,
					),
			);
		} catch (ex) {
			handleFetchError(getErrorMessage(ex), fetchParams);
		}
	};
};

type ParameterAction = "save" | "exec" | "shell";

const parseResponse = async (
	action: ParameterAction,
	response: Response,
): Promise<Running> => {
	if (action === "save") {
		return { resultMessage: "Save Success", resultDir: "" };
	}
	const resultDir = await response.text();
	return {
		resultMessage:
			action === "exec" ? "Execution Success" : "Save Shell Success",
		resultDir,
	};
};

const INVALID_OPTION_MESSAGE = "Invalid Option";

// 同じ文言は一度だけ出す（表示時のkeyにも使う）
const addUnique = (messages: string[], message: string) => {
	if (!messages.includes(message)) {
		messages.push(message);
	}
};

// 違反は関わる全ての入力欄に持たせ、折りたたんだ欄も展開すれば欄の下に出す。表示中の欄が無い違反は結果ダイアログにもまとめて出す
const toFieldErrors = (
	violations: OptionViolation[],
	visibleFields: Set<string>,
) => {
	const fieldErrors: FieldErrors = {};
	const unmatched: string[] = [];
	for (const violation of violations) {
		if (!violation.options.some((name) => visibleFields.has(name))) {
			addUnique(unmatched, violation.message);
		}
		for (const name of violation.options) {
			fieldErrors[name] ??= [];
			addUnique(fieldErrors[name], violation.message);
		}
	}
	return { fieldErrors, unmatched };
};

// 結果（失敗時はエラーメッセージ）を返す。中断されたときはnull
const useParameterAction = () => {
	const parameter = useSelectParameter();
	const environment = useEnvironment();
	const setFieldErrors = useSetFieldErrors();
	const visibleFields = useVisibleFields();
	return async (
		action: ParameterAction,
		extraBody: Record<string, unknown>,
		signal?: AbortSignal,
		onLog?: (line: string) => void,
	): Promise<Running | null> => {
		const fetchParams = {
			endpoint: `${environment.apiUrl + parameter.command}/${action}`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ name: parameter.name, ...extraBody }),
			},
			signal,
			onLog,
		};
		// 空のままなら同じ値を返して、全入力欄の再描画を避ける
		setFieldErrors((prev) => (Object.keys(prev).length === 0 ? prev : {}));
		try {
			const response = await fetchData(fetchParams);
			return await parseResponse(action, response);
		} catch (ex) {
			if (isAbortError(ex)) {
				return null;
			}
			if (ex instanceof OptionValidationError) {
				const { fieldErrors, unmatched } = toFieldErrors(
					ex.violations,
					visibleFields,
				);
				setFieldErrors(fieldErrors);
				return {
					resultMessage: INVALID_OPTION_MESSAGE,
					resultDir: "",
					resultDetails: unmatched,
					failed: true,
				};
			}
			// 結果ダイアログにメッセージを出すので、alertは出さない
			const errorMessage = getErrorMessage(ex);
			logFetchError(errorMessage, fetchParams);
			return { resultMessage: errorMessage, resultDir: "", failed: true };
		}
	};
};

export const useSaveParameter = () => {
	const execute = useParameterAction();
	return async (
		input: { [k: string]: FormDataEntryValue },
		signal?: AbortSignal,
	) => execute("save", { input }, signal);
};

export const useExecParameter = () => {
	const execute = useParameterAction();
	return async (
		input: { [k: string]: FormDataEntryValue },
		signal?: AbortSignal,
		onLog?: (line: string) => void,
	) => execute("exec", { input }, signal, onLog);
};

export const useSaveShell = () => {
	const execute = useParameterAction();
	return async (signal?: AbortSignal) => execute("shell", {}, signal);
};
