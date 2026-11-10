import { createContext, use } from "react";
import type { ResourceKind } from "../hooks/useResourceFiles";
import type { ResourceLocation } from "../model/ResourceLocation";

type OpenedResource = {
	kind: ResourceKind;
	fileName: string;
	location: ResourceLocation | null;
};

// 編集ダイアログが読み込んだリソースファイルと、その場所。
// 範囲内には別のファイルのダイアログ（生成ダイアログ等）も開くため、種類と名前が一致するときだけ場所を返す
const OpenedResourceContext = createContext<OpenedResource | null>(null);

export function OpenedResourceProvider({
	children,
	...opened
}: OpenedResource & { children: React.ReactNode }) {
	return (
		<OpenedResourceContext.Provider value={opened}>
			{children}
		</OpenedResourceContext.Provider>
	);
}

// 種類・名前のファイルを開いた場所。そのファイルを読み込んでいない・見つからなかったならnull
export function useOpenedResourceLocation(
	kind: ResourceKind,
	fileName: string,
): ResourceLocation | null {
	const opened = use(OpenedResourceContext);
	if (opened === null || opened.kind !== kind || opened.fileName !== fileName) {
		return null;
	}
	return opened.location;
}
