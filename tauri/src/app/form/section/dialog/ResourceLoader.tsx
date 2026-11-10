import type { ReactNode } from "react";
import { StatusMessage } from "../../../../components/dialog";
import {
	type ResourceKind,
	type ResourceParser,
	useResourceLoad,
} from "../../../../hooks/useResourceFiles";
import type { ResourceLocation } from "../../../../model/ResourceLocation";

// リソースファイルを読み込み終えてから、読み込んだ値と開いたファイルの場所で編集ダイアログを描画する
export function ResourceLoader<T>({
	kind,
	fileName,
	parser,
	children,
}: {
	kind: ResourceKind;
	fileName: string;
	parser: ResourceParser<T>;
	children: (data: T, location: ResourceLocation | null) => ReactNode;
}) {
	const { data, location, loading } = useResourceLoad(kind, fileName, parser);
	if (loading) {
		return <StatusMessage kind="loading">Loading...</StatusMessage>;
	}
	return children(data, location);
}
