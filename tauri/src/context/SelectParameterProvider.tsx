import {
	type Dispatch,
	type ReactNode,
	type SetStateAction,
	createContext,
	useContext,
	useState,
} from "react";
import type {
	CompareParams,
	ConvertParams,
	GenerateParams,
	Parameter,
	ParameterizeParams,
	RunParams,
} from "../model/CommandParam";

export type SelectParameter = {
	name: string;
	command: string;
	convert: ConvertParams;
	compare: CompareParams;
	generate: GenerateParams;
	run: RunParams;
	parameterize: ParameterizeParams;
	currentParameter: () => Parameter;
};
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
export const useSelectParameter = () => useContext(selectParameterContext);
export const useSetSelectParameter = () => {
	const setParameter = useContext(setSelectParameterContext);
	return (response: Parameter, command: string, name: string) => {
		const newParam = {} as SelectParameter;
		newParam.name = name;
		if (command === "convert") {
			newParam.convert = response as ConvertParams;
		}
		if (command === "compare") {
			newParam.compare = response as CompareParams;
		}
		if (command === "generate") {
			newParam.generate = response as GenerateParams;
		}
		if (command === "run") {
			newParam.run = response as RunParams;
		}
		if (command === "parameterize") {
			newParam.parameterize = response as ParameterizeParams;
		}
		newParam.command = command;
		newParam.currentParameter = () => {
			if (newParam.command === "convert") {
				return newParam.convert;
			}
			if (newParam.command === "compare") {
				return newParam.compare;
			}
			if (newParam.command === "generate") {
				return newParam.generate;
			}
			if (newParam.command === "run") {
				return newParam.run;
			}
			return newParam.parameterize;
		};
		setParameter(newParam);
	};
};
