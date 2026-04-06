import { useState } from "react";
import { ExpandButton } from "../../../components/element/ButtonIcon";
import type {
	CommandOption,
	SrcElements,
	SrcType,
} from "../../../model/CommandOption";
import { isSqlRelatedType } from "../../../model/QueryDatasource";
import Check from "./element/Check";
import FileText from "./element/FileText";
import PlainText from "./element/PlainText";
import SqlSrcText from "./element/SqlSrcText";

export default function SrcFormSection({
	srcElements,
	srcType,
	handleValueChange,
	handleToggleChecked,
}: {
	srcElements: SrcElements;
	srcType?: SrcType;
	handleValueChange: (param: CommandOption) => (newValue: string) => void;
	handleToggleChecked: (param: CommandOption) => (checked: boolean) => void;
}) {
	const [showOptional, setShowOptional] = useState(false);
	const toggleOptional = () => setShowOptional(!showOptional);
	const prefix = srcElements.prefix;

	return (
		<>
			{(srcType && isSqlRelatedType(srcType) && (
				<SqlSrcText
					prefix={prefix}
					element={srcElements.src}
					handleValueChange={handleValueChange(srcElements.src)}
					hidden={false}
					srcType={srcType}
				/>
			)) || (
				<FileText
					prefix={prefix}
					element={srcElements.src}
					handleValueChange={handleValueChange(srcElements.src)}
					hidden={false}
				/>
			)}
			{srcElements.encoding && (
				<PlainText
					prefix={prefix}
					element={srcElements.encoding}
					handleValueChange={handleValueChange(srcElements.encoding)}
					hidden={false}
				/>
			)}
			<div className="pt-2.5">
				<ExpandButton
					toggleOptional={toggleOptional}
					showOptional={showOptional}
					caption="traversal option"
				/>
			</div>
			<Check
				prefix={prefix}
				element={srcElements.recursive}
				handleOnChange={handleToggleChecked(srcElements.recursive)}
				hidden={!showOptional}
			/>
			<PlainText
				prefix={prefix}
				element={srcElements.regInclude}
				handleValueChange={handleValueChange(srcElements.regInclude)}
				hidden={!showOptional}
			/>
			<PlainText
				prefix={prefix}
				element={srcElements.regExclude}
				handleValueChange={handleValueChange(srcElements.regExclude)}
				hidden={!showOptional}
			/>
			{srcElements.extension && (
				<PlainText
					prefix={prefix}
					element={srcElements.extension}
					handleValueChange={handleValueChange(srcElements.extension)}
					hidden={!showOptional}
				/>
			)}
		</>
	);
}
