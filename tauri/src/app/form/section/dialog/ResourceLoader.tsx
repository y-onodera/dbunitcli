import type { ReactNode } from "react";
import { StatusMessage } from "../../../../components/dialog";
import { OpenedResourceProvider } from "../../../../context/OpenedResourceProvider";
import {
	type ResourceKind,
	type ResourceParser,
	useResourceLoad,
} from "../../../../hooks/useResourceFiles";

// リソースファイルを読み込み終えてから、読み込んだ値で編集ダイアログを描画する。
// 開いたファイルの場所は、中のResourceSettingDialogがOpenedResourceProviderから受け取る
export function ResourceLoader<T>({
	kind,
	fileName,
	parser,
	children,
}: {
	kind: ResourceKind;
	fileName: string;
	parser: ResourceParser<T>;
	children: (data: T) => ReactNode;
}) {
	const { data, location, loading } = useResourceLoad(kind, fileName, parser);
	if (loading) {
		return <StatusMessage kind="loading">Loading...</StatusMessage>;
	}
	return (
		<OpenedResourceProvider kind={kind} fileName={fileName} location={location}>
			{children(data)}
		</OpenedResourceProvider>
	);
}
