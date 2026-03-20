import { createContext, useContext, useState } from "react";
import type { DatasetSrcInfo } from "../model/CommandParam";

type DatasetSrcInfoContextValue = {
	state: DatasetSrcInfo | undefined;
	setState: (state: DatasetSrcInfo | undefined) => void;
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
	const [state, setState] = useState<DatasetSrcInfo | undefined>(initialValue);

	return (
		<DatasetSrcInfoContext.Provider value={{ state, setState }}>
			{children}
		</DatasetSrcInfoContext.Provider>
	);
}

export function useDatasetSrcInfo(): DatasetSrcInfo | undefined {
	return useContext(DatasetSrcInfoContext).state;
}

export function useSetDatasetSrcInfo(): (
	state: DatasetSrcInfo | undefined,
) => void {
	return useContext(DatasetSrcInfoContext).setState;
}
