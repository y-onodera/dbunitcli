import type { SetStateAction } from "react";
import { DialogField } from "../../../../components/dialog";
import type { CommandOption } from "../../../../model/CommandOption";
import { FileChooser } from "../element/Chooser";
import { OptionText } from "./SqlInsertParts";

// csvq実行時にCSVREADの相対パスを探す基準（core: FileResources#searchDatasetBase）に合わせる
const CSV_FILE_ELEMENT: CommandOption = {
	name: "csvFile",
	value: "",
	optional: true,
	attribute: {
		type: "FILE",
		required: false,
		selectOption: [],
		defaultPath: "DATASET",
	},
};

// CSVREADで読むファイルと文字コード。onChooseはファイル選択ボタンで選んだとき（別名の付け直し用）
export default function CsvFileFields({
	idPrefix,
	path,
	charset,
	onPathChange,
	onChoose,
	onCharsetChange,
}: {
	idPrefix: string;
	path: string;
	charset: string;
	onPathChange: (value: string) => void;
	onChoose: (value: string) => void;
	onCharsetChange: (value: string) => void;
}) {
	const handleChoose = (value: SetStateAction<string>) => {
		onChoose(typeof value === "function" ? value(path) : value);
	};
	return (
		<>
			<DialogField>
				<div className="flex gap-2 items-end">
					<OptionText
						id={`${idPrefix}_file`}
						label="CSV file"
						required
						value={path}
						onChange={onPathChange}
					/>
					<div className="mb-1">
						<FileChooser
							prefix={idPrefix}
							element={CSV_FILE_ELEMENT}
							path={path}
							setPath={handleChoose}
						/>
					</div>
				</div>
			</DialogField>
			<DialogField>
				<OptionText
					id={`${idPrefix}_charset`}
					label="Charset"
					required
					value={charset}
					onChange={onCharsetChange}
				/>
			</DialogField>
		</>
	);
}
