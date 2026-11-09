import type { Dispatch, ReactNode, SetStateAction } from "react";
import { createContext, use } from "react";
import type { DatasetSrcInfo } from "../model/CommandOption";
import { useResettableState } from "../utils/useResettableState";
import { JdbcConnectionProvider } from "./JdbcConnectionProvider";

const datasetSrcInfoContext = createContext<DatasetSrcInfo>({ values: {} });
const setDatasetSrcInfoContext = createContext<
	Dispatch<SetStateAction<DatasetSrcInfo>>
>(() => undefined);

export function DatasetSrcInfoProvider({
	children,
	initialValue,
	resetKey,
}: {
	children: ReactNode;
	initialValue: DatasetSrcInfo;
	resetKey?: string;
}) {
	const [state, setState] = useResettableState(initialValue, resetKey);
	return (
		<JdbcConnectionProvider resetKey={resetKey}>
			<datasetSrcInfoContext.Provider value={state}>
				<setDatasetSrcInfoContext.Provider value={setState}>
					{children}
				</setDatasetSrcInfoContext.Provider>
			</datasetSrcInfoContext.Provider>
		</JdbcConnectionProvider>
	);
}

export const useDatasetSrcInfo = () => use(datasetSrcInfoContext);
export const useSetDatasetSrcInfo = () => use(setDatasetSrcInfoContext);
