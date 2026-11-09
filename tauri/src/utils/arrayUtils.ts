// 入力欄の配列編集。空の値で置き換えるとその要素を除き、末尾より後ろのindexなら追加する
export function replaceArray<T>(array: T[], newOne: T, index: number): T[] {
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
export function removeArray<T>(array: T[], remove: number): T[] {
	return [...array].filter((_, index) => index !== remove);
}
