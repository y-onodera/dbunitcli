import { useEffect, useRef, useState } from "react";
import { useEnvironment } from "../../../../context/EnvironmentProvider";
import { type JobContext, useJob } from "../../../../hooks/useJob";
import {
	collectFormValues,
	type FormValues,
} from "../../../../utils/formValues";
import {
	type DialogLoadState,
	refreshDialogOptions,
} from "./refreshableDialogForm";

const EMPTY_VALUES: FormValues = {};

// useEffect内から呼ぶため、依存配列に関数を含めずに済むようモジュールレベルで定義する
function addGenerateType(
	values: FormValues,
	generateType: string | undefined,
): FormValues {
	return generateType ? { ...values, "-generateType": generateType } : values;
}

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
	const job = useJob();
	const abortControllerRef = useRef<AbortController | null>(null);

	useEffect(() => {
		const controller = new AbortController();
		abortControllerRef.current = controller;
		refreshDialogOptions(
			apiUrl,
			refreshEndpoint,
			addGenerateType(initialValues, generateType),
			controller.signal,
			setLoadState,
		);
		return () => {
			controller.abort();
		};
	}, [apiUrl, refreshEndpoint, generateType, initialValues]);

	const withGenerateType = (values: FormValues): FormValues =>
		addGenerateType(values, generateType);

	const refresh = async () => {
		await refreshDialogOptions(
			apiUrl,
			refreshEndpoint,
			withGenerateType(collectFormValues(formId, false).values),
			abortControllerRef.current?.signal,
			setLoadState,
		);
	};

	// runが投げた例外はダイアログ内にログと一緒に表示する。ダイアログを閉じると（アンマウントでabort）sidecar側も中断する
	const execute = async (
		run: (values: FormValues, context: JobContext) => Promise<void>,
	) => {
		const { values, validationError } = collectFormValues(formId, true);
		if (validationError) {
			return;
		}
		await job.run((context) => run(values, context));
	};

	return {
		apiUrl,
		loadState,
		executing: job.running,
		executeError: job.error,
		logs: job.logs,
		refresh,
		execute,
		withGenerateType,
	};
}
