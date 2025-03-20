export type Pattern = {
	string: string;
	exclude?: string[];
};
export type TableJoin = {
	left?: string;
	right?: string;
	column?: string[];
	on?: string;
};
export type Split = {
	prefix?: string;
	tableName?: string;
	suffix?: string;
	breakKey?: string[];
	filter?: string[];
	limit?: string;
};
export type DatasetSettingsBuilder = {
	settings: DatasetSettingBuilder[];
	commonSettings: DatasetSettingBuilder[];
};
export type DatasetSettingBuilder = {
	name?: string | string[];
	pattern?: string | Pattern;
	innerJoin?: TableJoin;
	outerJoin?: TableJoin;
	fullJoin?: TableJoin;
	separate?: DatasetSettingBuilder[];
	prefix?: string;
	tableName?: string;
	suffix?: string;
	split?: Split;
	keys?: string[];
	string?: object;
	number?: object;
	boolean?: object;
	function?: object;
	exclude?: string[];
	include?: string[];
	filter?: string[];
	distinct?: boolean;
	order?: string[];
};
export class DatasetSettings {
	static create(): DatasetSettings {
		return new DatasetSettings(
			[new DatasetSetting({})],
			[new DatasetSetting({})],
		);
	}
	constructor(
		readonly settings: DatasetSetting[],
		readonly commonSettings: DatasetSetting[],
	) {}

	static build(builder: DatasetSettingsBuilder): DatasetSettings {
		return new DatasetSettings(
			builder.settings?.length > 0
				? builder.settings.map((it) => new DatasetSetting(it))
				: [new DatasetSetting({})],
			builder.commonSettings?.length > 0
				? builder.commonSettings.map((it) => new DatasetSetting(it))
				: [new DatasetSetting({})],
		);
	}

	add(setting: DatasetSetting): DatasetSettings {
		return new DatasetSettings(
			this.settings.concat(setting),
			this.commonSettings,
		);
	}

	delete(setting: DatasetSetting): DatasetSettings {
		const newSettings = this.settings.filter((it) => it !== setting);
		return new DatasetSettings(
			newSettings.length > 0 ? newSettings : [new DatasetSetting({})],
			this.commonSettings,
		);
	}

	update(before: DatasetSetting, after: DatasetSetting): DatasetSettings {
		return new DatasetSettings(
			this.settings.map((it) => (it === before ? after : it)),
			this.commonSettings,
		);
	}

	addCommon(setting: DatasetSetting): DatasetSettings {
		return new DatasetSettings(
			this.settings,
			this.commonSettings.concat(setting),
		);
	}

	deleteCommon(setting: DatasetSetting): DatasetSettings {
		const newSettings = this.commonSettings.filter((it) => it !== setting);
		return new DatasetSettings(
			this.settings,
			newSettings.length > 0 ? newSettings : [new DatasetSetting({})],
		);
	}

	updateCommon(before: DatasetSetting, after: DatasetSetting): DatasetSettings {
		return new DatasetSettings(
			this.settings,
			this.commonSettings.map((it) => (it === before ? after : it)),
		);
	}
	toJSON() {
		const filterSettings = (settings: DatasetSetting[]) =>
			settings.filter((setting) => setting.displayName());

		return {
			settings: filterSettings(this.settings),
			commonSettings: filterSettings(this.commonSettings),
		};
	}
}
export class DatasetSetting {
	readonly separate: DatasetSetting[];
	readonly keys: string[];
	readonly exclude: string[];
	readonly include: string[];
	readonly filter: string[];
	readonly distinct: boolean;
	readonly order: string[];
	readonly string: object;
	readonly number: object;
	readonly boolean: object;
	readonly function: object;
	readonly name?: string[];
	readonly pattern?: Pattern;
	readonly innerJoin?: TableJoin;
	readonly outerJoin?: TableJoin;
	readonly fullJoin?: TableJoin;
	readonly prefix?: string;
	readonly tableName?: string;
	readonly suffix?: string;
	readonly split?: Split;

	constructor(builder: DatasetSettingBuilder) {
		this.separate = builder.separate
			? builder.separate.map((it) => new DatasetSetting(it))
			: [];
		this.keys = builder.keys ?? [];
		this.exclude = builder.exclude ?? [];
		this.include = builder.include ?? [];
		this.filter = builder.filter ?? [];
		this.distinct = builder.distinct ?? false;
		this.order = builder.order ?? [];
		this.string = builder.string ?? {};
		this.number = builder.number ?? {};
		this.boolean = builder.boolean ?? {};
		this.function = builder.function ?? {};
		this.name = builder.name
			? typeof builder.name === "string"
				? [builder.name]
				: builder.name
			: undefined;
		this.pattern = builder.pattern
			? typeof builder.pattern === "string"
				? ({ string: builder.pattern } as Pattern)
				: builder.pattern
			: undefined;
		this.innerJoin = builder.innerJoin;
		this.outerJoin = builder.outerJoin;
		this.fullJoin = builder.fullJoin;
		this.prefix = builder.prefix;
		this.tableName = builder.tableName;
		this.suffix = builder.suffix;
		this.split = builder.split;
	}

