import { useCallback, useState } from "react";
import { BlueButton } from "../../../components/element/Button";
import { useSetJdbcConnectionState } from "../../../context/JdbcConnectionProvider";
import {
	useJdbcConnectionTest,
	useJdbcSaveProperties,
} from "../../../hooks/useJdbc";
import type { JdbcOption } from "../../../model/CommandParam";
import JdbcSavePropertiesDialog from "../../settings/JdbcSavePropertiesDialog";
import JdbcPropertiesTextField from "./JdbcPropertiesTextField";
import JdbcTextField from "./JdbcTextField";
import JdbcUrlTextField from "./JdbcUrlTextField";

export default function JdbcFormSection({
	jdbcOption,
}: {
	jdbcOption: JdbcOption;
}) {
	const { prefix } = jdbcOption;
	const setJdbcConnection = useSetJdbcConnectionState();
	const [jdbcValues, setJdbcValues] = useState<Record<string, string>>(() => ({
		jdbcUrl: jdbcOption.jdbcUrl.value,
		jdbcUser: jdbcOption.jdbcUser.value,
		jdbcPass: jdbcOption.jdbcPass.value,
		jdbcProperties: jdbcOption.jdbcProperties.value,
	}));

	const handleJdbcValueChange = useCallback(
		(name: string, value: string) => {
			setJdbcValues((prev) => ({ ...prev, [name]: value }));
			setJdbcConnection({ jdbcValues: {}, connectionOk: false });
		},
		[setJdbcConnection],
	);

	const handleConnectionOk = useCallback(
		(values: Record<string, string>) => {
			setJdbcConnection({ jdbcValues: values, connectionOk: true });
		},
		[setJdbcConnection],
	);

	const handleConnectionFail = useCallback(() => {
		setJdbcConnection({ jdbcValues: {}, connectionOk: false });
	}, [setJdbcConnection]);

	return (
		<>
			<JdbcUrlTextField
				prefix={prefix}
				element={jdbcOption.jdbcUrl}
				onValueChange={handleJdbcValueChange}
			/>
			<JdbcTextField
				prefix={prefix}
				element={jdbcOption.jdbcUser}
				onValueChange={handleJdbcValueChange}
			/>
			<JdbcTextField
				prefix={prefix}
				element={jdbcOption.jdbcPass}
				onValueChange={handleJdbcValueChange}
			/>
			<JdbcPropertiesTextField
				prefix={prefix}
				element={jdbcOption.jdbcProperties}
				onValueChange={handleJdbcValueChange}
			/>
			<div className="mt-2 flex items-center gap-3">
				<JdbcConnectionTestButton
					prefix={prefix}
					jdbcValues={jdbcValues}
					onConnectionOk={handleConnectionOk}
					onConnectionFail={handleConnectionFail}
				/>
				<JdbcSavePropertiesButton prefix={prefix} jdbcValues={jdbcValues} />
			</div>
		</>
	);
}

function JdbcConnectionTestButton({
	prefix,
	jdbcValues,
	onConnectionOk,
	onConnectionFail,
}: {
	prefix: string;
	jdbcValues: Record<string, string>;
	onConnectionOk: (values: Record<string, string>) => void;
	onConnectionFail: () => void;
}) {
	const jdbcConnectionTest = useJdbcConnectionTest();
	const [result, setResult] = useState<{
		success: boolean;
		message: string;
	} | null>(null);
	const [testing, setTesting] = useState(false);

	const hasUrlUserPass =
		!!jdbcValues.jdbcUrl && !!jdbcValues.jdbcUser && !!jdbcValues.jdbcPass;
	const hasProperties = !!jdbcValues.jdbcProperties;
	const isEnabled = !testing && (hasUrlUserPass || hasProperties);

	const handleTest = async () => {
		setTesting(true);
		setResult(null);
		try {
			const testResult = await jdbcConnectionTest(jdbcValues);
			setResult(testResult);
			if (testResult?.success) {
				onConnectionOk(jdbcValues);
			} else {
				onConnectionFail();
			}
		} finally {
			setTesting(false);
		}
	};

	return (
		<>
			<BlueButton
				title={testing ? "Connecting..." : "Connection Test"}
				disabled={!isEnabled}
				id={`${prefix}_jdbcConnectionTest`}
				handleClick={handleTest}
			/>
			{result && (
				<span
					className={`text-sm font-medium ${result.success ? "text-green-600" : "text-red-600"}`}
				>
					{result.success ? "✓ " : "✗ "}
					{result.message}
				</span>
			)}
		</>
	);
}

function JdbcSavePropertiesButton({
	prefix,
	jdbcValues,
}: {
	prefix: string;
	jdbcValues: Record<string, string>;
}) {
	const jdbcSaveProperties = useJdbcSaveProperties();
	const [showDialog, setShowDialog] = useState(false);

	const hasAnyValue =
		!!jdbcValues.jdbcUrl || !!jdbcValues.jdbcUser || !!jdbcValues.jdbcPass;

	return (
		<>
			<BlueButton
				title="Save as Properties"
				disabled={!hasAnyValue}
				id={`${prefix}_jdbcSaveProperties`}
				handleClick={() => setShowDialog(true)}
			/>
			{showDialog && (
				<JdbcSavePropertiesDialog
					jdbcValues={jdbcValues}
					handleDialogClose={() => setShowDialog(false)}
					handleSave={async (name) => {
						await jdbcSaveProperties(name, jdbcValues);
						setShowDialog(false);
					}}
				/>
			)}
		</>
	);
}
