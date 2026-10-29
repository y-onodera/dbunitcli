import { SectionFieldset } from "../../components/dialog";
import { SectionLegend } from "../../components/dialog/SectionFieldset";
import { useResourcesSettings } from "../../context/WorkspaceResourcesProvider";
import type { ParameterizeOptions } from "../../model/SelectParameter";
import { DatasetLoadForm } from "./section/DatasetLoadForm";
import { RemoveResourceButton } from "./section/dialog/ResourceEditButton";
import { TemplateCommandButton } from "./section/dialog/TemplateCommandDialog";
import Check from "./section/element/Check";
import PlainText from "./section/element/PlainText";
import Select from "./section/element/Select";
import Text, { TextDropDownMenu } from "./section/element/Text";
import TemplateFormSection from "./section/TemplateFormSection";

export function ParameterizeForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	parameterize: ParameterizeOptions;
}) {
	const parameterize = prop.parameterize;
	const paramData = parameterize.paramData;
	const templateOption = parameterize.templateOption;
	const settings = useResourcesSettings();

	return (
		<>
			<SectionFieldset>
				<SectionLegend
					title="execute"
					command="commands/parameterize"
					label="Parameterize"
				/>
				<Select
					handleTypeSelect={prop.handleTypeSelect}
					prefix=""
					element={parameterize.unit}
				/>
				<Check prefix="" element={parameterize.parameterize} />
				<Check prefix="" element={parameterize.ignoreFail} />
				{parameterize.parallel && (
					<PlainText prefix="" element={parameterize.parallel} />
				)}
				<PlainText prefix="" element={parameterize.cmd} />
				<PlainText prefix="" element={parameterize.cmdParam} />
				<Text
					prefix=""
					element={parameterize.template}
					showDefaulePath={true}
					resourceFiles={settings.parameterizeTemplateFiles}
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
									setPath={setPath}
								/>,
							]}
							removeButton={() => (
								<RemoveResourceButton
									kind="parameterizeTemplate"
									path={path}
									setPath={setPath}
								/>
							)}
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
			</SectionFieldset>
			<DatasetLoadForm
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				srcData={paramData}
				helpCommand="sections/dataset-load-form"
				helpLabel="param"
			/>
		</>
	);
}
