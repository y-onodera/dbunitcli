import { type ComponentType, type ReactElement, useState } from "react";
import {
	DeleteButton,
	EditButton,
} from "../../../../components/element/ButtonIcon";

export type ResourceEditButtonProp = {
	path: string;
	setPath: (value: string) => void;
};

type RemoveResourceProp = ResourceEditButtonProp & {
	deleteResource: (path: string) => Promise<string>;
};

type ResourcesEditButtonProps = {
	renderDialog: (
		dialogOpen: boolean,
		closeDialog: () => void,
	) => ReactElement | null;
	renderTrigger?: (openDialog: () => void) => ReactElement;
};

export function RemoveResource({
	deleteResource,
	path,
	setPath,
}: RemoveResourceProp) {
	const handleRemove = async () => {
		const confirmed = await window.confirm(
			`${path}を削除してもよろしいですか？`,
		);
		if (!confirmed) {
			return;
		}

		try {
			const result = await deleteResource(path);
			if (result === "success") {
				setPath("");
			} else {
				alert("削除に失敗しました");
			}
		} catch (ex) {
			alert(ex);
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

// ファイル名で開く編集ダイアログ（保存したらパスを反映して閉じる）を持つ編集ボタンを生成する
export function createEditButton(
	DialogComponent: ComponentType<{
		fileName: string;
		handleDialogClose: () => void;
		handleSave: (path: string) => void;
	}>,
) {
	return function EditResourceButton({
		path,
		setPath,
	}: ResourceEditButtonProp) {
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
	};
}
