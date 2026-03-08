import { useState } from "react";
import { SettingDialog } from "../../components/dialog/SettingDialog";
import {
	ControllTextBox,
	InputLabel,
	PreviewField,
	SelectBox,
} from "../../components/element/Input";

type RdbType = "oracle" | "postgres" | "h2";

type JdbcUrlBuilderState = {
	rdbType: RdbType;
	host: string;
	port: string;
	serviceName: string;
};

const DEFAULT_PORTS: Record<RdbType, string> = {
	oracle: "1521",
	postgres: "5432",
	h2: "9092",
};

const SERVICE_NAME_LABEL: Record<RdbType, string> = {
	oracle: "Service Name",
	postgres: "Database Name",
	h2: "Database Name",
};

function buildJdbcUrl(state: JdbcUrlBuilderState): string {
	const { rdbType, host, port, serviceName } = state;
	if (!host && !serviceName) return "";
	switch (rdbType) {
		case "oracle":
			return `jdbc:oracle:thin:@${host}:${port}:${serviceName}`;
		case "postgres":
			return `jdbc:postgresql://${host}:${port}/${serviceName}`;
		case "h2":
			return `jdbc:h2:tcp://${host}:${port}/${serviceName}`;
	}
}

function parseJdbcUrl(url: string): Partial<JdbcUrlBuilderState> {
	if (!url) return {};
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

type JdbcUrlBuilderDialogProps = {
	currentUrl: string;
	handleDialogClose: () => void;
	handleSave: (url: string) => void;
};

export default function JdbcUrlBuilderDialog({
	currentUrl,
	handleDialogClose,
	handleSave,
}: JdbcUrlBuilderDialogProps) {
	const parsed = parseJdbcUrl(currentUrl);
	const initialRdbType: RdbType = parsed.rdbType ?? "postgres";
	const [state, setState] = useState<JdbcUrlBuilderState>({
		rdbType: initialRdbType,
		host: parsed.host ?? "localhost",
		port: parsed.port ?? DEFAULT_PORTS[initialRdbType],
		serviceName: parsed.serviceName ?? "",
	});

	const handleRdbTypeChange = async (selected: string) => {
		const rdbType = selected as RdbType;
		setState((prev) => ({
			...prev,
			rdbType,
			port: DEFAULT_PORTS[rdbType],
		}));
	};

	const builtUrl = buildJdbcUrl(state);

	return (
		<SettingDialog
			setting={builtUrl}
			handleDialogClose={handleDialogClose}
			handleCommit={(url) => { if (url) handleSave(url); }}
			commitLabel="Apply"
		>
			<div className="w-[480px]">
				<h2 className="text-lg font-bold mb-4">JDBC URL Builder</h2>

				<div className="mb-3">
					<InputLabel
						text="Database"
						id="jdbcUrlBuilder_rdbType"
						required={true}
					/>
					<SelectBox
						name="jdbcUrlBuilder_rdbType"
						id="jdbcUrlBuilder_rdbType"
						required={true}
						defaultValue={state.rdbType}
						handleOnChange={handleRdbTypeChange}
					>
						<option value="oracle">Oracle</option>
						<option value="postgres">PostgreSQL</option>
						<option value="h2">H2</option>
					</SelectBox>
				</div>

				<div className="mb-3">
					<InputLabel text="Host" id="jdbcUrlBuilder_host" required={true} />
					<ControllTextBox
						name="jdbcUrlBuilder_host"
						id="jdbcUrlBuilder_host"
						required={true}
						value={state.host}
						handleChange={(ev) =>
							setState((prev) => ({ ...prev, host: ev.target.value }))
						}
					/>
				</div>

				<div className="mb-3">
					<InputLabel text="Port" id="jdbcUrlBuilder_port" required={true} />
					<ControllTextBox
						name="jdbcUrlBuilder_port"
						id="jdbcUrlBuilder_port"
						required={true}
						value={state.port}
						handleChange={(ev) =>
							setState((prev) => ({ ...prev, port: ev.target.value }))
						}
					/>
				</div>

				<div className="mb-3">
					<InputLabel
						text={SERVICE_NAME_LABEL[state.rdbType]}
						id="jdbcUrlBuilder_serviceName"
						required={true}
					/>
					<ControllTextBox
						name="jdbcUrlBuilder_serviceName"
						id="jdbcUrlBuilder_serviceName"
						required={true}
						value={state.serviceName}
						handleChange={(ev) =>
							setState((prev) => ({ ...prev, serviceName: ev.target.value }))
						}
					/>
				</div>

				<div className="mb-4">
					<PreviewField
						id="jdbcUrlBuilder_preview"
						label="Preview"
						value={builtUrl}
						placeholder="(Enter host and service name)"
					/>
				</div>
			</div>
		</SettingDialog>
	);
}
