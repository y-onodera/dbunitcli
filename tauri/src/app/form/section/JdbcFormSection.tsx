import { useRef, useState } from "react";
import {
	InlineActions,
	SectionFieldset,
	SectionOptionHeader,
} from "../../../components/dialog";
import { BlueButton } from "../../../components/element/Button";
import { useSetJdbcConnectionState } from "../../../context/JdbcConnectionProvider";
import {
	useJdbcConnectionTest,
	useSaveJdbcProperties,
} from "../../../hooks/useJdbc";
import type { JdbcOption } from "../../../model/CommandOption";
import JdbcSavePropertiesDialog from "./dialog/JdbcSavePropertiesDialog";
import JdbcPropertiesTextField from "./element/JdbcPropertiesTextField";
import JdbcUrlTextField from "./element/JdbcUrlTextField";
import PlainText from "./element/PlainText";

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

	// 接続テストの応答が届く前に入力が変わったら、その結果で接続済みにしない
	const latestJdbcValuesRef = useRef(jdbcValues);

	const handleJdbcValueChange = (name: string, value: string) => {
		const next = { ...latestJdbcValuesRef.current, [name]: value };
		latestJdbcValuesRef.current = next;
		setJdbcValues(next);
		setJdbcConnection({ jdbcValues: {}, connectionOk: false });
	};

	const handleConnectionOk = (values: Record<string, string>) => {
		if (values !== latestJdbcValuesRef.current) {
			return;
		}
		setJdbcConnection({ jdbcValues: values, connectionOk: true });
	};

	const handleConnectionFail = () => {
		setJdbcConnection({ jdbcValues: {}, connectionOk: false });
	};
	const [showOptional, setShowOptional] = useState(false);
	const toggleOptional = () => setShowOptional(!showOptional);

	return (
		<SectionFieldset>
			<legend>jdbc</legend>
			<SectionOptionHeader
				showOptional={showOptional}
				toggleOptional={toggleOptional}
				caption="jdbc option"
				command="sections/jdbc"
				label="JDBC"
			/>
			<JdbcPropertiesTextField
				prefix={prefix}
				element={jdbcOption.jdbcProperties}
				onValueChange={handleJdbcValueChange}
				hidden={!showOptional}
			/>
			<JdbcUrlTextField
				prefix={prefix}
				element={jdbcOption.jdbcUrl}
				onValueChange={handleJdbcValueChange}
				hidden={!showOptional}
			/>
			<PlainText
				prefix={prefix}
				element={jdbcOption.jdbcUser}
				handleValueChange={(value) =>
					handleJdbcValueChange(jdbcOption.jdbcUser.name, value)
				}
				hidden={!showOptional}
			/>
			<PlainText
				prefix={prefix}
				element={jdbcOption.jdbcPass}
				handleValueChange={(value) =>
					handleJdbcValueChange(jdbcOption.jdbcPass.name, value)
				}
				hidden={!showOptional}
			/>
			<InlineActions>
				<JdbcConnectionTestButton
					prefix={prefix}
					jdbcValues={jdbcValues}
					onConnectionOk={handleConnectionOk}
					onConnectionFail={handleConnectionFail}
				/>
				<JdbcSavePropertiesButton prefix={prefix} jdbcValues={jdbcValues} />
			</InlineActions>
		</SectionFieldset>
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
	// 結果はテストした値と組で持ち、入力が変わったら出さない
	const [result, setResult] = useState<{
		success: boolean;
		message: string;
		values: Record<string, string>;
	} | null>(null);
	const [testing, setTesting] = useState(false);

	const hasUrlUserPass =
		!!jdbcValues.jdbcUrl && !!jdbcValues.jdbcUser && !!jdbcValues.jdbcPass;
	const hasProperties = !!jdbcValues.jdbcProperties;
	const isEnabled = !testing && (hasUrlUserPass || hasProperties);

	const handleTest = async () => {
		setTesting(true);
		setResult(null);
		// catchの無いtry/finallyはReact Compilerが未対応のため、Promiseのfinallyで書く
		await jdbcConnectionTest(jdbcValues)
			.then((testResult) => {
				setResult(testResult && { ...testResult, values: jdbcValues });
				if (testResult?.success) {
					onConnectionOk(jdbcValues);
				} else {
					onConnectionFail();
				}
			})
			.finally(() => {
				setTesting(false);
			});
	};

	return (
		<>
			<BlueButton
				title={testing ? "Connecting..." : "Connection Test"}
				disabled={!isEnabled}
				id={`${prefix}_jdbcConnectionTest`}
				handleClick={handleTest}
			/>
			{result && result.values === jdbcValues && (
				<span
					className={`text-sm font-medium ${result.success ? "text-success" : "text-error"}`}
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
	const saveJdbcProperties = useSaveJdbcProperties();
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
					save={(name) => saveJdbcProperties(name, jdbcValues)}
					handleSave={() => setShowDialog(false)}
				/>
			)}
		</>
	);
}
