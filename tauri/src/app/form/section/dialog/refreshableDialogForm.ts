import { fetchData, getErrorMessage } from "../../../../utils/fetchUtils";
import type { FormValues } from "../../../../utils/formValues";

// refresh系エンドポイントで再構成するダイアログフォーム（Scaffold・XlsxSchema生成）の共通基盤
export type DialogLoadState<OPTIONS> =
	| { status: "loading" }
	| { status: "error"; message: string }
	| { status: "loaded"; options: OPTIONS };

export async function postDialogForm<T>(
	apiUrl: string,
	endpoint: string,
	values: FormValues,
): Promise<T> {
	const response = await fetchData({
		endpoint: `${apiUrl}${endpoint}`,
		options: {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(values),
		},
	});
	return (await response.json()) as T;
}

// refresh結果をLoadStateへ反映する共通フロー（初期表示・選択変更時の再構成の双方で使用）
export async function refreshDialogOptions<OPTIONS>(
	apiUrl: string,
	endpoint: string,
	values: FormValues,
	signal: AbortSignal | undefined,
	setLoadState: (state: DialogLoadState<OPTIONS>) => void,
): Promise<void> {
	try {
		const options = await postDialogForm<OPTIONS>(apiUrl, endpoint, values);
		if (!signal?.aborted) {
			setLoadState({ status: "loaded", options });
		}
	} catch (ex) {
		if (!signal?.aborted) {
			setLoadState({ status: "error", message: getErrorMessage(ex) });
		}
	}
}
