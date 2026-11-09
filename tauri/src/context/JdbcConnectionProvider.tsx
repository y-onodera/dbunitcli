import type { Dispatch, ReactNode, SetStateAction } from "react";
import { createContext, use } from "react";
import { useResettableState } from "../utils/useResettableState";

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

export function JdbcConnectionProvider({
	children,
	resetKey,
}: {
	children: ReactNode;
	resetKey?: string;
}) {
	const [state, setState] = useResettableState(NOT_CONNECTED, resetKey);
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
