import type { Dispatch, ReactNode, SetStateAction } from "react";
import { createContext, use, useState } from "react";

// 入力欄のname（-src.src等）ごとのエラーメッセージ
export type FieldErrors = { [name: string]: string[] };

const fieldErrorsContext = createContext<FieldErrors>({});
const setFieldErrorsContext = createContext<
	Dispatch<SetStateAction<FieldErrors>>
>(() => undefined);

export default function FieldErrorProvider({
	children,
}: {
	children: ReactNode;
}) {
	const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
	return (
		<fieldErrorsContext.Provider value={fieldErrors}>
			<setFieldErrorsContext.Provider value={setFieldErrors}>
				{children}
			</setFieldErrorsContext.Provider>
		</fieldErrorsContext.Provider>
	);
}

export const useFieldErrors = (name: string): string[] =>
	use(fieldErrorsContext)[name] ?? [];
export const useSetFieldErrors = () => use(setFieldErrorsContext);
