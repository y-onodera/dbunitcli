import { createContext, useContext } from "react";
import type { ResourceKind } from "../hooks/useResourceFiles";

export type ResourceEncodings = Partial<Record<ResourceKind, string>>;

// 範囲内の編集ダイアログ・プレビューがリソースを読み書きする文字コード。
// 値はコマンド実行時にそのファイルを読む文字コードオプションの入力中の値。入れ子にすると内側の値で上書きする
const ResourceEncodingContext = createContext<ResourceEncodings>({});

export function ResourceEncodingProvider({
	children,
	encodings,
}: {
	children: React.ReactNode;
	encodings: ResourceEncodings;
}) {
	const parent = useContext(ResourceEncodingContext);
	return (
		<ResourceEncodingContext.Provider value={{ ...parent, ...encodings }}>
			{children}
		</ResourceEncodingContext.Provider>
	);
}

// 種類のリソースを読み書きする文字コード。指定が無ければ空（sidecarが種類ごとの既定を使う）
export function useResourceEncoding(kind: ResourceKind): string {
	return useContext(ResourceEncodingContext)[kind] ?? "";
}
