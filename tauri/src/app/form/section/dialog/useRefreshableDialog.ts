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
	// 最後に始めた読込だけを反映する。新しい読込を始めたら前の読込を中断し、アンマウントでも中断する
	const loadControllerRef = useRef<AbortController | null>(null);

	useEffect(() => {
		loadControllerRef.current?.abort();
		const controller = new AbortController();
		loadControllerRef.current = controller;
		refreshDialogOptions(
			apiUrl,
			refreshEndpoint,
			addGenerateType(initialValues, generateType),
			controller.signal,
			setLoadState,
		);
		return () => {
			loadControllerRef.current?.abort();
		};
	}, [apiUrl, refreshEndpoint, generateType, initialValues]);

	const withGenerateType = (values: FormValues): FormValues =>
		addGenerateType(values, generateType);

	const refresh = async () => {
		loadControllerRef.current?.abort();
		const controller = new AbortController();
		loadControllerRef.current = controller;
		await refreshDialogOptions(
			apiUrl,
			refreshEndpoint,
			withGenerateType(collectFormValues(formId, false).values),
			controller.signal,
			setLoadState,
		);
	};

	// runが投げた例外はダイアログ内にログと一緒に表示する。ダイアログを閉じると（アンマウントでabort）sidecar側も中断する。
	// 成功したら結果を返す（入力エラー・失敗・中断ならnull）
	const execute = async <T>(
		run: (values: FormValues, context: JobContext) => Promise<T>,
	): Promise<{ value: T } | null> => {
		const { values, validationError } = collectFormValues(formId, true);
		if (validationError) {
			return null;
		}
		return job.run((context) => run(values, context));
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
