import type { Dispatch, SetStateAction } from "react";
import { useCallback, useState } from "react";
import { BlueButton } from "../../../components/element/Button";
import { BlueEditButton } from "../../../components/element/ButtonIcon";
import { useSetJdbcConnectionState } from "../../../context/JdbcConnectionProvider";
import { useResourcesSettings } from "../../../context/WorkspaceResourcesProvider";
import {
	useDeleteJdbcProperties,
	useJdbcConnectionTest,
	useJdbcSaveProperties,
} from "../../../hooks/useJdbc";
import type { CommandParam, JdbcOption } from "../../../model/CommandParam";
import JdbcSavePropertiesDialog from "../../settings/JdbcSavePropertiesDialog";
import JdbcUrlBuilderDialog from "../../settings/JdbcUrlBuilderDialog";
import { RemoveResource } from "../../settings/ResourceEditButton";
import ResourceDropDownMenu from "./ResourceDropDownMenu";
import ResourceText from "./ResourceText";

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
			<JdbcTextField
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
			<JdbcTextField
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

function JdbcTextField({
	prefix,
	element,
	onValueChange,
}: {
	prefix: string;
	element: CommandParam;
	onValueChange: (name: string, value: string) => void;
}) {
	const settings = useResourcesSettings();
	const isJdbcUrl = element.name === "jdbcUrl";
	const isJdbcProperties = element.name === "jdbcProperties";
	const resourceFiles = isJdbcProperties ? settings.jdbcFiles : [];

	const renderMenu = ({
		path,
		setPath,
		isValueInDatalist,
	}: {
		path: string;
		setPath: Dispatch<SetStateAction<string>>;
		isValueInDatalist: boolean;
	}) => {
		const wrappedSetPath: Dispatch<SetStateAction<string>> = (action) => {
			const newPath = typeof action === "function" ? action(path) : action;
			setPath(newPath);
			onValueChange(element.name, newPath);
		};

		if (isJdbcProperties) {
			return (
				<ResourceDropDownMenu
					prefix={prefix}
					element={element}
					path={path}
					setPath={wrappedSetPath}
					isValueInDatalist={isValueInDatalist}
					removeButton={(closeMenu) => (
						<RemoveJdbcPropertiesButton
							path={path}
							setPath={(value) => {
								wrappedSetPath(value);
								closeMenu();
							}}
						/>
					)}
					className="mr-24"
				/>
			);
		}
		if (isJdbcUrl) {
			return <JdbcUrlBuilderButton path={path} setPath={wrappedSetPath} />;
		}
		return <div className="w-36" />;
	};

	return (
		<ResourceText
			prefix={prefix}
			element={element}
			resourceFiles={resourceFiles}
			onValueChange={(value) => onValueChange(element.name, value)}
		>
			{renderMenu}
		</ResourceText>
	);
}

function JdbcUrlBuilderButton({
	path,
	setPath,
}: {
	path: string;
	setPath: Dispatch<SetStateAction<string>>;
}) {
	const [showDialog, setShowDialog] = useState(false);
	return (
		<>
			<BlueEditButton handleClick={() => setShowDialog(true)} />
			{showDialog && (
				<JdbcUrlBuilderDialog
					currentUrl={path}
					handleDialogClose={() => setShowDialog(false)}
					handleSave={(url) => {
						setPath(url);
						setShowDialog(false);
					}}
				/>
			)}
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

function RemoveJdbcPropertiesButton({
	path,
	setPath,
}: {
	path: string;
	setPath: (value: string) => void;
}) {
	const deleteJdbcProperties = useDeleteJdbcProperties();
	return (
		<RemoveResource
			path={path}
			setPath={setPath}
			deleteResource={deleteJdbcProperties}
		/>
	);
}
