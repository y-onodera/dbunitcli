import type { Dispatch, ReactNode, SetStateAction } from "react";
import { createContext, use, useState } from "react";

type JdbcConnectionState = {
	jdbcValues: Record<string, string>;
	connectionOk: boolean;
};

const NOT_CONNECTED: JdbcConnectionState = {
	jdbcValues: {},
	connectionOk: false,
};

const jdbcConnectionContext = createContext<JdbcConnectionState>(NOT_CONNECTED);
const setJdbcConnectionContext = createContext<
	Dispatch<SetStateAction<JdbcConnectionState>>
>(() => undefined);

// resetKeyが変わったら未接続に戻す（子を再マウントせずに状態だけ作り直す）
export function JdbcConnectionProvider({
	children,
	resetKey,
}: {
	children: ReactNode;
	resetKey?: string;
}) {
	const [state, setState] = useState<JdbcConnectionState>(NOT_CONNECTED);
	const [currentResetKey, setCurrentResetKey] = useState(resetKey);
	if (resetKey !== currentResetKey) {
		setCurrentResetKey(resetKey);
		setState(NOT_CONNECTED);
	}
	return (
		<jdbcConnectionContext.Provider value={state}>
			<setJdbcConnectionContext.Provider value={setState}>
				{children}
			</setJdbcConnectionContext.Provider>
		</jdbcConnectionContext.Provider>
	);
}

export const useJdbcConnectionState = () => use(jdbcConnectionContext);
export const useSetJdbcConnectionState = () => use(setJdbcConnectionContext);
