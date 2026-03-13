import {
	type Dispatch,
	type SetStateAction,
	useCallback,
	useState,
} from "react";
import { BlueButton, ButtonWithIcon } from "../../components/element/Button";
import { EditIcon, PreviewIcon } from "../../components/element/Icon";
import {
	ControllTextBox,
	InputLabel,
	ResourceDatalist,
} from "../../components/element/Input";
import { useSetJdbcConnectionState } from "../../context/JdbcConnectionProvider";
import { useResourcesSettings } from "../../context/WorkspaceResourcesProvider";
import {
	useDeleteJdbcProperties,
	useJdbcConnectionTest,
	useJdbcSaveProperties,
} from "../../hooks/useJdbc";
import type { CommandParam } from "../../model/CommandParam";
import JdbcPropertiesPreviewDialog from "../settings/JdbcPropertiesPreviewDialog";
import JdbcSavePropertiesDialog from "../settings/JdbcSavePropertiesDialog";
import JdbcUrlBuilderDialog from "../settings/JdbcUrlBuilderDialog";
import { RemoveResource } from "../settings/ResourceEditButton";
import { FileChooser } from "./Chooser";

export const JDBC_FIELD_NAMES = [
	"jdbcUrl",
	"jdbcUser",
	"jdbcPass",
	"jdbcProperties",
] as const;

export default function JdbcFormSection({
	prefix,
	elements,
}: {
	prefix: string;
	elements: CommandParam[];
}) {
	const setJdbcConnection = useSetJdbcConnectionState();
	const [jdbcValues, setJdbcValues] = useState<Record<string, string>>(() => {
		const initial: Record<string, string> = {};
		for (const el of elements) {
			initial[el.name] = el.value;
		}
		return initial;
	});

	const handleJdbcValueChange = useCallback(
		(name: string, value: string) => {
			setJdbcValues((prev) => ({ ...prev, [name]: value }));
			setJdbcConnection({ jdbcValues: {}, connectionOk: false });
		},
		[setJdbcConnection],
	);

	const handleApplyValues = useCallback(
		(newValues: Partial<Record<string, string>>) => {
			setJdbcValues((prev) => {
				const defined = Object.fromEntries(
					Object.entries(newValues).filter(
						(entry): entry is [string, string] => entry[1] !== undefined,
					),
				);
				return { ...prev, ...defined };
			});
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
			{elements.map((element) => (
				<JdbcTextField
					key={prefix + element.name}
					prefix={prefix}
					element={element}
					value={jdbcValues[element.name] ?? element.value}
					onValueChange={handleJdbcValueChange}
					onApplyValues={
						element.name === "jdbcProperties" ? handleApplyValues : undefined
					}
				/>
			))}
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
	value,
	onValueChange,
	onApplyValues,
}: {
	prefix: string;
	element: CommandParam;
	value: string;
	onValueChange: (name: string, value: string) => void;
	onApplyValues?: (values: Partial<Record<string, string>>) => void;
}) {
	const [showJdbcUrlBuilder, setShowJdbcUrlBuilder] = useState(false);
	const [showPreview, setShowPreview] = useState(false);
	const settings = useResourcesSettings();
	const labelText = prefix ? `-${prefix}.${element.name}` : `-${element.name}`;
	const id = prefix ? `${prefix}_${element.name}` : element.name;
	const isJdbcUrl = element.name === "jdbcUrl";
	const isJdbcProperties = element.name === "jdbcProperties";
	const resourceFiles = isJdbcProperties ? settings.jdbcFiles : [];
	const hasButton = isJdbcUrl || isJdbcProperties;

	const setPath: Dispatch<SetStateAction<string>> = (action) => {
		const newPath =
			typeof action === "function" ? action(value) : action;
		onValueChange(element.name, newPath);
	};

	return (
		<div>
			<InputLabel
				text={labelText}
				id={id}
				required={element.attribute.required}
			/>
			<div className="flex">
				<div className={`flex-1${hasButton ? "" : " mr-36"}`}>
					<ControllTextBox
						name={labelText}
						id={id}
						required={element.attribute.required}
						value={value}
						list={isJdbcProperties ? `${id}_list` : undefined}
						handleChange={(ev) => onValueChange(element.name, ev.target.value)}
					/>
					{isJdbcProperties && (
						<ResourceDatalist id={id} resources={resourceFiles} />
					)}
				</div>
				<div className="flex">
					{isJdbcProperties && (
						<FileChooser
							prefix={prefix}
							element={element}
							path={value}
							setPath={setPath}
						/>
					)}
					{isJdbcProperties && value && (
						<RemoveJdbcPropertiesButton
							path={value}
							setPath={(v) => onValueChange(element.name, v)}
						/>
					)}
					{isJdbcProperties && value && onApplyValues && (
						<JdbcPropertiesPreviewButton
							path={value}
							showDialog={showPreview}
							setShowDialog={setShowPreview}
							onApplyValues={onApplyValues}
						/>
					)}
					{isJdbcUrl && (
						<JdbcUrlBuilderButton
							path={value}
							setPath={setPath}
							showDialog={showJdbcUrlBuilder}
							setShowDialog={setShowJdbcUrlBuilder}
						/>
					)}
				</div>
			</div>
		</div>
	);
}

function JdbcPropertiesPreviewButton({
	path,
	showDialog,
	setShowDialog,
	onApplyValues,
}: {
	path: string;
	showDialog: boolean;
	setShowDialog: (show: boolean) => void;
	onApplyValues: (values: Partial<Record<string, string>>) => void;
}) {
	return (
		<>
			<ButtonWithIcon
				handleClick={() => setShowDialog(true)}
				id="jdbcPropertiesPreviewButton"
			>
				<PreviewIcon title="Preview Properties" fill="white" />
			</ButtonWithIcon>
			{showDialog && (
				<JdbcPropertiesPreviewDialog
					path={path}
					handleDialogClose={() => setShowDialog(false)}
					handleApply={(values) => {
						onApplyValues(values);
						setShowDialog(false);
					}}
				/>
			)}
		</>
	);
}

function JdbcUrlBuilderButton({
	path,
	setPath,
	showDialog,
	setShowDialog,
}: {
	path: string;
	setPath: Dispatch<SetStateAction<string>>;
	showDialog: boolean;
	setShowDialog: Dispatch<SetStateAction<boolean>>;
}) {
	return (
		<>
			<ButtonWithIcon
				handleClick={() => setShowDialog(true)}
				id="jdbcUrlBuilderButton"
			>
				<EditIcon title="JDBC URL Builder" fill="white" />
			</ButtonWithIcon>
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
		!!jdbcValues.jdbcUrl ||
		!!jdbcValues.jdbcUser ||
		!!jdbcValues.jdbcPass;

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
