import { splitComma } from "../utils/arrayUtils";

// headerNameのブランク列（空または空白だけの要素）を1始まりの位置で返す。欄が丸ごと空なら未指定なので対象外
export function blankColumnPositions(headerName: string): number[] {
	if (headerName.trim() === "") {
		return [];
	}
	return headerName
		.split(",")
		.flatMap((name, index) => (name.trim() === "" ? [index + 1] : []));
}

// ブランク列だけを数えて defaultName+連番 で埋める
export function fillBlankColumns(
	headerName: string,
	defaultName: string,
): string {
	const blankCount = blankColumnPositions(headerName).length;
	if (blankCount === 0) {
		return headerName;
	}
	const names = headerName.split(",");
	const used = new Set(splitComma(headerName));
	const picked = pickNames(defaultName, blankCount, used).values();
	return names
		.map((name) => (name.trim() === "" ? picked.next().value : name))
		.join(",");
}

// 既存の列名と重なる番号は飛ばす。桁はブランクの数に揃えてゼロ埋めし、飛ばした分で収まらなければ桁を増やす
function pickNames(
	prefix: string,
	count: number,
	used: ReadonlySet<string>,
): string[] {
	for (let width = String(count).length; ; width++) {
		const picked: string[] = [];
		for (let n = 1; n < 10 ** width && picked.length < count; n++) {
			const candidate = `${prefix}${String(n).padStart(width, "0")}`;
			if (!used.has(candidate)) {
				picked.push(candidate);
			}
		}
		if (picked.length === count) {
			return picked;
		}
	}
}
