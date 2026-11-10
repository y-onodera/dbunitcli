import type { Dispatch, ReactNode, SetStateAction } from "react";
import { createContext, use } from "react";
import type { InputParam } from "../model/SelectParameter";
import { useResettableState } from "../utils/useResettableState";
import { useSelectParameter } from "./SelectParameterProvider";

const paramInputsContext = createContext<InputParam>({});
const setParamInputsContext = createContext<
	Dispatch<SetStateAction<InputParam>>
>(() => undefined);

// 編集中の-P。inputParamは読込ごとに別の参照になり、refresh・renameでは同じ参照を引き継ぐ（SelectParameter・useSelectParameter）。
// 参照で比べて、読込のときだけ保存値へ戻し、refresh・renameでは編集を残す
export default function ParameterInputProvider({
	children,
}: {
	children: ReactNode;
}) {
	const inputParam = useSelectParameter()?.inputParam;
	const [paramInputs, setParamInputs] = useResettableState(
		inputParam ?? {},
		inputParam,
	);
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
