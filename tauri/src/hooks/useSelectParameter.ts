import { useEnvironment } from "../context/EnvironmentProvider";
import {
	type FieldErrors,
	useSetFieldErrors,
	useVisibleFields,
} from "../context/FieldErrorProvider";
import {
	useSelectParameter,
	useSetSelectParameter,
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

export const useAddParameter = (command: string) => {
	const setParameter = useSetParameterList();
	const environment = useEnvironment();
	return async () => {
		const fetchParams = {
			endpoint: `${environment.apiUrl + command.toLowerCase()}/add`,
			options: {
				method: "GET",
				headers: { "Content-Type": "application/json" },
			},
		};
		await fetchData(fetchParams)
			.then((response) => response.json())
			.then((parameters: string[]) => {
				setParameter((current) =>
					current.replace(command.toLowerCase(), parameters),
				);
			})
			.catch((ex) => handleFetchError(getErrorMessage(ex), fetchParams));
	};
};
export const useParameterActions = (command: string, name: string) => {
	const setParameterList = useSetParameterList();
	const { apiUrl } = useEnvironment();
	const parameter = useSelectParameter();
	const setParameter = useSetSelectParameter();

	const postAndUpdateList = async (action: string) => {
		const fetchParams = {
			endpoint: `${apiUrl + command.toLowerCase()}/${action}`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ name }),
			},
		};
		await fetchData(fetchParams)
			.then((response) => response.json())
			.then((parameters: string[]) => {
				setParameterList((current) =>
					current.replace(command.toLowerCase(), parameters),
				);
			})
			.catch((ex) => handleFetchError(getErrorMessage(ex), fetchParams));
	};

	const handleDelete = () => postAndUpdateList("delete");
	const handleCopy = () => postAndUpdateList("copy");
	const handleRename = async (newName: string) => {
		const fetchParams = {
			endpoint: `${apiUrl + command.toLowerCase()}/rename`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ oldName: name, newName }),
			},
		};
		await fetchData(fetchParams)
			.then((response) => response.json())
			.then((parameters: string[]) => {
				setParameterList((current) =>
					current.replace(command.toLowerCase(), parameters),
				);
				if (
					parameter.command === command.toLowerCase() &&
					parameter.name === name
				) {
					setParameter(parameter.options, parameter.command, newName);
				}
			})
			.catch((ex) => handleFetchError(getErrorMessage(ex), fetchParams));
	};

	return { handleDelete, handleCopy, handleRename };
};
export const useParameterizeFrom = () => {
	const setParameter = useSetSelectParameterState();
	const setParameterList = useSetParameterList();
	const environment = useEnvironment();
	return async (sourceCommand: string, name: string) => {
		const fetchParams = {
			endpoint: `${environment.apiUrl + sourceCommand.toLowerCase()}/parameterize`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ name }),
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
	return messages;
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
			fieldErrors[name] = addUnique(fieldErrors[name] ?? [], violation.message);
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
