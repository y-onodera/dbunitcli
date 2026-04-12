import { useState } from "react";
import { SettingDialog } from "../../../../components/dialog";
import { BlueButton } from "../../../../components/element/Button";
import { EditButton } from "../../../../components/element/ButtonIcon";
import { useJdbcConnectionState } from "../../../../context/JdbcConnectionProvider";
import {
	useDeleteDataSource,
	useLoadDataSource,
	useSaveDataSource,
} from "../../../../hooks/useQueryDatasource";
import type { QueryDatasourceType } from "../../../../model/QueryDatasource";
import { saveOnSuccess } from "../../../../utils/fetchUtils";
import {
	RemoveResource,
	type ResourceEditButtonProp,
} from "./ResourceEditButton";
import SqlTableInsertDialog from "./SqlTableInsertDialog";

type SqlEditorDialogProps = {
	type: QueryDatasourceType;
	fileName: string;
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
	value: string;
};

export default function SqlEditorDialog(props: SqlEditorDialogProps) {
	const [content, setContent] = useState<string>(props.value);
	const [showTableSelector, setShowTableSelector] = useState(false);
	const saveDataSource = useSaveDataSource();
	const { jdbcValues, connectionOk } = useJdbcConnectionState();

	const handleCommit = (path: string) =>
		saveOnSuccess(
			() => saveDataSource({ name: path, contents: content }),
			() => props.handleSave(path),
		);

	const handleInsert = (tables: string[]) => {
		const suffix = tables.join("\n");
		setContent((prev) => {
			if (prev === "" || prev.endsWith("\n")) {
				return prev + suffix;
			}
			return `${prev}\n${suffix}`;
		});
		setShowTableSelector(false);
	};

	return (
		<SettingDialog
			handleDialogClose={props.handleDialogClose}
			fileName={props.fileName}
			handleSave={handleCommit}
		>
			<div className="w-[640px] p-4">
				<h2 className="text-xl font-bold mb-4">
					{props.type === "sql" ? "Edit SQL" : "Edit Table Definition"}
				</h2>
				{connectionOk && (
					<div className="mb-2">
						<BlueButton
							title="Select Tables"
							handleClick={() => setShowTableSelector(true)}
						/>
					</div>
				)}
				<div className="relative">
					<textarea
						id="contents"
						className="w-full h-96 p-4 border border-gray-300 rounded-lg
                         font-mono text-base bg-gray-50
                         focus-visible:ring-3 ring-indigo-300"
						value={content}
						onChange={(e) => setContent(e.target.value)}
						placeholder={
							props.type === "sql"
								? "Enter SQL query..."
								: "Enter table definition..."
						}
						spellCheck={false}
					/>
				</div>
			</div>
			{showTableSelector && (
				<SqlTableInsertDialog
					jdbcValues={jdbcValues}
					onInsert={handleInsert}
					onClose={() => setShowTableSelector(false)}
				/>
			)}
		</SettingDialog>
	);
}
type SqlEditorButtonProps = ResourceEditButtonProp & {
	type: QueryDatasourceType;
};
export function SqlEditorButton({ path, setPath, type }: SqlEditorButtonProps) {
	const [showDialog, setShowDialog] = useState(false);
	const [content, setContent] = useState("");
	const loadDataSource = useLoadDataSource();
	const handleOpen = async () => {
		try {
			if (path) {
				const result = await loadDataSource(path);
				setContent(result);
			}
			setShowDialog(true);
		} catch (ex) {
			alert(ex);
		}
	};

	const handleClose = () => {
		setShowDialog(false);
		setContent("");
	};

	const handleSave = (path: string) => {
		setPath(path);
		setShowDialog(false);
		setContent("");
	};

	return (
		<>
			<EditButton handleClick={handleOpen} />
			{showDialog && (
				<SqlEditorDialog
					type={type}
					fileName={path}
					value={content}
					handleDialogClose={handleClose}
					handleSave={handleSave}
				/>
			)}
		</>
	);
}
export function RemoveSqlEditorButton({
	path,
	setPath,
	type,
}: SqlEditorButtonProps) {
	const deleteDataSource = useDeleteDataSource(type);

	return (
		<RemoveResource
			path={path}
			setPath={setPath}
			deleteResource={deleteDataSource}
		/>
	);
}
