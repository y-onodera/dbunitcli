import type { Dispatch, FormEvent, ReactNode, SetStateAction } from "react";
import { createContext, use, useEffect, useState } from "react";

// 入力欄のname（-src.src等）ごとのエラーメッセージ
export type FieldErrors = { [name: string]: string[] };

const NO_ERRORS: string[] = [];

const fieldErrorsContext = createContext<FieldErrors>({});
const setFieldErrorsContext = createContext<
	Dispatch<SetStateAction<FieldErrors>>
>(() => undefined);
// 表示中の入力欄のname。エラーを出せる欄かどうかの判定に使い、変わっても再描画は要らないため state にしない
const visibleFieldsContext = createContext<Set<string>>(new Set());

// 計算されたキーの分割代入はReact Compilerが未対応のため、コンポーネント外で行う。エラーが無ければ同じ値を返して再描画を避ける
function omitFieldErrors(errors: FieldErrors, name: string): FieldErrors {
	if (!(name in errors)) {
		return errors;
	}
	const { [name]: _, ...rest } = errors;
	return rest;
}

export default function FieldErrorProvider({
	children,
}: {
	children: ReactNode;
}) {
	const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
	const [visibleFields] = useState(() => new Set<string>());
	// 入力し直した欄の検証エラーは消す。欄ごとではなく、バブリングしてくるchangeをここでまとめて受ける
	const handleChange = (ev: FormEvent<HTMLDivElement>) => {
		const { name } = ev.target as HTMLInputElement;
		if (name) {
			setFieldErrors((current) => omitFieldErrors(current, name));
		}
	};
	return (
		<visibleFieldsContext.Provider value={visibleFields}>
			<fieldErrorsContext.Provider value={fieldErrors}>
				<setFieldErrorsContext.Provider value={setFieldErrors}>
					<div className="contents" onChange={handleChange}>
						{children}
					</div>
				</setFieldErrorsContext.Provider>
			</fieldErrorsContext.Provider>
		</visibleFieldsContext.Provider>
	);
}

export const useSetFieldErrors = () => use(setFieldErrorsContext);
export const useVisibleFields = () => use(visibleFieldsContext);

// changeを起こさずに値を変えたとき（ファイル選択等）に、その欄の検証エラーを消す
export const useClearFieldErrors = () => {
	const setFieldErrors = use(setFieldErrorsContext);
	return (name: string) =>
		setFieldErrors((current) => omitFieldErrors(current, name));
};

// 表示中のあいだ入力欄をエラーの表示先として登録し、その欄のエラーを返す
export const useFieldErrors = (name: string, visible: boolean): string[] => {
	const visibleFields = use(visibleFieldsContext);
	useEffect(() => {
		if (!visible) {
			return;
		}
		visibleFields.add(name);
		return () => {
			visibleFields.delete(name);
		};
	}, [visibleFields, name, visible]);
	return use(fieldErrorsContext)[name] ?? NO_ERRORS;
};
