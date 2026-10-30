import { type ComponentType, useLayoutEffect, useRef, useState } from "react";
import {
	DialogBody,
	DialogField,
	SettingDialog,
	StatusMessage,
} from "../../../../components/dialog";
import { BlueButton } from "../../../../components/element/Button";
import { TextArea } from "../../../../components/element/Input";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useJdbcConnectionState } from "../../../../context/JdbcConnectionProvider";
import {
	useResourceContent,
	useSaveResourceContent,
} from "../../../../hooks/useResourceFiles";
import { withoutSetting, withValues } from "../../../../model/CommandOption";
import type { QueryDatasourceType } from "../../../../model/QueryDatasource";
import { saveOnSuccess } from "../../../../utils/fetchUtils";
import { insertAtCursor } from "../../../../utils/textInsert";
import { DatasetTableNamesPreviewDialog } from "./DatasetTableNamesPreviewButton";
import type { EditResourceDialogProps } from "./ResourceEditButton";
import SqlTableInsertDialog from "./SqlTableInsertDialog";

type SqlEditorDialogProps = EditResourceDialogProps & {
	type: QueryDatasourceType;
};

export default function SqlEditorDialog(props: SqlEditorDialogProps) {
	const { content, loading } = useResourceContent(props.type, props.fileName);
	if (loading) {
		return <StatusMessage kind="loading">Loading...</StatusMessage>;
	}
	return <Dialog {...props} value={content} />;
}

// EditResourceButtonへ渡すため、srcTypeごとに種類を固定した編集ダイアログ（モジュールで1度だけ作り、再マウントを防ぐ）
function sqlEditorDialogOf(
	type: QueryDatasourceType,
): ComponentType<EditResourceDialogProps> {
	return (props) => <SqlEditorDialog type={type} {...props} />;
}
export const sqlEditorDialogs: Record<
	QueryDatasourceType,
	ComponentType<EditResourceDialogProps>
> = {
	sql: sqlEditorDialogOf("sql"),
	table: sqlEditorDialogOf("table"),
	csvq: sqlEditorDialogOf("csvq"),
};

function Dialog(props: SqlEditorDialogProps & { value: string }) {
	const [content, setContent] = useState<string>(props.value);
	const [showTableSelector, setShowTableSelector] = useState(false);
	const textareaRef = useRef<HTMLTextAreaElement>(null);
	const pendingCursorRef = useRef<number | null>(null);
	const saveDataSource = useSaveResourceContent(props.type);
	const { jdbcValues, connectionOk } = useJdbcConnectionState();

	const datasetSrcInfo = useDatasetSrcInfo();
	const [previewPath, setPreviewPath] = useState<string | null>(null);

	const saveThen = (onSaved: (path: string) => void) => (path: string) =>
		saveOnSuccess(
			() => saveDataSource(path, content),
			() => onSaved(path),
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
			handleSave={saveThen(props.handleSave)}
			footerActions={(fileName) => (
				// 保存したファイルをsrcにしてテーブル一覧をプレビューする（settingは適用しない）。編集ダイアログは開いたまま
				<BlueButton
					title="Save and Preview"
					handleClick={() => saveThen(setPreviewPath)(fileName)}
				/>
			)}
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
			{previewPath !== null && (
				<DatasetTableNamesPreviewDialog
					datasetSrcInfo={withValues(withoutSetting(datasetSrcInfo), {
						srcType: props.type,
						src: previewPath,
					})}
					handleDialogClose={() => setPreviewPath(null)}
				/>
			)}
		</SettingDialog>
	);
}
