import { createContext, useContext, useState } from "react";
import type { DatasetSrcInfo } from "../model/CommandOption";
import { JdbcConnectionProvider } from "./JdbcConnectionProvider";

type DatasetSrcInfoContextValue = {
	state: DatasetSrcInfo;
	setState: (state: DatasetSrcInfo) => void;
};

const DatasetSrcInfoContext = createContext<DatasetSrcInfoContextValue>({
	state: {} as DatasetSrcInfo,
	setState: () => {},
});

export function DatasetSrcInfoProvider({
	children,
	initialValue,
}: {
	children: React.ReactNode;
	initialValue: DatasetSrcInfo;
}) {
	const [state, setState] = useState<DatasetSrcInfo>(initialValue);

	return (
		<JdbcConnectionProvider>
			<DatasetSrcInfoContext.Provider value={{ state, setState }}>
				{children}
			</DatasetSrcInfoContext.Provider>
		</JdbcConnectionProvider>
	);
}

export function useDatasetSrcInfo(): DatasetSrcInfo {
	return useContext(DatasetSrcInfoContext).state;
}

export function useSetDatasetSrcInfo(): (state: DatasetSrcInfo) => void {
	return useContext(DatasetSrcInfoContext).setState;
}
