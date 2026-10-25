import type { Dispatch, ReactNode, SetStateAction } from "react";
import { createContext, use, useEffect, useState } from "react";

// 入力欄のname（-src.src等）ごとのエラーメッセージ
export type FieldErrors = { [name: string]: string[] };

const fieldErrorsContext = createContext<FieldErrors>({});
const setFieldErrorsContext = createContext<
	Dispatch<SetStateAction<FieldErrors>>
>(() => undefined);
// 表示中の入力欄のname。エラーを出せる欄かどうかの判定に使い、変わっても再描画は要らないため state にしない
const visibleFieldsContext = createContext<Set<string>>(new Set());

export default function FieldErrorProvider({
	children,
}: {
	children: ReactNode;
}) {
	const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
	const [visibleFields] = useState(() => new Set<string>());
	return (
		<visibleFieldsContext.Provider value={visibleFields}>
			<fieldErrorsContext.Provider value={fieldErrors}>
				<setFieldErrorsContext.Provider value={setFieldErrors}>
					{children}
				</setFieldErrorsContext.Provider>
			</fieldErrorsContext.Provider>
		</visibleFieldsContext.Provider>
	);
}

export const useFieldErrors = (name: string): string[] =>
	use(fieldErrorsContext)[name] ?? [];
export const useSetFieldErrors = () => use(setFieldErrorsContext);
export const useVisibleFields = () => use(visibleFieldsContext);

// 表示中のあいだ、入力欄をエラーの表示先として登録する
export const useRegisterVisibleField = (name: string, visible: boolean) => {
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
};
