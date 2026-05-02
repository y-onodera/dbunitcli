import type { Dispatch, ReactNode, SetStateAction } from "react";
import { createContext, use, useState } from "react";

type ParamInputs = { [key: string]: string };

const paramInputsContext = createContext<ParamInputs>({});
const setParamInputsContext = createContext<Dispatch<SetStateAction<ParamInputs>>>(
	() => undefined,
);

export default function ParameterInputProvider({ children }: { children: ReactNode }) {
	const [paramInputs, setParamInputs] = useState<ParamInputs>({});
	return (
		<paramInputsContext.Provider value={paramInputs}>
			<setParamInputsContext.Provider value={setParamInputs}>
				{children}
			</setParamInputsContext.Provider>
		</paramInputsContext.Provider>
	);
}

export const useParamInputs = () => use(paramInputsContext);
export const useSetParamInputs = () => use(setParamInputsContext);
