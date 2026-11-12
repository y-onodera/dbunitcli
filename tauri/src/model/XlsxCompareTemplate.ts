// 比較用xlsxテンプレート（データシート2枚＋比較シート）の生成リクエスト（sidecarのXlsxCompareTemplateRequestDto）
export type ColumnPair = { first: string; second: string };

export type CompareSheet = { sheetName: string; columns: string[] };

export type XlsxCompareTemplateRequest = {
	fileName: string;
	compareSheetName: string;
	first: CompareSheet;
	second: CompareSheet;
	keys: ColumnPair[];
	compares: ColumnPair[];
};

// データセット1つ分の入力: 読み込んだテーブルとその列、シート名
export type DatasetColumns = {
	tableName: string;
	sheetName: string;
	columns: string[];
};

export const EMPTY_DATASET_COLUMNS: DatasetColumns = {
	tableName: "",
	sheetName: "",
	columns: [],
};

export type XlsxCompareTemplateInput = {
	fileName: string;
	compareSheetName: string;
	first: DatasetColumns;
	// nullなら1データセット: 2枚目のシートもfirstと同じ列で作る
	second: DatasetColumns | null;
	secondSheetName: string;
	// 1データセットのときはfirstの列名を両シートで使う
	keys: ColumnPair[];
	compares: ColumnPair[];
};

const isFilled = (pair: ColumnPair) => pair.first !== "" && pair.second !== "";

// テーブルを選び直したとき、シート名が未入力か前のテーブル名のままなら新しいテーブル名に合わせる
export function selectTable(
	current: DatasetColumns,
	tableName: string,
): DatasetColumns {
	const followsTable =
		current.sheetName === "" || current.sheetName === current.tableName;
	return {
		tableName,
		sheetName: followsTable ? tableName : current.sheetName,
		columns: [],
	};
}

// 1データセットのときの比較ペア: キー以外の全列を同名どうしで比べる
export function sameNamePairs(
	columns: string[],
	keyColumns: string[],
): ColumnPair[] {
	return columns
		.filter((column) => !keyColumns.includes(column))
		.map((column) => ({ first: column, second: column }));
}

// 入力からリクエストを作る。足りない入力はエラーとして投げる（ダイアログのエラー表示に出す）
export function buildCompareTemplateRequest(
	input: XlsxCompareTemplateInput,
): XlsxCompareTemplateRequest {
	if (input.first.columns.length === 0) {
		throw new Error("Load the columns of the old dataset.");
	}
	if (input.second && input.second.columns.length === 0) {
		throw new Error("Load the columns of the new dataset.");
	}
	const keys = input.keys.filter(isFilled);
	if (keys.length === 0) {
		throw new Error("Select at least one key column.");
	}
	const second = input.second ?? {
		...input.first,
		sheetName: input.secondSheetName,
	};
	const compares = input.second
		? input.compares.filter(isFilled)
		: sameNamePairs(
				input.first.columns,
				keys.map((key) => key.first),
			);
	return {
		fileName: input.fileName,
		compareSheetName: input.compareSheetName,
		first: { sheetName: input.first.sheetName, columns: input.first.columns },
		second: { sheetName: second.sheetName, columns: second.columns },
		keys,
		compares,
	};
}
