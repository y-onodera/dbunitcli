import { useState } from "react";
import { EditButton } from "../../../../components/element/ButtonIcon";
import { useJdbcConnectionState } from "../../../../context/JdbcConnectionProvider";
import { useLoadDataSource } from "../../../../hooks/useQueryDatasource";
import JdbcTableSelectorDialog from "./JdbcTableSelectorDialog";
import type { ResourceEditButtonProp } from "./ResourceEditButton";

export default function JdbcTableSelectorButton({
	path,
	setPath,
}: ResourceEditButtonProp) {
	const [showDialog, setShowDialog] = useState(false);
	const [content, setContent] = useState("");
	const loadDataSource = useLoadDataSource();
	const { jdbcValues } = useJdbcConnectionState();

	const handleOpen = async () => {
		try {
			if (path) {
				const result = await loadDataSource("table", path);
				setContent(result);
			}
			setShowDialog(true);
		} catch (ex) {
			alert(ex);
		}
	};

	const handleClose = () => {
		setShowDialog(false);
		setContent("");
	};

	const handleSave = (newPath: string) => {
		setPath(newPath);
		setShowDialog(false);
		setContent("");
	};

	return (
		<>
			<EditButton handleClick={handleOpen} />
			{showDialog && (
				<JdbcTableSelectorDialog
					jdbcValues={jdbcValues}
					currentContent={content}
					fileName={path}
					handleDialogClose={handleClose}
					handleSave={handleSave}
				/>
			)}
		</>
	);
}
