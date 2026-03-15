import { useState } from "react";
import { SettingDialog } from "../../components/dialog";
import { useSaveDataSource } from "../../hooks/useQueryDatasource";
import type { QueryDatasourceType } from "../../model/QueryDatasource";
import { saveOnSuccess } from "../../utils/fetchUtils";

type SqlEditorDialogProps = {
	type: QueryDatasourceType;
	fileName: string;
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
	value: string;
};

export default function SqlEditorDialog(props: SqlEditorDialogProps) {
	const [content, setContent] = useState<string>(props.value);
	const saveDataSource = useSaveDataSource();

	const handleCommit = (path: string) =>
		saveOnSuccess(
			() => saveDataSource({ type: props.type, name: path, contents: content }),
			() => props.handleSave(path),
		);

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
		</SettingDialog>
	);
}
