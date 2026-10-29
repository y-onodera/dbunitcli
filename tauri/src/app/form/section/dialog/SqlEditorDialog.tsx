import { type ComponentType, useLayoutEffect, useRef, useState } from "react";
import {
	DialogBody,
	DialogField,
	SettingDialog,
	StatusMessage,
} from "../../../../components/dialog";
import { BlueButton } from "../../../../components/element/Button";
import { TextArea } from "../../../../components/element/Input";
import { useJdbcConnectionState } from "../../../../context/JdbcConnectionProvider";
import {
	useQueryDatasourceData,
	useSaveQueryDatasource,
} from "../../../../hooks/useQueryDatasource";
import type { QueryDatasourceType } from "../../../../model/QueryDatasource";
import { saveOnSuccess } from "../../../../utils/fetchUtils";
import { insertAtCursor } from "../../../../utils/textInsert";
import type { EditResourceDialogProps } from "./ResourceEditButton";
import SqlTableInsertDialog from "./SqlTableInsertDialog";

type SqlEditorDialogProps = EditResourceDialogProps & {
	type: QueryDatasourceType;
};

export default function SqlEditorDialog(props: SqlEditorDialogProps) {
	const { content, loading } = useQueryDatasourceData(
		props.type,
		props.fileName,
	);
	if (loading) {
		return <StatusMessage kind="loading">Loading...</StatusMessage>;
	}
	return <Dialog {...props} value={content} />;
}

// EditResourceButtonへ渡すため、srcTypeごとに種類を固定した編集ダイアログ
function SqlFileEditDialog(props: EditResourceDialogProps) {
	return <SqlEditorDialog type="sql" {...props} />;
}
function TableFileEditDialog(props: EditResourceDialogProps) {
	return <SqlEditorDialog type="table" {...props} />;
}
function CsvqFileEditDialog(props: EditResourceDialogProps) {
	return <SqlEditorDialog type="csvq" {...props} />;
}
export const sqlEditorDialogs: Record<
	QueryDatasourceType,
	ComponentType<EditResourceDialogProps>
> = {
	sql: SqlFileEditDialog,
	table: TableFileEditDialog,
	csvq: CsvqFileEditDialog,
};

function Dialog(props: SqlEditorDialogProps & { value: string }) {
	const [content, setContent] = useState<string>(props.value);
	const [showTableSelector, setShowTableSelector] = useState(false);
	const textareaRef = useRef<HTMLTextAreaElement>(null);
	const pendingCursorRef = useRef<number | null>(null);
	const saveDataSource = useSaveQueryDatasource(props.type);
	const { jdbcValues, connectionOk } = useJdbcConnectionState();

	const handleCommit = (path: string) =>
		saveOnSuccess(
			() => saveDataSource(path, content),
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
					plain={props.type === "table"}
					onClose={() => setShowTableSelector(false)}
				/>
			)}
		</SettingDialog>
	);
}
