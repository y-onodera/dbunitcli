import { removeArray, replaceArray } from "../utils/arrayUtils";
import { type Pattern, patternText, toPattern } from "./DatasetSettings";

// シートを名前でなくパターンで指定する。一致の判定は core の TableMetaDataFilterParser.parsePattern を参照。
// filePathはシート名の判定に使われないため持たない
export type SheetPattern = Omit<Pattern, "filePath">;

export interface RowSettingBuilder {
	sheetName?: string;
	sheetPattern?: SheetPattern;
	tableName?: string;
	header?: string[];
	dataStart?: string;
	columnIndex?: string[];
	breakKey?: string[];
	addFileInfo?: boolean;
}

export interface CellSettingBuilder {
	sheetName?: string;
	sheetPattern?: SheetPattern;
	tableName?: string;
	header?: string[];
	rows?: { cellAddress: string[] }[];
	addFileInfo?: boolean;
}

export class RowSetting {
	sheetName: string;
	sheetPattern?: SheetPattern;
	tableName: string;
	header: string[];
	dataStart: string;
	columnIndex: string[];
	breakKey: string[];
	addFileInfo: boolean;

	static create(): RowSetting {
		return new RowSetting({});
	}

	constructor(builder: RowSettingBuilder) {
		this.sheetName = builder.sheetName ?? "";
		this.sheetPattern = builder.sheetPattern;
		this.tableName = builder.tableName ?? "";
		this.header = builder.header ?? [];
		this.dataStart = builder.dataStart ?? "";
		this.columnIndex = builder.columnIndex ?? [];
		this.breakKey = builder.breakKey ?? [];
		this.addFileInfo = builder.addFileInfo ?? false;
	}

	with(builder: RowSettingBuilder): RowSetting {
		return new RowSetting({
			...this,
			...builder,
		});
	}
	displayName(): string {
		return settingDisplayName(this);
	}
}

export class CellSetting {
	sheetName: string;
	sheetPattern?: SheetPattern;
	tableName: string;
	header: string[];
	rows: { cellAddress: string[] }[];
	addFileInfo: boolean;

	static create(): CellSetting {
		return new CellSetting({});
	}

	constructor(builder: CellSettingBuilder) {
		this.sheetName = builder.sheetName ?? "";
		this.sheetPattern = builder.sheetPattern;
		this.tableName = builder.tableName ?? "";
		this.header = builder.header ?? [];
		this.rows = builder.rows ?? [];
		this.addFileInfo = builder.addFileInfo ?? false;
	}

	replaceRows(newRows: string[], index: number): CellSetting {
		if (newRows.length === 0) {
			return this.with({
				rows: removeArray(this.rows, index),
			});
		}
		return this.with({
			rows: replaceArray(this.rows, { cellAddress: newRows }, index),
		});
	}

	with(builder: CellSettingBuilder): CellSetting {
		return new CellSetting({
			...this,
			...builder,
		});
	}

	displayName(): string {
		return settingDisplayName(this);
	}
}

export class XlsxSchema {
	rows: RowSetting[];
	cells: CellSetting[];

	constructor(rows: RowSetting[], cells: CellSetting[]) {
		this.rows = rows;
		this.cells = cells;
	}

	static create(): XlsxSchema {
		return XlsxSchema.build({});
	}

	static build(schema: XlsxSchemaBuilder): XlsxSchema {
		const patterns = schema.patterns ?? {};
		const patternKeys = new Set(Object.keys(patterns));
		const withPattern = <T extends { sheetName?: string }>(builder: T) =>
			patternKeys.has(builder.sheetName ?? "")
				? {
						...builder,
						sheetName: "",
						sheetPattern: toSheetPattern(patterns[builder.sheetName ?? ""]),
					}
				: builder;
		return new XlsxSchema(
			schema.rows?.map((builder) => new RowSetting(withPattern(builder))) || [
				RowSetting.create(),
			],
			schema.cells?.map((builder) => new CellSetting(withPattern(builder))) || [
				CellSetting.create(),
			],
		);
	}

	addRowSetting(setting: RowSetting): XlsxSchema {
		return new XlsxSchema(this.rows.concat(setting), this.cells);
	}

	deleteRowSetting(setting: RowSetting): XlsxSchema {
		const newRows = this.rows.filter((it) => it !== setting);
		return new XlsxSchema(newRows, this.cells);
	}

	updateRowSetting(before: RowSetting, after: RowSetting): XlsxSchema {
		return new XlsxSchema(
			this.rows.map((it) => (it === before ? after : it)),
			this.cells,
		);
	}

	addCellSetting(setting: CellSetting): XlsxSchema {
		return new XlsxSchema(this.rows, this.cells.concat(setting));
	}

