import { useState } from "react";
import { DialogTitle, SettingDialog } from "../../../../components/dialog";
import { BlueButton } from "../../../../components/element/Button";
import { EditButton } from "../../../../components/element/ButtonIcon";
import { TextArea } from "../../../../components/element/Input";
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

	const appendToContent = (text: string) => {
		setContent((prev) =>
			prev === "" || prev.endsWith("\n") ? prev + text : `${prev}\n${text}`,
		);
	};

	const handleInsert = (tables: string[]) => {
		appendToContent(tables.join("\n"));
		setShowTableSelector(false);
	};

	const handleInsertColumn = (column: string) => {
		appendToContent(column);
	};

	return (
		<SettingDialog
			handleDialogClose={props.handleDialogClose}
			fileName={props.fileName}
			handleSave={handleCommit}
		>
			<div className="w-[640px] p-4">
				<DialogTitle>
					{props.type === "sql" ? "Edit SQL" : "Edit Table Definition"}
				</DialogTitle>
				{connectionOk && (
					<div className="mb-2">
						<BlueButton
							title="Select Tables"
							handleClick={() => setShowTableSelector(true)}
						/>
					</div>
				)}
				<TextArea
					id="contents"
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
			{showTableSelector && (
				<SqlTableInsertDialog
					jdbcValues={jdbcValues}
					onInsert={handleInsert}
					onInsertColumn={handleInsertColumn}
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
