import { useState } from "react";
import { SectionFieldset } from "../../components/dialog";
import { SectionLegend } from "../../components/dialog/SectionFieldset";
import { useResourcesSettings } from "../../context/WorkspaceResourcesProvider";
import type { ParameterizeOptions } from "../../model/SelectParameter";
import { DatasetLoadForm } from "./section/DatasetLoadForm";
import ParameterizeTemplateDialog from "./section/dialog/ParameterizeTemplateDialog";
import {
	EditResourceButton,
	RemoveResourceButton,
} from "./section/dialog/ResourceEditButton";
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
	// templateの編集で読み書きする文字コード（-template.encodingの入力中の値）
	const [templateEncoding, setTemplateEncoding] = useState(
		templateOption?.encoding?.value ?? "",
	);
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
								<EditResourceButton
									key="edit"
									dialog={ParameterizeTemplateDialog}
									dialogProps={{ encoding: templateEncoding }}
									path={path}
									setPath={setPath}
								/>,
							]}
							removeButton={(closeMenu) => (
								<RemoveResourceButton
									kind="parameterizeTemplate"
									path={path}
									setPath={setPath}
									onRemoved={closeMenu}
								/>
							)}
						/>
					)}
				</Text>
				{templateOption && (
					<TemplateFormSection
						templateOption={templateOption}
						showEncoding={true}
						encoding={templateEncoding}
						handleValueChange={(param) => (value: string) => {
							if (param === templateOption.encoding) {
								setTemplateEncoding(value);
							}
						}}
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
