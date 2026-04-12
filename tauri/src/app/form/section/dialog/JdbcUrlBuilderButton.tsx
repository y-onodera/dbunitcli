import { type Dispatch, type SetStateAction, useState } from "react";
import { BlueEditButton } from "../../../../components/element/ButtonIcon";
import JdbcUrlBuilderDialog from "./JdbcUrlBuilderDialog";

export function JdbcUrlBuilderButton({
	value,
	setValue,
}: {
	value: string;
	setValue: Dispatch<SetStateAction<string>>;
}) {
	const [showDialog, setShowDialog] = useState(false);
	return (
		<>
			<BlueEditButton handleClick={() => setShowDialog(true)} />
			{showDialog && (
				<JdbcUrlBuilderDialog
					currentUrl={value}
					handleDialogClose={() => setShowDialog(false)}
					handleSave={(url) => {
						setValue(url);
						setShowDialog(false);
					}}
				/>
			)}
		</>
	);
}
