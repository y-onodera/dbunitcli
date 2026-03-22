import DropDownMenu from "../../../components/element/DropDownMenu";
import { useJdbcConnectionState } from "../../../context/JdbcConnectionProvider";
import {
	isSqlRelatedType,
	type QueryDatasourceType,
} from "../../../model/QueryDatasource";
import JdbcTableSelectorButton from "../../settings/JdbcTableSelectorButton";
import SqlEditorButton, {
	RemoveSqlEditorButton,
} from "../../settings/SqlEditorButton";
import { FileChooser, OpenInOS } from "./Chooser";
import type { FileProp } from "./FormElementProp";

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
	const isFileType = element.attribute.type.includes("FILE");
	const isDirType = element.attribute.type.includes("DIR");
	const isFileOrDir = isFileType || isDirType;
	const isSqlSrc = isSqlRelatedType(srcType ?? "");
	const isSqlOrTable = srcType === "sql" || srcType === "table";
	return (
		<DropDownMenu>
			{(closeMenu) => (
				<>
					{isSqlSrc && (
						<li>
							<SqlEditorButton
								type={srcType as QueryDatasourceType}
								path={path}
								setPath={setPath}
							/>
						</li>
					)}
					{srcType === "table" && connectionOk && (
						<li>
							<JdbcTableSelectorButton path={path} setPath={setPath} />
						</li>
					)}
					{isFileOrDir && path && (
						<li>
							<OpenInOS
								prefix={prefix}
								element={element}
								srcType={srcType}
								path={path}
								setPath={setPath}
							/>
						</li>
					)}
					{isSqlOrTable && isValueInDatalist && (
						<li>
							<RemoveSqlEditorButton
								path={path}
								setPath={setPath}
								type={srcType as QueryDatasourceType}
							/>
						</li>
					)}
					{isFileType && (
						<li>
							<FileChooser
								prefix={prefix}
								element={element}
								srcType={srcType}
								path={path}
								setPath={setPath}
								onSelect={closeMenu}
							/>
						</li>
					)}
				</>
			)}
		</DropDownMenu>
	);
}
