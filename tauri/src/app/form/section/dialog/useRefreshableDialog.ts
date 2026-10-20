import { useCallback, useEffect, useRef, useState } from "react";
import { useEnvironment } from "../../../../context/EnvironmentProvider";
import { useJobLog } from "../../../../hooks/useJobLog";
import { getErrorMessage } from "../../../../utils/fetchUtils";
import {
	collectFormValues,
	type FormValues,
} from "../../../../utils/formValues";
import {
	type DialogJob,
	type DialogLoadState,
	refreshDialogOptions,
} from "./refreshableDialogForm";

const EMPTY_VALUES: FormValues = {};

type RefreshableDialogOptions = {
	refreshEndpoint: string;
	formId: string;
	// refresh・実行で値へ付与するgenerateType（generate系のみ）
	generateType?: string;
	// 初期表示時にrefreshへ渡す値（参照が変わると再読込するため安定させること）
	initialValues?: FormValues;
};

// refresh系ダイアログの共通フロー: 初期読込・選択変更時の再構成・実行（検証→処理→エラー表示）
export function useRefreshableDialog<OPTIONS>({
	refreshEndpoint,
	formId,
	generateType,
	initialValues = EMPTY_VALUES,
}: RefreshableDialogOptions) {
	const { apiUrl } = useEnvironment();
	const [loadState, setLoadState] = useState<DialogLoadState<OPTIONS>>({
		status: "loading",
	});
	const [executing, setExecuting] = useState(false);
	const [executeError, setExecuteError] = useState("");
	const jobLog = useJobLog();
	const abortControllerRef = useRef<AbortController | null>(null);

	const withGenerateType = useCallback(
		(values: FormValues): FormValues =>
			generateType ? { ...values, "-generateType": generateType } : values,
		[generateType],
	);

	useEffect(() => {
		const controller = new AbortController();
		abortControllerRef.current = controller;
		refreshDialogOptions(
			apiUrl,
			refreshEndpoint,
			withGenerateType(initialValues),
			controller.signal,
			setLoadState,
		);
		return () => {
			controller.abort();
		};
	}, [apiUrl, refreshEndpoint, withGenerateType, initialValues]);

	const isAborted = () => abortControllerRef.current?.signal.aborted ?? false;

	// 実行をジョブとして送るためのオプション。ダイアログを閉じると（アンマウントでabort）sidecar側も中断する
	const job = (): DialogJob => {
		const signal = abortControllerRef.current?.signal;
		return { signal, onLog: jobLog.onLog(signal) };
	};

	const refresh = async () => {
		await refreshDialogOptions(
			apiUrl,
			refreshEndpoint,
			withGenerateType(collectFormValues(formId, false).values),
			abortControllerRef.current?.signal,
			setLoadState,
		);
	};

	// runが投げた例外はダイアログ内に表示する。成功時は呼び出し側が閉じる／遷移するためexecutingは戻さない
	const execute = async (run: (values: FormValues) => Promise<void>) => {
		const { values, validationError } = collectFormValues(formId, true);
		if (validationError) {
			return;
		}
		setExecuting(true);
		setExecuteError("");
		jobLog.reset();
		try {
			await run(values);
		} catch (ex) {
			if (!isAborted()) {
				setExecuteError(getErrorMessage(ex));
				setExecuting(false);
			}
		}
	};

	return {
		apiUrl,
		loadState,
		executing,
		executeError,
		logs: jobLog.logs,
		job,
		isAborted,
		refresh,
		execute,
		withGenerateType,
	};
}
