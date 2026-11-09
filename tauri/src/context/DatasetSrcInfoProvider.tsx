import type { Dispatch, ReactNode, SetStateAction } from "react";
import { createContext, use, useState } from "react";
import type { DatasetSrcInfo } from "../model/CommandOption";
import { JdbcConnectionProvider } from "./JdbcConnectionProvider";

const datasetSrcInfoContext = createContext<DatasetSrcInfo>({ values: {} });
const setDatasetSrcInfoContext = createContext<
	Dispatch<SetStateAction<DatasetSrcInfo>>
>(() => undefined);

// initialValueは初回と、resetKeyが変わったときだけ読む（子を再マウントせずに状態だけ作り直す）
export function DatasetSrcInfoProvider({
	children,
	initialValue,
	resetKey,
}: {
	children: ReactNode;
	initialValue: DatasetSrcInfo;
	resetKey?: string;
}) {
	const [state, setState] = useState<DatasetSrcInfo>(initialValue);
	const [currentResetKey, setCurrentResetKey] = useState(resetKey);
	if (resetKey !== currentResetKey) {
		setCurrentResetKey(resetKey);
		setState(initialValue);
	}
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
