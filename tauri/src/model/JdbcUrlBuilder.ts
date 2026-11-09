export type RdbType = "oracle" | "postgres" | "h2";

export type JdbcUrlBuilderState = {
	rdbType: RdbType;
	host: string;
	port: string;
	serviceName: string;
};

export const DEFAULT_PORTS: Record<RdbType, string> = {
	oracle: "1521",
	postgres: "5432",
	h2: "9092",
};

export const SERVICE_NAME_LABEL: Record<RdbType, string> = {
	oracle: "Service Name",
	postgres: "Database Name",
	h2: "Database Name",
};

export function buildJdbcUrl(state: JdbcUrlBuilderState): string {
	const { rdbType, host, port, serviceName } = state;
	if (!host && !serviceName) {
		return "";
	}
	switch (rdbType) {
		case "oracle":
			return `jdbc:oracle:thin:@//${host}:${port}/${serviceName}`;
		case "postgres":
			return `jdbc:postgresql://${host}:${port}/${serviceName}`;
		case "h2":
			return `jdbc:h2:tcp://${host}:${port}/${serviceName}`;
	}
}

export function parseJdbcUrl(url: string): Partial<JdbcUrlBuilderState> {
	if (!url) {
		return {};
	}
	if (url.startsWith("jdbc:oracle:thin:@")) {
		const rest = url.slice("jdbc:oracle:thin:@".length).replace(/^\/\//, "");
		if (rest.includes("/")) {
			return parseHostPortDb("oracle", rest);
		}
		// SID形式(host:port:sid)。組み立て直すとサービス名形式になる
		const parts = rest.split(":");
		if (parts.length >= 3) {
			return {
				rdbType: "oracle",
				host: parts[0],
				port: parts[1],
				serviceName: parts.slice(2).join(":"),
			};
		}
	}
	if (url.startsWith("jdbc:postgresql://")) {
		return parseHostPortDb("postgres", url.slice("jdbc:postgresql://".length));
	}
	if (url.startsWith("jdbc:h2:tcp://")) {
		return parseHostPortDb("h2", url.slice("jdbc:h2:tcp://".length));
	}
	return {};
}

// host[:port][/db]
function parseHostPortDb(rdbType: RdbType, rest: string): JdbcUrlBuilderState {
	const slashIdx = rest.indexOf("/");
	const hostPort = slashIdx >= 0 ? rest.slice(0, slashIdx) : rest;
	const colonIdx = hostPort.lastIndexOf(":");
	return {
		rdbType,
		host: colonIdx >= 0 ? hostPort.slice(0, colonIdx) : hostPort,
		port: colonIdx >= 0 ? hostPort.slice(colonIdx + 1) : DEFAULT_PORTS[rdbType],
		serviceName: slashIdx >= 0 ? rest.slice(slashIdx + 1) : "",
	};
}
