import { describe, expect, it } from "vitest";
import {
	DEFAULT_PORTS,
	SERVICE_NAME_LABEL,
	buildJdbcUrl,
	parseJdbcUrl,
} from "../../model/JdbcUrlBuilder";
import type { JdbcUrlBuilderState } from "../../model/JdbcUrlBuilder";

describe("DEFAULT_PORTS", () => {
	it("各DBのデフォルトポートが正しいこと", () => {
		expect(DEFAULT_PORTS.oracle).toBe("1521");
		expect(DEFAULT_PORTS.postgres).toBe("5432");
		expect(DEFAULT_PORTS.h2).toBe("9092");
	});
});

describe("SERVICE_NAME_LABEL", () => {
	it("Oracleのラベルがサービス名であること", () => {
		expect(SERVICE_NAME_LABEL.oracle).toBe("Service Name");
	});
	it("PostgreSQLとH2のラベルがデータベース名であること", () => {
		expect(SERVICE_NAME_LABEL.postgres).toBe("Database Name");
		expect(SERVICE_NAME_LABEL.h2).toBe("Database Name");
	});
});

describe("buildJdbcUrl", () => {
	it("hostもserviceNameも空の場合は空文字を返すこと", () => {
		const state: JdbcUrlBuilderState = {
			rdbType: "postgres",
			host: "",
			port: "5432",
			serviceName: "",
		};
		expect(buildJdbcUrl(state)).toBe("");
	});

	it("Oracle の JDBC URL を生成できること", () => {
		const state: JdbcUrlBuilderState = {
			rdbType: "oracle",
			host: "localhost",
			port: "1521",
			serviceName: "ORCL",
		};
		expect(buildJdbcUrl(state)).toBe("jdbc:oracle:thin:@localhost:1521:ORCL");
	});

	it("PostgreSQL の JDBC URL を生成できること", () => {
		const state: JdbcUrlBuilderState = {
			rdbType: "postgres",
			host: "localhost",
			port: "5432",
			serviceName: "mydb",
		};
		expect(buildJdbcUrl(state)).toBe("jdbc:postgresql://localhost:5432/mydb");
	});

	it("H2 の JDBC URL を生成できること", () => {
		const state: JdbcUrlBuilderState = {
			rdbType: "h2",
			host: "localhost",
			port: "9092",
			serviceName: "testdb",
		};
		expect(buildJdbcUrl(state)).toBe("jdbc:h2:tcp://localhost:9092/testdb");
	});
});

describe("parseJdbcUrl", () => {
	it("空文字の場合は空オブジェクトを返すこと", () => {
		expect(parseJdbcUrl("")).toEqual({});
	});

	it("不明なURLの場合は空オブジェクトを返すこと", () => {
		expect(parseJdbcUrl("jdbc:mysql://localhost/db")).toEqual({});
	});

	it("Oracle の JDBC URL をパースできること", () => {
		const result = parseJdbcUrl("jdbc:oracle:thin:@localhost:1521:ORCL");
		expect(result.rdbType).toBe("oracle");
		expect(result.host).toBe("localhost");
		expect(result.port).toBe("1521");
		expect(result.serviceName).toBe("ORCL");
	});

	it("PostgreSQL の JDBC URL をパースできること", () => {
		const result = parseJdbcUrl("jdbc:postgresql://localhost:5432/mydb");
		expect(result.rdbType).toBe("postgres");
		expect(result.host).toBe("localhost");
		expect(result.port).toBe("5432");
		expect(result.serviceName).toBe("mydb");
	});

	it("H2 の JDBC URL をパースできること", () => {
		const result = parseJdbcUrl("jdbc:h2:tcp://localhost:9092/testdb");
		expect(result.rdbType).toBe("h2");
		expect(result.host).toBe("localhost");
		expect(result.port).toBe("9092");
		expect(result.serviceName).toBe("testdb");
	});

	it("buildJdbcUrl でビルドした URL を parseJdbcUrl で元に戻せること", () => {
		const state: JdbcUrlBuilderState = {
			rdbType: "postgres",
			host: "db.example.com",
			port: "5432",
			serviceName: "production",
		};
		const url = buildJdbcUrl(state);
		const parsed = parseJdbcUrl(url);
		expect(parsed.rdbType).toBe(state.rdbType);
		expect(parsed.host).toBe(state.host);
		expect(parsed.port).toBe(state.port);
		expect(parsed.serviceName).toBe(state.serviceName);
	});
});
