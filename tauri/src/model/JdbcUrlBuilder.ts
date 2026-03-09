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
	if (!host && !serviceName) { return ""; }
	switch (rdbType) {
		case "oracle":
			return `jdbc:oracle:thin:@${host}:${port}:${serviceName}`;
		case "postgres":
			return `jdbc:postgresql://${host}:${port}/${serviceName}`;
		case "h2":
			return `jdbc:h2:tcp://${host}:${port}/${serviceName}`;
	}
}

export function parseJdbcUrl(url: string): Partial<JdbcUrlBuilderState> {
	if (!url) { return {}; }
	if (url.startsWith("jdbc:oracle:thin:@")) {
		const rest = url.slice("jdbc:oracle:thin:@".length);
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
		const rest = url.slice("jdbc:postgresql://".length);
		const slashIdx = rest.indexOf("/");
		const hostPort = slashIdx >= 0 ? rest.slice(0, slashIdx) : rest;
		const db = slashIdx >= 0 ? rest.slice(slashIdx + 1) : "";
		const colonIdx = hostPort.lastIndexOf(":");
		return {
			rdbType: "postgres",
			host: colonIdx >= 0 ? hostPort.slice(0, colonIdx) : hostPort,
			port:
				colonIdx >= 0 ? hostPort.slice(colonIdx + 1) : DEFAULT_PORTS.postgres,
			serviceName: db,
		};
	}
	if (url.startsWith("jdbc:h2:tcp://")) {
		const rest = url.slice("jdbc:h2:tcp://".length);
		const slashIdx = rest.indexOf("/");
		const hostPort = slashIdx >= 0 ? rest.slice(0, slashIdx) : rest;
		const db = slashIdx >= 0 ? rest.slice(slashIdx + 1) : "";
		const colonIdx = hostPort.lastIndexOf(":");
		return {
			rdbType: "h2",
			host: colonIdx >= 0 ? hostPort.slice(0, colonIdx) : hostPort,
			port: colonIdx >= 0 ? hostPort.slice(colonIdx + 1) : DEFAULT_PORTS.h2,
			serviceName: db,
		};
	}
	return {};
}
