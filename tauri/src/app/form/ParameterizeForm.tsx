import { useState } from "react";
import { useParameterList } from "../../context/WorkspaceResourcesProvider";
import type { ParameterizeOptions } from "../../model/SelectParameter";
import { DatasetLoadForm } from "./section/DatasetLoadForm";
import Check from "./section/element/Check";
import PlainText from "./section/element/PlainText";
import Select from "./section/element/Select";
import Text, { TextDropDownMenu } from "./section/element/Text";
import TemplateFormSection from "./section/TemplateFormSection";
import {
	TemplateCommandButton,
	resolveCommand,
} from "./section/dialog/TemplateCommandDialog";

export function ParameterizeForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	parameterize: ParameterizeOptions;
}) {
	const parameterize = prop.parameterize;
	const paramData = parameterize.paramData;
	const templateOption = parameterize.templateOption;
	const [cmdValue, setCmdValue] = useState(parameterize.cmd.value);
	const parameterList = useParameterList();

	const resolved = resolveCommand(cmdValue);
	const templateDataList = resolved ? parameterList[resolved] : [];

	return (
		<>
			<fieldset className="border border-gray-200 p-3">
				<legend>execute</legend>
				<Select
					handleTypeSelect={prop.handleTypeSelect}
					prefix=""
					element={parameterize.unit}
				/>
				<Check prefix="" element={parameterize.parameterize} />
				<Check prefix="" element={parameterize.ignoreFail} />
				<PlainText
					prefix=""
					element={parameterize.cmd}
					handleValueChange={setCmdValue}
				/>
				<PlainText prefix="" element={parameterize.cmdParam} />
				<Text
					prefix=""
					element={parameterize.template}
					showDefaulePath={true}
					resourceFiles={templateDataList}
				>
					{({ path, setPath, isValueInDatalist }) => (
						<TextDropDownMenu
							path={path}
							setPath={setPath}
							prefix=""
							element={parameterize.template}
							isValueInDatalist={isValueInDatalist}
							editButtons={[
								<TemplateCommandButton
									key="open-cmd"
									name={path}
								/>,
							]}
						/>
					)}
				</Text>
				{templateOption && (
					<TemplateFormSection
						templateOption={templateOption}
						showEncoding={true}
						handleValueChange={() => (_: string) => {}}
					/>
				)}
			</fieldset>
			<DatasetLoadForm
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				srcData={paramData}
			/>
		</>
	);
}
