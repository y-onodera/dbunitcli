import { createContext, useContext, useState } from "react";
import type { DatasetSrcInfo } from "../model/CommandParam";
import { JdbcConnectionProvider } from "./JdbcConnectionProvider";

type DatasetSrcInfoContextValue = {
	state: DatasetSrcInfo | undefined;
	setState: (state: DatasetSrcInfo) => void;
};

const DatasetSrcInfoContext = createContext<DatasetSrcInfoContextValue>({
	state: undefined,
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

export function useDatasetSrcInfo(): DatasetSrcInfo | undefined {
	return useContext(DatasetSrcInfoContext).state;
}

export function useSetDatasetSrcInfo(): (state: DatasetSrcInfo) => void {
	return useContext(DatasetSrcInfoContext).setState;
}
