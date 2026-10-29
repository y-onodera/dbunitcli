import { type ComponentType, type ReactElement, useState } from "react";
import {
	DeleteButton,
	EditButton,
} from "../../../../components/element/ButtonIcon";
import {
	type ResourceKind,
	useDeleteResource,
} from "../../../../hooks/useResourceFiles";

export type ResourceEditButtonProp = {
	path: string;
	setPath: (value: string) => void;
};

// onRemovedは削除に成功したときだけ呼ぶ（ドロップダウンを閉じる等）
export type RemoveResourceButtonProp = ResourceEditButtonProp & {
	onRemoved?: () => void;
};

type RemoveResourceProp = RemoveResourceButtonProp & {
	deleteResource: (path: string) => Promise<string>;
};

type ResourcesEditButtonProps = {
	renderDialog: (
		dialogOpen: boolean,
		closeDialog: () => void,
	) => ReactElement | null;
	renderTrigger?: (openDialog: () => void) => ReactElement;
};

// 確認のうえ削除し、成功したかを返す。try/catch内の分岐はReact Compilerが未対応のため、コンポーネント外で行う
async function confirmAndDelete(
	path: string,
	deleteResource: (path: string) => Promise<string>,
): Promise<boolean> {
	const confirmed = await window.confirm(`${path}を削除してもよろしいですか？`);
	if (!confirmed) {
		return false;
	}
	try {
		if ((await deleteResource(path)) === "success") {
			return true;
		}
		alert("削除に失敗しました");
	} catch (ex) {
		alert(ex);
	}
	return false;
}

export function RemoveResource({
	deleteResource,
	path,
	setPath,
	onRemoved,
}: RemoveResourceProp) {
	const handleRemove = async () => {
		if (await confirmAndDelete(path, deleteResource)) {
			setPath("");
			onRemoved?.();
		}
	};

	return <DeleteButton handleClick={handleRemove} />;
}

export default function ResourceEditButton({
	renderDialog,
	renderTrigger,
}: ResourcesEditButtonProps) {
	const [dialogOpen, setDialogOpen] = useState(false);

	const openDialog = () => {
		setDialogOpen(true);
	};

	const closeDialog = () => {
		setDialogOpen(false);
	};

	return (
		<>
			{renderDialog(dialogOpen, closeDialog)}
			{renderTrigger ? (
				renderTrigger(openDialog)
			) : (
				<EditButton handleClick={openDialog} />
			)}
		</>
	);
}

// ファイル名で開く編集ダイアログ（保存したらパスを反映して閉じる）を持つ編集ボタン
export function EditResourceButton({
	dialog: DialogComponent,
	path,
	setPath,
}: ResourceEditButtonProp & {
	dialog: ComponentType<{
		fileName: string;
		handleDialogClose: () => void;
		handleSave: (path: string) => void;
	}>;
}) {
	const renderDialog = (open: boolean, closeDialog: () => void) => {
		if (!open) {
			return null;
		}
		return (
			<DialogComponent
				fileName={path}
				handleDialogClose={closeDialog}
				handleSave={(newPath: string) => {
					setPath(newPath);
					closeDialog();
				}}
			/>
		);
	};
	return <ResourceEditButton renderDialog={renderDialog} />;
}

// 種類ごとの削除APIでRemoveResourceを使う削除ボタン
export function RemoveResourceButton({
	kind,
	path,
	setPath,
	onRemoved,
}: RemoveResourceButtonProp & { kind: ResourceKind }) {
	const deleteResource = useDeleteResource(kind);
	return (
		<RemoveResource
			path={path}
			setPath={setPath}
			onRemoved={onRemoved}
			deleteResource={deleteResource}
		/>
	);
}
