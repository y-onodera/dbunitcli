import type { Dispatch, ReactNode, SetStateAction } from "react";
import { createContext, use, useState } from "react";
import type { Parameter } from "../model/SelectParameter";
import { SelectParameter } from "../model/SelectParameter";

const selectParameterContext = createContext<SelectParameter>(
	{} as SelectParameter,
);
const setSelectParameterContext = createContext<
	Dispatch<SetStateAction<SelectParameter>>
>(() => undefined);
export default function SelectParameterProvider(props: {
	children: ReactNode;
}) {
	const [parameter, setParameter] = useState<SelectParameter>(
		{} as SelectParameter,
	);
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
export const useSetSelectParameter = () => {
	const setParameter = use(setSelectParameterContext);
	return (response: Parameter, command: string, name: string) => {
		setParameter(new SelectParameter(response, command, name));
	};
};
