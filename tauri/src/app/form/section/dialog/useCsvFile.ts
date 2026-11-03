import { useState } from "react";
import { csvReadExpression } from "../../../../utils/textInsert";

// CSVREADで読むファイル・文字コード・別名。
// aliasOfはファイル選択ボタンで選んだファイルから別名を作る。fieldsはCsvFileFieldsへ渡す
export function useCsvFile(
	encoding: string,
	aliasOf: (path: string) => string,
) {
	const [path, setPath] = useState("");
	const [charset, setCharset] = useState(encoding);
	const [alias, setAlias] = useState("");
	return {
		alias,
		setAlias,
		expression: csvReadExpression(path, charset),
		ready: path.trim() !== "" && charset.trim() !== "",
		fields: {
			path,
			charset,
			onPathChange: setPath,
			onChoose: (next: string) => {
				setPath(next);
				setAlias(aliasOf(next));
			},
			onCharsetChange: setCharset,
		},
	};
}
