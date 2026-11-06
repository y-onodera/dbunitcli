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
import type { QueryDatasourceType } from "../../../../model/QueryDatasource";
import {
	insertAtCursor,
	insertInlineAtCursor,
} from "../../../../utils/textInsert";
import CsvReadInsertDialog from "./CsvReadInsertDialog";
import { InlinePreviewButton } from "./DatasetTableNamesPreviewButton";
import type {
	EditResourceDialogProps,
	EncodingProp,
} from "./ResourceEditButton";
import SqlTableInsertDialog from "./SqlTableInsertDialog";

// queryOnlyはデータセットの読込元のSQL。テーブル・列の挿入の用途からDMLを除く
type SqlEditorOptions = EncodingProp & { queryOnly?: boolean };

type SqlEditorDialogProps = EditResourceDialogProps &
	SqlEditorOptions & {
		type: QueryDatasourceType;
	};

export default function SqlEditorDialog(props: SqlEditorDialogProps) {
	const { content, loading } = useResourceContent(
		props.type,
		props.fileName,
		props.encoding,
	);
	if (loading) {
		return <StatusMessage kind="loading">Loading...</StatusMessage>;
	}
	return <Dialog {...props} value={content} />;
}

// EditResourceButtonへ渡すため、srcTypeごとに種類を固定した編集ダイアログ（モジュールで1度だけ作り、再マウントを防ぐ）
function sqlEditorDialogOf(
	type: QueryDatasourceType,
): ComponentType<EditResourceDialogProps & SqlEditorOptions> {
	return (props) => <SqlEditorDialog type={type} {...props} />;
}
export const sqlEditorDialogs: Record<
	QueryDatasourceType,
	ComponentType<EditResourceDialogProps & SqlEditorOptions>
> = {
	sql: sqlEditorDialogOf("sql"),
	table: sqlEditorDialogOf("table"),
	csvq: sqlEditorDialogOf("csvq"),
};

function Dialog(props: SqlEditorDialogProps & { value: string }) {
	const [content, setContent] = useState<string>(props.value);
	const [showSelector, setShowSelector] = useState(false);
	const textareaRef = useRef<HTMLTextAreaElement>(null);
	const pendingCursorRef = useRef<number | null>(null);
	const { jdbcValues, connectionOk } = useJdbcConnectionState();

	const datasetSrcInfo = useDatasetSrcInfo();
	const saveDataSource = useSaveResourceContent(props.type, props.encoding);

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

	const handleInsert = (lines: string[], inline?: boolean) => {
		insertToContent(lines, inline);
		setShowSelector(false);
	};

	const handleInsertColumn = (columns: string[], inline?: boolean) => {
		insertToContent(columns, inline);
	};

	// csvqはDBではなくCSVを読むため、DBのテーブル一覧の代わりにCSVファイルを選ぶ
	const csvq = props.type === "csvq";
	const table = props.type === "table";

	function renderSelector() {
		if (csvq) {
			return (
				// CSVREADには文字コードの明示が要るため、encoding未指定ならUTF-8を初期値にする
				<CsvReadInsertDialog
					encoding={datasetSrcInfo.values.encoding || "UTF-8"}
					onInsert={handleInsert}
					onClose={() => setShowSelector(false)}
				/>
			);
		}
		return (
			<SqlTableInsertDialog
				jdbcValues={jdbcValues}
				onInsert={handleInsert}
				onInsertColumn={handleInsertColumn}
				plain={table}
				queryOnly={props.queryOnly}
				onClose={() => setShowSelector(false)}
			/>
		);
	}

	return (
		<SettingDialog
			title={table ? "Edit Table Definition" : "Edit SQL"}
			helpCommand="dialogs/sql-editor"
			helpLabel="SQL Editor"
			handleDialogClose={props.handleDialogClose}
			fileName={props.fileName}
			save={(path) => saveDataSource(path, content)}
			handleSave={props.handleSave}
			footerActions={() => (
				// 保存せず、編集中の内容をsrcのファイルの代わりにしてプレビューする（settingは適用しない）。
				// sql・csvqはSQL1つで1テーブルなので、一覧を挟まずにそのテーブルを開く
				<InlinePreviewButton
					toInline={() => ({ inlineSource: content })}
					singleTable={!table}
				/>
			)}
		>
			<DialogBody size="md">
				{(csvq || connectionOk) && (
					<DialogField>
						<BlueButton
							title={csvq ? "Select CSV" : "Select Tables"}
							handleClick={() => setShowSelector(true)}
						/>
					</DialogField>
				)}
				<TextArea
					id="contents"
					ref={textareaRef}
					value={content}
					onChange={(e) => setContent(e.target.value)}
					placeholder={
						table ? "Enter table definition..." : "Enter SQL query..."
					}
					spellCheck={false}
				/>
			</DialogBody>
			{showSelector && renderSelector()}
		</SettingDialog>
	);
}