	replace(newTarget: string): DatasetSetting {
		if (this.handler() === newTarget) {
			return this;
		}
		if (this.join()) {
			const value =
				newTarget === "name" || newTarget === "pattern"
					? this.join()?.left
					: this.join();
			return this.replaceTarget({ [newTarget]: value });
		}
		const value =
			newTarget === "name"
				? (this.pattern?.string ?? [""])
				: newTarget === "pattern"
					? this.name
						? this.name[0]
						: ""
					: {
							left: this.name
								? this.name[0]
								: this.pattern
									? this.pattern.string
									: "",
						};
		return this.replaceTarget({ [newTarget]: value });
	}

	replaceName(newOne: string, index: number): DatasetSetting {
		return this.with({
			name: replaceArray(this.name ?? [], newOne, index),
		});
	}

	removeName(index: number): DatasetSetting {
		return this.with({
			name: removeArray(this.name ?? [], index),
		});
	}

	replacePattern(newVal: Pattern): DatasetSetting {
		return this.with({
			pattern: { ...this.pattern, ...newVal },
		});
	}

	replacePatternExclude(newOne: string, index: number): DatasetSetting {
		return this.replacePattern({
			string: this.pattern?.string ?? "",
			exclude: replaceArray(this.pattern?.exclude ?? [], newOne, index),
		});
	}

	removePatternExclude(index: number): DatasetSetting {
		return this.replacePattern({
			string: this.pattern?.string ?? "",
			exclude: removeArray(this.pattern?.exclude ?? [], index),
		});
	}

	replaceJoin(newVal: TableJoin): DatasetSetting {
		return this.replaceTarget({
			[this.handler()]: { ...this.join(), ...newVal },
		});
	}

	replaceJoinColumn(newOne: string, index: number): DatasetSetting {
		return this.replaceJoin({
			column: replaceArray(this.join()?.column ?? [], newOne, index),
		});
	}

	removeJoinColumn(index: number): DatasetSetting {
		return this.replaceJoin({
			column: removeArray(this.join()?.column ?? [], index),
		});
	}

	replaceTarget(builder: DatasetSettingBuilder): DatasetSetting {
		return this.with({
			name: builder.name
				? typeof builder.name === "string"
					? [builder.name]
					: builder.name
				: undefined,
			pattern: builder.pattern
				? typeof builder.pattern === "string"
					? ({ string: builder.pattern } as Pattern)
					: builder.pattern
				: undefined,
			innerJoin: builder.innerJoin,
			outerJoin: builder.outerJoin,
			fullJoin: builder.fullJoin,
		});
	}

	withSplit(isSplit: boolean): DatasetSetting {
		return this.with({
			tableName: isSplit ? undefined : this.split ? this.split.tableName : "",
			split: isSplit
				? this.tableName
					? { tableName: this.tableName }
					: { tableName: "" }
				: undefined,
		});
	}

	replaceSplit(newVal: Split): DatasetSetting {
		return this.with({ split: { ...this.split, ...newVal } });
	}

	replaceSplitBreakKey(newOne: string, index: number): DatasetSetting {
		return this.replaceSplit({
			breakKey: replaceArray(this.split?.breakKey ?? [], newOne, index),
		});
	}

	removeSplitBreakKey(index: number): DatasetSetting {
		return this.replaceSplit({
			breakKey: removeArray(this.split?.breakKey ?? [], index),
		});
	}

	replaceKeys(newOne: string, index: number): DatasetSetting {
		return this.with({
			keys: replaceArray(this.keys, newOne, index),
		});
	}

	removeKeys(index: number): DatasetSetting {
		return this.with({
			keys: removeArray(this.keys, index),
		});
	}

	replaceInclude(newOne: string, index: number): DatasetSetting {
		return this.with({
			include: replaceArray(this.include, newOne, index),
		});
	}

	removeInclude(index: number): DatasetSetting {
		return this.with({
			include: removeArray(this.include, index),
		});
	}

	replaceExclude(newOne: string, index: number): DatasetSetting {
		return this.with({
			exclude: replaceArray(this.exclude, newOne, index),
		});
	}

