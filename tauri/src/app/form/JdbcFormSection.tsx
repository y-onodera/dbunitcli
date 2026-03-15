import {
	type Dispatch,
	type SetStateAction,
	useCallback,
	useEffect,
	useRef,
	useState,
} from "react";
import { BlueButton } from "../../components/element/Button";
import { BlueEditButton, BlueSettingButton, PreviewButton } from "../../components/element/ButtonIcon";
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
						<JdbcPropertiesDropDownMenu
							prefix={prefix}
							element={element}
							path={value}
							setPath={setPath}
							onApplyValues={onApplyValues}
						/>
					)}
					{isJdbcUrl && (
						<JdbcUrlBuilderButton
							path={value}
							setPath={setPath}
						/>
					)}
				</div>
			</div>
		</div>
	);
}

function JdbcPropertiesPreviewButton({
	path,
	onApplyValues,
}: {
	path: string;
	onApplyValues: (values: Partial<Record<string, string>>) => void;
}) {
	const [showDialog, setShowDialog] = useState(false);
	return (
		<>
			<PreviewButton handleClick={() => setShowDialog(true)} />
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

function JdbcPropertiesDropDownMenu({
	prefix,
	element,
	path,
	setPath,
	onApplyValues,
}: {
	prefix: string;
	element: CommandParam;
	path: string;
	setPath: Dispatch<SetStateAction<string>>;
	onApplyValues?: (values: Partial<Record<string, string>>) => void;
}) {
	const settings = useResourcesSettings();
	const isValueInDatalist = settings.jdbcFiles.includes(path);
	const [showMenu, setShowMenu] = useState(false);
	const buttonRef = useRef<HTMLDivElement>(null);
	const menuRef = useRef<HTMLDivElement>(null);
	const [menuPosition, setMenuPosition] = useState<"right" | "left">("right");

	useEffect(() => {
		if (showMenu && buttonRef.current) {
			const rect = buttonRef.current.getBoundingClientRect();
			const menuWidth = 96;
			if (rect.right + menuWidth > window.innerWidth) {
				setMenuPosition("left");
			} else {
				setMenuPosition("right");
			}
		}
		function handleClickOutside(event: MouseEvent) {
			if (
				menuRef.current &&
				!menuRef.current.contains(event.target as Node) &&
				buttonRef.current &&
				!buttonRef.current.contains(event.target as Node)
			) {
				setShowMenu(false);
			}
		}
		if (showMenu) {
			document.addEventListener("mousedown", handleClickOutside);
			return () => {
				document.removeEventListener("mousedown", handleClickOutside);
			};
		}
	}, [showMenu]);

	return (
		<div className="relative mr-24" ref={buttonRef}>
			<BlueSettingButton handleClick={() => setShowMenu(!showMenu)} />
			{showMenu && (
				<div
					ref={menuRef}
					className="absolute z-50 p-4 text-gray-900 bg-white border border-gray-100 rounded-lg shadow-md"
					style={{
						...(menuPosition === "right"
							? { left: "100%", top: 0 }
							: { right: "100%", top: 0 }),
					}}
				>
					<ul className="space-y-4">
						{path && onApplyValues && (
							<li>
								<JdbcPropertiesPreviewButton
									path={path}
									onApplyValues={onApplyValues}
								/>
							</li>
						)}
						{path && isValueInDatalist && (
							<li>
								<RemoveJdbcPropertiesButton
									path={path}
									setPath={(value) => {
										setPath(value);
										setShowMenu(false);
									}}
								/>
							</li>
						)}
						<li>
							<FileChooser
								prefix={prefix}
								element={element}
								path={path}
								setPath={setPath}
								onSelect={() => setShowMenu(false)}
							/>
						</li>
					</ul>
				</div>
			)}
		</div>
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
