import { createContext, useContext, useState } from "react";

type JdbcConnectionState = {
	jdbcValues: Record<string, string>;
	connectionOk: boolean;
};

type JdbcConnectionContextValue = {
	state: JdbcConnectionState;
	setState: (state: JdbcConnectionState) => void;
};

const JdbcConnectionContext = createContext<JdbcConnectionContextValue>({
	state: { jdbcValues: {}, connectionOk: false },
	setState: () => {},
});

export function JdbcConnectionProvider({
	children,
}: {
	children: React.ReactNode;
}) {
	const [state, setState] = useState<JdbcConnectionState>({
		jdbcValues: {},
		connectionOk: false,
	});

	return (
		<JdbcConnectionContext.Provider value={{ state, setState }}>
			{children}
		</JdbcConnectionContext.Provider>
	);
}

export function useJdbcConnectionState(): JdbcConnectionState {
	return useContext(JdbcConnectionContext).state;
}

export function useSetJdbcConnectionState(): (state: JdbcConnectionState) => void {
	return useContext(JdbcConnectionContext).setState;
}