	removeExclude(index: number): DatasetSetting {
		return this.with({
			exclude: removeArray(this.exclude, index),
		});
	}

	replaceString(
		index: number,
		value: { [prop: string]: string },
	): DatasetSetting {
		return this.with({
			string: replaceObject(this.string, index, value),
		});
	}

	removeString(index: number): DatasetSetting {
		return this.with({
			string: removeObject(this.string, index),
		});
	}

	replaceNumber(
		index: number,
		value: { [prop: string]: string },
	): DatasetSetting {
		return this.with({
			number: replaceObject(this.number, index, value),
		});
	}

	removeNumber(index: number): DatasetSetting {
		return this.with({
			number: removeObject(this.number, index),
		});
	}

	replaceBoolean(
		index: number,
		value: { [prop: string]: string },
	): DatasetSetting {
		return this.with({
			boolean: replaceObject(this.boolean, index, value),
		});
	}

	removeBoolean(index: number): DatasetSetting {
		return this.with({
			boolean: removeObject(this.boolean, index),
		});
	}

	replaceFunction(
		index: number,
		value: { [prop: string]: string },
	): DatasetSetting {
		return this.with({
			function: replaceObject(this.function, index, value),
		});
	}

	removeFunction(index: number): DatasetSetting {
		return this.with({
			function: removeObject(this.function, index),
		});
	}

	replaceFilter(newOne: string, index: number): DatasetSetting {
		return this.with({
			filter: replaceArray(this.filter, newOne, index),
		});
	}

	removeFilter(index: number): DatasetSetting {
		return this.with({
			filter: removeArray(this.filter, index),
		});
	}

	replaceOrder(newOne: string, index: number): DatasetSetting {
		return this.with({
			order: replaceArray(this.order, newOne, index),
		});
	}

	removeOrder(index: number): DatasetSetting {
		return this.with({
			order: removeArray(this.order, index),
		});
	}

	with(builder: DatasetSettingBuilder): DatasetSetting {
		return new DatasetSetting({
			...this,
			...builder,
		});
	}

	displayName(): string {
		return target(this);
	}

	handler(): string {
		return this.pattern
			? "pattern"
			: this.innerJoin
				? "innerJoin"
				: this.outerJoin
					? "outerJoin"
					: this.fullJoin
						? "fullJoin"
						: this.name
							? "name"
							: "";
	}

	join(): TableJoin | undefined {
		return this.innerJoin || this.outerJoin || this.fullJoin;
	}
}
function target(setting: DatasetSetting): string {
	if (setting.name) {
		if (typeof setting.name === "string") {
			return `name :${setting.name}`;
		}
		const names = setting.name as string[];
		return `name :[${names.join(",")}]`;
	}
	if (setting.pattern) {
		if (typeof setting.pattern === "string") {
			return `pattern :${setting.pattern}`;
		}
		const pattern = setting.pattern as Pattern;
		return `pattern :${pattern.string} exclude :[${pattern.exclude ? pattern.exclude.join(",") : ""}]`;
	}
	if (setting.innerJoin) {
		return `innerJoin :${setting.innerJoin.left} ${setting.innerJoin.right} on ${setting.innerJoin.column ? setting.innerJoin.column.join(",") : setting.innerJoin.on}`;
	}
	if (setting.outerJoin) {
		return `innerJoin :${setting.outerJoin.left} ${setting.outerJoin.right} on ${setting.outerJoin.column ? setting.outerJoin.column.join(",") : setting.outerJoin.on}`;
	}
	if (setting.fullJoin) {
		return `innerJoin :${setting.fullJoin.left} ${setting.fullJoin.right} on ${setting.fullJoin.column ? setting.fullJoin.column.join(",") : setting.fullJoin.on}`;
	}
	return "";
}
function replaceArray(
	array: string[],
	newOne: string,
	index: number,
): string[] {
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
function removeArray(array: string[], remove: number): string[] {
	return [...array].filter((_, index) => index !== remove);
}
function replaceObject(
	src: object,
	index: number,
	value: { [prop: string]: string },
): object {
	const result = {};
	Object.entries(src).forEach(([k, v], i) => {
		if (i === index) {
			Object.assign(result, value);
		} else {
			Object.assign(result, { [k]: v });
		}
	});
	if (Object.keys(src).length === index) {
		Object.assign(result, value);
	}
	return result;
}
function removeObject(src: object, index: number): object {
	return Object.fromEntries(
		Object.entries(src).filter(([_], i) => i !== index),
	);
}
export function newDatasetSetting(): DatasetSetting {
	return new DatasetSetting({} as DatasetSettingBuilder);
}
