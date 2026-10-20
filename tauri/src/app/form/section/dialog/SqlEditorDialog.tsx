import { useLayoutEffect, useRef, useState } from "react";
import {
	DialogBody,
	DialogField,
	SettingDialog,
} from "../../../../components/dialog";
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
import { insertAtCursor } from "../../../../utils/textInsert";
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
	const textareaRef = useRef<HTMLTextAreaElement>(null);
	const pendingCursorRef = useRef<number | null>(null);
	const saveDataSource = useSaveDataSource();
	const { jdbcValues, connectionOk } = useJdbcConnectionState();

	const handleCommit = (path: string) =>
		saveOnSuccess(
			() => saveDataSource({ name: path, contents: content }),
			() => props.handleSave(path),
		);

	const insertToContent = (text: string) => {
		const textarea = textareaRef.current;
		const start = textarea?.selectionStart ?? content.length;
		const end = textarea?.selectionEnd ?? content.length;
		const result = insertAtCursor(content, text, start, end);
		pendingCursorRef.current = result.cursor;
		setContent(result.value);
	};

	useLayoutEffect(() => {
		const cursor = pendingCursorRef.current;
		if (cursor === null) {
			return;
		}
		pendingCursorRef.current = null;
		textareaRef.current?.setSelectionRange(cursor, cursor);
	});

	const handleInsert = (tables: string[]) => {
		insertToContent(tables.join("\n"));
		setShowTableSelector(false);
	};

	const handleInsertColumn = (columns: string[]) => {
		insertToContent(columns.join("\n"));
	};

	return (
		<SettingDialog
			title={props.type === "sql" ? "Edit SQL" : "Edit Table Definition"}
			helpCommand="dialogs/sql-editor"
			helpLabel="SQL Editor"
			handleDialogClose={props.handleDialogClose}
			fileName={props.fileName}
			handleSave={handleCommit}
		>
			<DialogBody size="md">
				{connectionOk && (
					<DialogField>
						<BlueButton
							title="Select Tables"
							handleClick={() => setShowTableSelector(true)}
						/>
					</DialogField>
				)}
				<TextArea
					id="contents"
					ref={textareaRef}
					value={content}
					onChange={(e) => setContent(e.target.value)}
					placeholder={
						props.type === "sql"
							? "Enter SQL query..."
							: "Enter table definition..."
					}
					spellCheck={false}
				/>
			</DialogBody>
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
