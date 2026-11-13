import { useState } from "react";
import {
	SectionFieldset,
	SectionOptionHeader,
} from "../../../components/dialog";
import type { CommandOption, SrcElements } from "../../../model/CommandOption";
import { regexFilterSlot } from "./dialog/RegexFilterDialog";
import Check from "./element/Check";
import PlainText from "./element/PlainText";

export default function DataLoadFormSection({
	options,
	handleValueChange,
	handleToggleChecked,
}: {
	options: SrcElements;
	handleValueChange: (param: CommandOption) => (newValue: string) => void;
	handleToggleChecked: (param: CommandOption) => (checked: boolean) => void;
}) {
	const [showOptional, setShowOptional] = useState(false);
	// loadMetaDataは行を読まない（loadData=false）ときだけ選べる
	const [loadData, setLoadData] = useState(options.loadData?.value !== "false");
	const toggleOptional = () => setShowOptional(!showOptional);
	const prefix = options.prefix;
	return (
		<SectionFieldset>
			<legend>data load</legend>
			<SectionOptionHeader
				showOptional={showOptional}
				toggleOptional={toggleOptional}
				caption="data load option"
				command="sections/data-load"
				label="Data Load"
			/>
			{options.headerName && (
				<PlainText
					prefix={prefix}
					element={options.headerName}
					handleValueChange={handleValueChange(options.headerName)}
					hidden={!showOptional}
				/>
			)}
			{options.startRow && (
				<PlainText
					prefix={prefix}
					element={options.startRow}
					handleValueChange={handleValueChange(options.startRow)}
					hidden={!showOptional}
				/>
			)}
			{options.addFileInfo && (
				<Check
					prefix={prefix}
					element={options.addFileInfo}
					handleOnChange={handleToggleChecked(options.addFileInfo)}
					hidden={!showOptional}
				/>
			)}
			{options.loadData && (
				<Check
					prefix={prefix}
					element={options.loadData}
					handleOnChange={(checked) => {
						setLoadData(checked);
						handleToggleChecked(options.loadData)(checked);
					}}
					hidden={!showOptional}
				/>
			)}
			{options.loadMetaData && !loadData && (
				<Check
					prefix={prefix}
					element={options.loadMetaData}
					handleOnChange={handleToggleChecked(options.loadMetaData)}
					hidden={!showOptional}
				/>
			)}
			{options.includeMetaData && (
				<Check
					prefix={prefix}
					element={options.includeMetaData}
					handleOnChange={handleToggleChecked(options.includeMetaData)}
					hidden={!showOptional}
				/>
			)}
			<PlainText
				prefix={prefix}
				element={options.regTableInclude}
				handleValueChange={handleValueChange(options.regTableInclude)}
				hidden={!showOptional}
			>
				{regexFilterSlot("table", options.regTableInclude.name)}
			</PlainText>
			<PlainText
				prefix={prefix}
				element={options.regTableExclude}
				handleValueChange={handleValueChange(options.regTableExclude)}
				hidden={!showOptional}
			>
				{regexFilterSlot("table", options.regTableExclude.name)}
			</PlainText>
		</SectionFieldset>
	);
}
