import type { Dispatch, ReactNode, SetStateAction } from "react";
import { createContext, use, useState } from "react";
import type { SelectParameter } from "../model/SelectParameter";

// コマンド・パラメータを選ぶまではundefined
const selectParameterContext = createContext<SelectParameter | undefined>(
	undefined,
);
const setSelectParameterContext = createContext<
	Dispatch<SetStateAction<SelectParameter | undefined>>
>(() => undefined);
export default function SelectParameterProvider(props: {
	children: ReactNode;
}) {
	const [parameter, setParameter] = useState<SelectParameter | undefined>();
	return (
		<selectParameterContext.Provider value={parameter}>
			<setSelectParameterContext.Provider value={setParameter}>
				{props.children}
			</setSelectParameterContext.Provider>
		</selectParameterContext.Provider>
	);
}
export const useSelectParameter = () => use(selectParameterContext);
export const useSetSelectParameterState = () => use(setSelectParameterContext);
