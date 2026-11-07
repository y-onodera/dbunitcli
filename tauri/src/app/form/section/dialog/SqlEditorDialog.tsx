import { type ComponentType, useState } from "react";
import {
	DialogBody,
	DialogField,
	DialogTextArea,
	SettingDialog,
	StatusMessage,
} from "../../../../components/dialog";
import { BlueButton } from "../../../../components/element/Button";
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
import type { EditResourceDialogProps } from "./ResourceEditButton";
import SqlTableInsertDialog from "./SqlTableInsertDialog";
import { useTextareaInsert } from "./useTextareaInsert";

// queryOnlyはデータセットの読込元のSQL。テーブル・列の挿入の用途からDMLを除く
type SqlEditorOptions = { queryOnly?: boolean };

type SqlEditorDialogProps = EditResourceDialogProps &
	SqlEditorOptions & {
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
	const {
		value: content,
		setValue: setContent,
		textareaRef,
		insert,
	} = useTextareaInsert(props.value);
	const [showSelector, setShowSelector] = useState(false);
	const { jdbcValues, connectionOk } = useJdbcConnectionState();
	const saveDataSource = useSaveResourceContent(props.type);

	const datasetSrcInfo = useDatasetSrcInfo();

	const insertToContent = (lines: string[], inline = false) =>
		insert((current, start, end) =>
			inline
				? insertInlineAtCursor(current, lines, start, end)
				: insertAtCursor(current, lines.join("\n"), start, end),
		);

	const handleInsert = (lines: string[], inline?: boolean) => {
		insertToContent(lines, inline);
		setShowSelector(false);
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
				onInsertColumn={insertToContent}
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
			maximizable
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
				<DialogTextArea
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
