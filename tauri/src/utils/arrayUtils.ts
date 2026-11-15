// 入力欄の配列編集。空の値で置き換えるとその要素を除き、末尾より後ろのindexなら追加する
export function replaceArray<T>(array: T[], newOne: T, index: number): T[] {
	if (index >= array.length) {
		return newOne ? [...array, newOne] : [...array];
	}
	if (!newOne) {
		return removeArray(array, index);
	}
	return array.map((it, i) => (i === index ? newOne : it));
}
export function removeArray<T>(array: T[], remove: number): T[] {
	return array.filter((_, index) => index !== remove);
}
// カンマ区切りを分ける。前後の空白を除き、空の要素は持たない
export function splitComma(text: string): string[] {
	return text
		.split(",")
		.map((it) => it.trim())
		.filter((it) => it !== "");
}