	deleteCellSetting(setting: CellSetting): XlsxSchema {
		const newCells = this.cells.filter((it) => it !== setting);
		return new XlsxSchema(this.rows, newCells);
	}

	updateCellSetting(before: CellSetting, after: CellSetting): XlsxSchema {
		return new XlsxSchema(
			this.rows,
			this.cells.map((it) => (it === before ? after : it)),
		);
	}
	// sheetPatternはcoreの形式(トップレベルのpatternsに登録し、sheetNameでそのキーを参照する)にして保存する
	toJSON() {
		const patterns: Record<string, SheetPattern> = {};
		const toSaved = <T extends SheetTarget>(settings: T[]) =>
			settings.flatMap(({ sheetPattern, ...rest }) => {
				const sheet = savedSheet({ ...rest, sheetPattern });
				if (!rest.tableName || !sheet.sheetName) {
					return [];
				}
				if (sheet.pattern) {
					patterns[sheet.sheetName] = sheet.pattern;
				}
				return [{ ...rest, sheetName: sheet.sheetName }];
			});
		const rows = toSaved(this.rows);
		const cells = toSaved(this.cells);
		return {
			...(Object.keys(patterns).length > 0 ? { patterns } : {}),
			rows,
			cells,
		};
	}
}
export interface XlsxSchemaBuilder {
	patterns?: Record<string, string | string[] | SheetPattern>;
	rows?: RowSettingBuilder[];
	cells?: CellSettingBuilder[];
}

export function createRowSetting(): RowSetting {
	return RowSetting.create();
}

export function createCellSetting(): CellSetting {
	return CellSetting.create();
}

type SheetTarget = {
	sheetName: string;
	sheetPattern?: SheetPattern;
	tableName: string;
};

// 保存するsheetName(パターン指定ならpatternsのキー)と、patternsに登録するパターン。未指定ならsheetNameは空文字
function savedSheet(setting: SheetTarget): {
	sheetName: string;
	pattern?: SheetPattern;
} {
	if (!setting.sheetPattern) {
		return { sheetName: setting.sheetName };
	}
	const pattern = normalizeSheetPattern(setting.sheetPattern);
	return pattern
		? { sheetName: patternKey(pattern), pattern }
		: { sheetName: "" };
}

function settingDisplayName(setting: SheetTarget): string {
	const sheet = savedSheet(setting).sheetName;
	if (!sheet || !setting.tableName) {
		return "";
	}
	return `${sheet}→${setting.tableName}`;
}

// coreはregexを優先するので保存時はどちらか一方にする。excludeの空要素は除く
function normalizeSheetPattern(
	pattern: SheetPattern | undefined,
): SheetPattern | undefined {
	if (!pattern) {
		return undefined;
	}
	const exclude = pattern.exclude?.filter((it) => it);
	if (!patternText(pattern)) {
		return undefined;
	}
	const base = pattern.regex
		? { regex: pattern.regex }
		: { string: pattern.string };
	return exclude?.length ? { ...base, exclude } : base;
}

// patternsのキー。sheetNameの完全一致がpatternsより先に判定されるため、実在するシート名と重ならない書式にする
function patternKey(pattern: SheetPattern): string {
	const base = pattern.regex
		? `regex:${pattern.regex}`
		: `string:${pattern.string}`;
	return pattern.exclude?.length
		? `${base} exclude:${pattern.exclude.join(",")}`
		: base;
}

// 配列の指定は画面で編集できないため先頭だけを使う
function toSheetPattern(
	pattern: string | string[] | SheetPattern,
): SheetPattern {
	if (Array.isArray(pattern)) {
		return { string: pattern[0] ?? "" };
	}
	return toPattern(pattern) ?? {};
}

type SheetTargetSetting<T> = {
	sheetName: string;
	sheetPattern?: SheetPattern;
	with(builder: { sheetName?: string; sheetPattern?: SheetPattern }): T;
};

// name/patternの切替。入力中のシート名とstringを相互に引き継ぐ
export function switchSheetTarget<T extends SheetTargetSetting<T>>(
	setting: T,
	kind: string,
): T {
	const isPattern = kind === "pattern";
	if (isPattern === !!setting.sheetPattern) {
		return setting;
	}
	if (isPattern) {
		return setting.with({
			sheetPattern: { string: setting.sheetName },
			sheetName: "",
		});
	}
	return setting.with({
		sheetPattern: undefined,
		sheetName: patternText(setting.sheetPattern),
	});
}

export function replaceSheetPattern<T extends SheetTargetSetting<T>>(
	setting: T,
	change: SheetPattern,
): T {
	return setting.with({ sheetPattern: { ...setting.sheetPattern, ...change } });
}
