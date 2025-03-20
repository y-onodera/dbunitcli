export interface RowSettingBuilder {
	sheetName?: string;
	tableName?: string;
	header?: string[];
	dataStart?: string;
	columnIndex?: string[];
	breakKey?: string[];
	addFileInfo?: boolean;
}

export interface CellSettingBuilder {
	sheetName?: string;
	tableName?: string;
	header?: string[];
	rows?: { cellAddress: string[] }[];
	addFileInfo?: boolean;
}

export class RowSetting {
	sheetName: string;
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
		if (!this.sheetName || !this.tableName) {
			return "";
		}
		return `${this.sheetName}→${this.tableName}`;
	}
}

export class CellSetting {
	sheetName: string;
	tableName: string;
	header: string[];
	rows: { cellAddress: string[] }[];
	addFileInfo: boolean;

	static create(): CellSetting {
		return new CellSetting({});
	}

	constructor(builder: CellSettingBuilder) {
		this.sheetName = builder?.sheetName || "";
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
		if (!this.sheetName || !this.tableName) {
			return "";
		}
		return `${this.sheetName}→${this.tableName}`;
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
		return new XlsxSchema(
			schema.rows?.map((builder) => new RowSetting(builder)) || [
				RowSetting.create(),
			],
			schema.cells?.map((builder) => new CellSetting(builder)) || [
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
	toJSON() {
		const filterSettings = <T extends { tableName: string; sheetName: string }>(
			settings: T[],
		) => settings.filter((setting) => setting.tableName && setting.sheetName);

		return {
			rows: filterSettings(this.rows),
			cells: filterSettings(this.cells),
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
	rows?: RowSettingBuilder[];
	cells?: CellSettingBuilder[];
}

export function createRowSetting(): RowSetting {
	return RowSetting.create();
}

export function createCellSetting(): CellSetting {
	return CellSetting.create();
}
