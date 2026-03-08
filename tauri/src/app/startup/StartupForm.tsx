import { open } from "@tauri-apps/plugin-dialog";
import type React from "react";
import {
	type Dispatch,
	type SetStateAction,
	useCallback,
	useRef,
	useState,
} from "react";
import { BlueButton, ButtonWithIcon } from "../../components/element/Button";
import { DirIcon } from "../../components/element/Icon";
import { ControllTextBox, InputLabel } from "../../components/element/Input";
import { useWorkspaceContext } from "../../context/WorkspaceResourcesProvider";
import { useWorkspaceUpdate } from "../../hooks/useWorkspaceResources";

const StartupForm: React.FC<{ onSelect: () => void }> = ({ onSelect }) => {
	const context = useWorkspaceContext();
	const workspaceUpdate = useWorkspaceUpdate();
	const [workspace, setWorkspace] = useState(context.workspace);
	const [datasetBase, setDatasetBase] = useState(context.datasetBase);
	const [resultBase, setResultBase] = useState(context.resultBase);
	const [errors, setErrors] = useState<{ [key: string]: string }>({});
	const prevWorkspaceRef = useRef(workspace);

	const updateWorkspace = useCallback(
		(newWorkspace: SetStateAction<string>) => {
			const oldWorkspace = prevWorkspaceRef.current;
			const resolved =
				typeof newWorkspace === "function"
					? newWorkspace(oldWorkspace)
					: newWorkspace;
			if (oldWorkspace) {
				setDatasetBase((db) =>
					db.startsWith(oldWorkspace)
						? resolved + db.slice(oldWorkspace.length)
						: db,
				);
				setResultBase((rb) =>
					rb.startsWith(oldWorkspace)
						? resolved + rb.slice(oldWorkspace.length)
						: rb,
				);
			}
			prevWorkspaceRef.current = resolved;
			setWorkspace(resolved);
		},
		[],
	);

	const handleSelect = async () => {
		const newErrors: { [key: string]: string } = {};
		if (!workspace) {
			newErrors.workspace = "Workspace path is required.";
		}
		setErrors(newErrors);

		if (Object.keys(newErrors).length === 0) {
			workspaceUpdate(workspace, datasetBase, resultBase);
			onSelect();
		}
	};

	return (
		<>
			<div className="grid grid-cols-6 justify-center">
				<InputField
					id="workspace"
					label="Workspace"
					required={true}
					value={workspace}
					setValue={updateWorkspace}
					error={errors.workspace}
				/>
				<InputField
					id="datasetBase"
					label="Dataset Base"
					required={false}
					value={datasetBase}
					setValue={setDatasetBase}
					error={errors.datasetBase}
				/>
				<InputField
					id="resultBase"
					label="Result Base"
					required={false}
					value={resultBase}
					setValue={setResultBase}
					error={errors.resultBase}
				/>
			</div>
			<div className="grid grid-cols-6">
				<div className="col-start-5 flex items-center justify-end">
					<BlueButton title="confirm" handleClick={() => handleSelect()} />
				</div>
			</div>
		</>
	);
};

const InputField: React.FC<{
	id: string;
	label: string;
	required: boolean;
	value: string;
	setValue: Dispatch<SetStateAction<string>>;
	error?: string;
}> = ({ id, label, required, value, setValue, error }) => {
	return (
		<>
			<InputLabel
				id={`${id}_label`}
				text={label}
				required={required}
				wStyle="col-start-1 p-2.5 w=1/6"
			/>
			<div className="flex col-start-2 col-span-4 p-2">
				<ControllTextBox
					name={id}
					id={id}
					required={required}
					value={value}
					handleChange={(e) => setValue(e.target.value)}
				/>
				<DirectoryChooser id={id} text={value} setPath={setValue} />
			</div>
			{error && (
				<div className="col-start-2 col-span-4 text-red-500">{error}</div>
			)}
		</>
	);
};

function DirectoryChooser(prop: {
	id: string;
	text: string;
	setPath: Dispatch<SetStateAction<string>>;
}) {
	const handleDirectoryChooserClick = () => {
		open({ defaultPath: prop.text, directory: true }).then(
			(files) => files && prop.setPath(files as string),
		);
	};
	return (
		<ButtonWithIcon
			handleClick={handleDirectoryChooserClick}
			id={`${prop.id}DirectoryChooser`}
		>
			<DirIcon title="DirectoryChooser" fill="white" />
		</ButtonWithIcon>
	);
}

export default StartupForm;
