import type { ReactNode } from "react";
import type { QueryDatasourceType } from "../../../../model/QueryDatasource";
import SqlEditorButton, {
	RemoveSqlEditorButton,
} from "../../../settings/SqlEditorButton";
import type { FileProp, TextProp } from "./FormElementProp";
import Text, { TextDropDownMenu } from "./Text";

type Props = Omit<FileProp, "onSelect" | "hidden">;
export default function SqlSrcText({
	prefix,
	element,
	hidden,
	srcType,
	handleValueChange,
}: TextProp) {
	return (
		<Text
			prefix={prefix}
			element={element}
			hidden={hidden}
			showDefaulePath={true}
			handleValueChange={handleValueChange}
		>
			{({ path, setPath }) => (
				<SqlSrcDropDownMenu
					path={path}
					setPath={setPath}
					prefix={prefix}
					element={element}
					srcType={srcType}
				/>
			)}
		</Text>
	);
}
function SqlSrcDropDownMenu({
	path,
	setPath,
	prefix,
	element,
	srcType,
}: Props) {
	const isSqlOrTable = srcType === "sql" || srcType === "table";

	const editButtons: ReactNode[] = [];
	editButtons.push(
		<SqlEditorButton
			type={srcType as QueryDatasourceType}
			path={path}
			setPath={setPath}
		/>,
	);

	return (
		<TextDropDownMenu
			path={path}
			setPath={setPath}
			prefix={prefix}
			element={element}
			srcType={srcType}
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
