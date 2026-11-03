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
import {
	insertAtCursor,
	insertInlineAtCursor,
} from "../../../../utils/textInsert";
import CsvReadInsertDialog from "./CsvReadInsertDialog";
import { SaveAndPreviewButton } from "./DatasetTableNamesPreviewButton";
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
	const [showCsvSelector, setShowCsvSelector] = useState(false);
	const textareaRef = useRef<HTMLTextAreaElement>(null);
	const pendingCursorRef = useRef<number | null>(null);
	const saveDataSource = useSaveResourceContent(props.type);
	const { jdbcValues, connectionOk } = useJdbcConnectionState();

	const datasetSrcInfo = useDatasetSrcInfo();

	const insertToContent = (lines: string[], inline = false) => {
		const textarea = textareaRef.current;
		const start = textarea?.selectionStart ?? content.length;
		const end = textarea?.selectionEnd ?? content.length;
		const result = inline
			? insertInlineAtCursor(content, lines, start, end)
			: insertAtCursor(content, lines.join("\n"), start, end);
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
		insertToContent(tables);
		setShowTableSelector(false);
	};

	const handleInsertCsv = (lines: string[]) => {
		insertToContent(lines);
		setShowCsvSelector(false);
	};

	const handleInsertColumn = (columns: string[], inline?: boolean) => {
		insertToContent(columns, inline);
	};

	// csvqはDBではなくCSVを読むため、DBのテーブル一覧の代わりにCSVファイルを選ぶ
	function renderTableSelectorButton() {
		if (props.type === "csvq") {
			return (
				<DialogField>
					<BlueButton
						title="Select CSV"
						handleClick={() => setShowCsvSelector(true)}
					/>
				</DialogField>
			);
		}
		if (!connectionOk) {
			return null;
		}
		return (
			<DialogField>
				<BlueButton
					title="Select Tables"
					handleClick={() => setShowTableSelector(true)}
				/>
			</DialogField>
		);
	}

	return (
		<SettingDialog
			title={props.type === "table" ? "Edit Table Definition" : "Edit SQL"}
			helpCommand="dialogs/sql-editor"
			helpLabel="SQL Editor"
			handleDialogClose={props.handleDialogClose}
			fileName={props.fileName}
			save={(path) => saveDataSource(path, content)}
			handleSave={props.handleSave}
			footerActions={(saveThen) => (
				// 保存したファイルをsrcにしてプレビューする（settingは適用しない）
				<SaveAndPreviewButton
					saveThen={saveThen}
					toSrcInfo={(path) =>
						withValues(withoutSetting(datasetSrcInfo), {
							srcType: props.type,
							src: path,
						})
					}
				/>
			)}
		>
			<DialogBody size="md">
				{renderTableSelectorButton()}
				<TextArea
					id="contents"
					ref={textareaRef}
					value={content}
					onChange={(e) => setContent(e.target.value)}
					placeholder={
						props.type === "table"
							? "Enter table definition..."
							: "Enter SQL query..."
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
			{showCsvSelector && (
				// CSVREADには文字コードの明示が要るため、encoding未指定ならUTF-8を初期値にする
				<CsvReadInsertDialog
					encoding={datasetSrcInfo.values.encoding || "UTF-8"}
					onInsert={handleInsertCsv}
					onClose={() => setShowCsvSelector(false)}
				/>
			)}
		</SettingDialog>
	);
}
