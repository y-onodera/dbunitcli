import {
	type FileSettingDialogProps,
	SettingDialog,
} from "../../../../components/dialog";
import {
	type ResourceKind,
	useResourceLocation,
} from "../../../../hooks/useResourceFiles";

// リソースファイルを編集するSettingDialog。開いたファイルの場所を調べ、上書きか規定フォルダへの保存かを選ばせる
export function ResourceSettingDialog({
	resourceKind,
	...props
}: Omit<FileSettingDialogProps, "location" | "locationLoading"> & {
	resourceKind: ResourceKind;
}) {
	const { location, loading } = useResourceLocation(
		resourceKind,
		props.fileName,
	);
	return (
		<SettingDialog {...props} location={location} locationLoading={loading} />
	);
}
