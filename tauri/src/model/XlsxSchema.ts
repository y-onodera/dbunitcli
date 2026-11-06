// シートを名前でなくパターンで指定する。一致の判定は core の TableMetaDataFilterParser.parsePattern を参照
export type SheetPattern = {
	string?: string;
	regex?: string;
	exclude?: string[];
};

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
		this.sheetName = builder.sheetName || "";
		this.sheetPattern = builder.sheetPattern;
		this.tableName = builder.tableName || "";
		this.header = builder.header || [];
		this.dataStart = builder.dataStart || "";
		this.columnIndex = builder.columnIndex || [];
		this.breakKey = builder.breakKey || [];
		this.addFileInfo = builder.addFileInfo || false;
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
		this.sheetName = builder?.sheetName || "";
		this.sheetPattern = builder?.sheetPattern;
		this.tableName = builder?.tableName || "";
		this.header = builder?.header || [];
		this.rows = builder?.rows || [];
		this.addFileInfo = builder?.addFileInfo || false;
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
		const withPattern = <T extends { sheetName?: string }>(builder: T) =>
			resolveSheetPattern(builder, schema.patterns);
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
			settings
				.filter((setting) => setting.tableName && sheetKey(setting))
				.map(({ sheetPattern, ...rest }) => {
					const pattern = normalizeSheetPattern(sheetPattern);
					if (!pattern) {
						return rest;
					}
					const key = patternKey(pattern);
					patterns[key] = pattern;
					return { ...rest, sheetName: key };
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
function replaceArray<T>(array: T[], newOne: T, index: number): T[] {
	let result = [...array];
	if (result.length > index) {
		if (newOne) {
			result[index] = newOne;
		} else {
			result = removeArray(result, index);
		}
	} else if (newOne) {
		result = result.concat(newOne);
	}
	return result;
}
function removeArray<T>(array: T[], remove: number): T[] {
	return [...array].filter((_, index) => index !== remove);
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

// シートの指定(名前またはパターン)を表す文字列。未指定なら空文字
function sheetKey(setting: SheetTarget): string {
	if (setting.sheetPattern) {
		const pattern = normalizeSheetPattern(setting.sheetPattern);
		return pattern ? patternKey(pattern) : "";
	}
	return setting.sheetName;
}

function settingDisplayName(setting: SheetTarget): string {
	const sheet = setting.sheetPattern ? sheetKey(setting) : setting.sheetName;
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
	if (!pattern.regex && !pattern.string) {
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

function resolveSheetPattern<T extends { sheetName?: string }>(
	builder: T,
	patterns: XlsxSchemaBuilder["patterns"],
): T & { sheetPattern?: SheetPattern } {
	const sheetName = builder.sheetName ?? "";
	if (!patterns || !Object.keys(patterns).includes(sheetName)) {
		return builder;
	}
	return {
		...builder,
		sheetName: "",
		sheetPattern: toSheetPattern(patterns[sheetName]),
	};
}

// 配列の指定は画面で編集できないため先頭だけを使う
function toSheetPattern(
	pattern: string | string[] | SheetPattern,
): SheetPattern {
	if (typeof pattern === "string") {
		return { string: pattern };
	}
	if (Array.isArray(pattern)) {
		return { string: pattern[0] ?? "" };
	}
	return pattern;
}
