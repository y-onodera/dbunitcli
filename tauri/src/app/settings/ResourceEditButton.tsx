import { type ReactElement, useState } from "react";
import { DeleteButton, EditButton } from "../../components/element/ButtonIcon";

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
			<EditButton handleClick={openDialog} />
		</>
	);
}
