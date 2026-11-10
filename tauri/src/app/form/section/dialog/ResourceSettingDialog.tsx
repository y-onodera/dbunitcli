import {
	type FileSettingDialogProps,
	SettingDialog,
} from "../../../../components/dialog";
import { useOpenedResourceLocation } from "../../../../context/OpenedResourceProvider";
import type { ResourceKind } from "../../../../hooks/useResourceFiles";

// リソースファイルを編集するSettingDialog。開いたファイルの場所（ResourceLoaderが読み込んだもの）から、上書きか規定フォルダへの保存かを選ばせる
export function ResourceSettingDialog({
	resourceKind,
	...props
}: Omit<FileSettingDialogProps, "location"> & {
	resourceKind: ResourceKind;
}) {
	const location = useOpenedResourceLocation(resourceKind, props.fileName);
	return <SettingDialog {...props} location={location} />;
}
