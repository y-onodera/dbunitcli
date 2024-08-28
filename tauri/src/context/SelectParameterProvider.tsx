import {
	type Dispatch,
	type ReactNode,
	type SetStateAction,
	createContext,
	useContext,
	useState,
} from "react";

export type Attribute = {
	type: string;
	required: boolean;
	selectOption: string[];
};
export type CommandParam = {
	name: string;
	value: string;
	attribute: Attribute;
};
export type CommandParams = {
	handleTypeSelect: () => Promise<void>;
	name: string;
	prefix: string;
	elements: CommandParam[];
	jdbc?: CommandParams;
};
export type DatasetSource = CommandParams & {
	jdbc: CommandParams;
	templateRender: CommandParams;
};
export type Parameter =
	| ConvertParams
	| CompareParams
	| GenerateParams
	| RunParams
	| ParameterizeParams;
export type ConvertParams = {
	srcData: DatasetSource;
	convertResult: CommandParams;
};
export type CompareParams = {
	elements: CommandParam[];
	newData: DatasetSource;
	oldData: DatasetSource;
	imageOption: CommandParams;
	convertResult: CommandParams;
	expectData: DatasetSource;
};
export type GenerateParams = {
	elements: CommandParam[];
	srcData: DatasetSource;
	templateOption: CommandParams;
	convertResult: CommandParams;
};
export type RunParams = {
	elements: CommandParam[];
	srcData: DatasetSource;
	templateOption: CommandParams;
	jdbcOption: CommandParams;
	convertResult: CommandParams;
};
export type ParameterizeParams = {
	elements: CommandParam[];
	paramData: DatasetSource;
	templateOption: CommandParams;
};
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
