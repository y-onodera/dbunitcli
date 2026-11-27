// headerNameのブランク列（空または空白だけの要素）を1始まりの位置で返す。欄が丸ごと空なら未指定なので対象外
export function blankColumnPositions(headerName: string): number[] {
	if (headerName.trim() === "") {
		return [];
	}
	return headerName
		.split(",")
		.flatMap((name, index) => (name.trim() === "" ? [index + 1] : []));
}

// ブランク列だけを数えて defaultName+連番 で埋める。桁はブランクの数（重複回避で進んだ番号）に揃えてゼロ埋めする
export function fillBlankColumns(
	headerName: string,
	defaultName: string,
): string {
	const names = headerName.split(",");
	const blankCount = blankColumnPositions(headerName).length;
	if (blankCount === 0) {
		return headerName;
	}
	const used = new Set(
		names.map((name) => name.trim()).filter((name) => name !== ""),
	);
	for (let width = String(blankCount).length; ; width++) {
		const filled = assignNames(names, defaultName, width, used);
		if (filled) {
			return filled.join(",");
		}
	}
}

// 既存の列名と重なる番号は飛ばす。番号が桁に収まらなければnull
function assignNames(
	names: string[],
	defaultName: string,
	width: number,
	used: ReadonlySet<string>,
): string[] | null {
	const limit = 10 ** width;
	let next = 1;
	const nextName = () => {
		for (; next < limit; next++) {
			const candidate = `${defaultName}${String(next).padStart(width, "0")}`;
			if (!used.has(candidate)) {
				next++;
				return candidate;
			}
		}
		return null;
	};
	const result: string[] = [];
	for (const name of names) {
		if (name.trim() !== "") {
			result.push(name);
			continue;
		}
		const assigned = nextName();
		if (assigned === null) {
			return null;
		}
		result.push(assigned);
	}
	return result;
}
