import type { ReactNode } from "react";
import { useJdbcConnectionState } from "../../../context/JdbcConnectionProvider";
import {
	isSqlRelatedType,
	type QueryDatasourceType,
} from "../../../model/QueryDatasource";
import JdbcTableSelectorButton from "../../settings/JdbcTableSelectorButton";
import SqlEditorButton, {
	RemoveSqlEditorButton,
} from "../../settings/SqlEditorButton";
import type { FileProp } from "./FormElementProp";
import ResourceDropDownMenu from "./ResourceDropDownMenu";

type Props = Omit<FileProp, "onSelect" | "hidden"> & {
	isValueInDatalist: boolean;
};

export default function SqlSrcDropDownMenu({
	path,
	setPath,
	prefix,
	element,
	srcType,
	isValueInDatalist,
}: Props) {
	const { connectionOk } = useJdbcConnectionState();
	const isSqlOrTable = srcType === "sql" || srcType === "table";

	const editButtons: ReactNode[] = [];
	if (isSqlRelatedType(srcType ?? "")) {
		editButtons.push(
			<SqlEditorButton
				type={srcType as QueryDatasourceType}
				path={path}
				setPath={setPath}
			/>,
		);
	}
	if (srcType === "table" && connectionOk) {
		editButtons.push(
			<JdbcTableSelectorButton path={path} setPath={setPath} />,
		);
	}

	return (
		<ResourceDropDownMenu
			path={path}
			setPath={setPath}
			prefix={prefix}
			element={element}
			srcType={srcType}
			isValueInDatalist={isValueInDatalist}
			editButtons={editButtons}
			removeButton={
				isSqlOrTable
					? () => (
							<RemoveSqlEditorButton
								path={path}
								setPath={setPath}
								type={srcType as QueryDatasourceType}
							/>
						)
					: undefined
			}
		/>
	);
}
