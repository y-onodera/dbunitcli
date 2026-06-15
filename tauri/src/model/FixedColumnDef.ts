export interface ColumnDefBuilder {
	name?: string;
	length?: number;
	align?: string;
	pad?: string;
}

export class ColumnDef {
	name: string;
	length: number;
	align: string;
	pad: string;

	static create(): ColumnDef {
		return new ColumnDef({});
	}

	constructor(builder: ColumnDefBuilder) {
		this.name = builder.name ?? "";
		this.length = builder.length ?? 0;
		this.align = builder.align ?? "left";
		this.pad = builder.pad ?? " ";
	}

	with(builder: ColumnDefBuilder): ColumnDef {
		return new ColumnDef({ ...this, ...builder });
	}

	displayName(): string {
		if (!this.name) {
			return "";
		}
		return `${this.name} (${this.length})`;
	}
}

export interface FixedColumnDefBuilder {
	columns?: ColumnDefBuilder[];
}

export class FixedColumnDef {
	columns: ColumnDef[];

	constructor(columns: ColumnDef[]) {
		this.columns = columns;
	}

	static create(): FixedColumnDef {
		return FixedColumnDef.build({});
	}

	static build(schema: FixedColumnDefBuilder): FixedColumnDef {
		return new FixedColumnDef(
			schema.columns?.map((b) => new ColumnDef(b)) ?? [ColumnDef.create()],
		);
	}

	toJSON() {
		return {
			columns: this.columns.filter((c) => c.name),
		};
	}
}

export function createColumnDef(): ColumnDef {
	return ColumnDef.create();
}
