import { useState } from "react";
import { ExpandButton } from "../../../components/element/ButtonIcon";
import type { CommandOption, SrcElements } from "../../../model/CommandOption";
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
	const toggleOptional = () => setShowOptional(!showOptional);
	const prefix = options.prefix;
	return (
		<fieldset className="border border-gray-200 p-3">
			<legend>data load</legend>
			<ExpandButton
				toggleOptional={toggleOptional}
				showOptional={showOptional}
				caption="data load option"
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
					handleOnChange={handleToggleChecked(options.loadData)}
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
			/>
			<PlainText
				prefix={prefix}
				element={options.regTableExclude}
				handleValueChange={handleValueChange(options.regTableExclude)}
				hidden={!showOptional}
			/>
		</fieldset>
	);
}
