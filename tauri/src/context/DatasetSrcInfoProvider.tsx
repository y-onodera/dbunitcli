import type { Dispatch, ReactNode, SetStateAction } from "react";
import { createContext, use } from "react";
import type { DatasetSrcInfo } from "../model/CommandOption";
import { useResettableState } from "../utils/useResettableState";
import { JdbcConnectionProvider } from "./JdbcConnectionProvider";

// Providerの外かを見分けるため、既定値は同じ参照を使う
const OUTSIDE_PROVIDER: DatasetSrcInfo = { values: {} };
const datasetSrcInfoContext = createContext<DatasetSrcInfo>(OUTSIDE_PROVIDER);
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
	// 入力欄（Text）は要素の値が変わると表示をその値へ戻すため、入力値の写しも作り直す。
	// 接続状態は値の変化では作り直さない（refreshで入力済みの値が返るたびに接続テストをやり直させない）
	const [state, setState] = useResettableState(
		initialValue,
		`${resetKey ?? ""}\u0000${JSON.stringify(initialValue.values)}`,
	);
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
// データセットの読込フォームの中（データセットを読める範囲）か
export const useInDatasetSrcInfoScope = () =>
	use(datasetSrcInfoContext) !== OUTSIDE_PROVIDER;
export const useSetDatasetSrcInfo = () => use(setDatasetSrcInfoContext);
