import {
	type Dispatch,
	type SetStateAction,
	useCallback,
	useEffect,
	useState,
} from "react";
import { BlueButton, ButtonWithIcon } from "../../components/element/Button";
import { SettingIcon } from "../../components/element/Icon";
import {
	ControllTextBox,
	InputLabel,
	ResourceDatalist,
} from "../../components/element/Input";
import { useResourcesSettings } from "../../context/WorkspaceResourcesProvider";
import { useJdbcConnectionTest } from "../../hooks/useJdbcConnectionTest";
import type { CommandParam } from "../../model/CommandParam";
import JdbcUrlBuilderDialog from "../settings/JdbcUrlBuilderDialog";
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
	const [jdbcValues, setJdbcValues] = useState<Record<string, string>>(() => {
		const initial: Record<string, string> = {};
		for (const el of elements) {
			initial[el.name] = el.value;
		}
		return initial;
	});
	const handleJdbcValueChange = useCallback((name: string, value: string) => {
		setJdbcValues((prev) => ({ ...prev, [name]: value }));
	}, []);

	return (
		<>
			{elements.map((element) => (
				<JdbcTextField
					key={prefix + element.name}
					prefix={prefix}
					element={element}
					onValueChange={handleJdbcValueChange}
				/>
			))}
			<JdbcConnectionTestButton prefix={prefix} jdbcValues={jdbcValues} />
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
	const [path, setPath] = useState(element.value);
	const [showJdbcUrlBuilder, setShowJdbcUrlBuilder] = useState(false);
	const settings = useResourcesSettings();
	const labelText = prefix ? `-${prefix}.${element.name}` : `-${element.name}`;
	const id = prefix ? `${prefix}_${element.name}` : element.name;
	const isJdbcUrl = element.name === "jdbcUrl";
	const isJdbcProperties = element.name === "jdbcProperties";
	const resourceFiles = isJdbcProperties ? settings.jdbcFiles : [];
	const hasButton = isJdbcUrl || isJdbcProperties;

	useEffect(() => {
		onValueChange(element.name, path);
	}, [path, element.name, onValueChange]);

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
						value={path}
						list={isJdbcProperties ? `${id}_list` : undefined}
						handleChange={(ev) => setPath(ev.target.value)}
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
							path={path}
							setPath={setPath}
						/>
					)}
					{isJdbcUrl && (
						<JdbcUrlBuilderButton
							path={path}
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
				<SettingIcon title="JDBC URL Builder" fill="white" />
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
}: {
	prefix: string;
	jdbcValues: Record<string, string>;
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
			setResult(await jdbcConnectionTest(jdbcValues));
		} finally {
			setTesting(false);
		}
	};

	return (
		<div className="mt-2 flex items-center gap-3">
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
		</div>
	);
}
