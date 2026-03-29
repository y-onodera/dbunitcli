import { useState } from "react";
import { ExpandButton } from "../../../components/element/ButtonIcon";
import type { CommandParam, SrcElements } from "../../../model/CommandParam";
import { isSqlRelatedType } from "../../../model/QueryDatasource";
import Check from "./element/Check";
import FileText from "./element/FileText";
import type { SelectProp } from "./element/FormElementProp";
import ResourceText from "./element/ResourceText";
import Select from "./element/Select";
import SqlSrcText from "./element/SqlSrcText";

export default function SrcFormSection({
	srcElements,
	handleTypeSelect,
	handleValueChange,
	handleToggleChecked,
}: {
	srcElements: SrcElements;
	handleTypeSelect: SelectProp["handleTypeSelect"];
	handleValueChange: (param: CommandParam) => (newValue: string) => void;
	handleToggleChecked: (param: CommandParam) => (checked: boolean) => void;
}) {
	const [showOptional, setShowOptional] = useState(false);
	const toggleOptional = () => setShowOptional(!showOptional);
	const prefix = srcElements.prefix;
	const srcType = srcElements.srcType.value;

	return (
		<>
			<Select
				handleTypeSelect={handleTypeSelect}
				prefix={prefix}
				element={srcElements.srcType}
				hidden={false}
			/>
			{(isSqlRelatedType(srcElements.srcType.value) && (
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
				<ResourceText
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
			<ResourceText
				prefix={prefix}
				element={srcElements.regInclude}
				handleValueChange={handleValueChange(srcElements.regInclude)}
				hidden={!showOptional}
			/>
			<ResourceText
				prefix={prefix}
				element={srcElements.regExclude}
				handleValueChange={handleValueChange(srcElements.regExclude)}
				hidden={!showOptional}
			/>
			<ResourceText
				prefix={prefix}
				element={srcElements.extension}
				handleValueChange={handleValueChange(srcElements.extension)}
				hidden={!showOptional}
			/>
		</>
	);
}
