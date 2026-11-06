import { useState } from "react";
import { SectionFieldset } from "../../components/dialog";
import { SectionLegend } from "../../components/dialog/SectionFieldset";
import { JdbcConnectionProvider } from "../../context/JdbcConnectionProvider";
import type { CommandOption } from "../../model/CommandOption";
import type { RunOptions } from "../../model/SelectParameter";
import FileText from "./section/element/FileText";
import PlainText from "./section/element/PlainText";
import Select from "./section/element/Select";
import SrcFormSection from "./section/SrcFormSection";
import TemplateFormSection from "./section/TemplateFormSection";
import TraversalFormSection from "./section/TraversalFormSection";

export function RunForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	run: RunOptions;
}) {
	const run = prop.run;
	// SQLスクリプト・templateGroupの編集で読み書きする文字コード（-template.encodingの入力中の値）
	const [templateEncoding, setTemplateEncoding] = useState(
		run.templateOption?.encoding?.value ?? "",
	);
	return (
		<SectionFieldset>
			<SectionLegend title="run" command="commands/run" label="Run" />
			<Select
				handleTypeSelect={prop.handleTypeSelect}
				prefix=""
				element={run.scriptType}
			/>
			{run.parallel && <PlainText prefix="" element={run.parallel} />}
			<JdbcConnectionProvider>
				<SrcFormSection
					srcElements={run.srcData}
					srcType={run.scriptType.value === "sql" ? "sql" : undefined}
					jdbcOption={run.jdbcOption}
					handleValueChange={() => (_: string) => {}}
					encoding={templateEncoding}
				/>
				<TraversalFormSection
					srcElements={run.srcData}
					handleValueChange={() => (_: string) => {}}
					handleToggleChecked={() => (_: boolean) => {}}
				/>
				{run.templateOption && (
					<TemplateFormSection
						templateOption={run.templateOption}
						showEncoding={!run.srcData.encoding}
						handleValueChange={(param) => (value: string) => {
							if (param === run.templateOption?.encoding) {
								setTemplateEncoding(value);
							}
						}}
						encoding={templateEncoding}
					/>
				)}
			</JdbcConnectionProvider>
			{!run.jdbcOption && (
				<>
					<FileText
						prefix=""
						element={run.baseDir as CommandOption}
						handleValueChange={() => {}}
						hidden={false}
					/>
					{run.scriptType.value === "ant" && (
						<PlainText
							prefix=""
							element={run.antTarget as CommandOption}
							handleValueChange={() => {}}
							hidden={false}
						/>
					)}
				</>
			)}
		</SectionFieldset>
	);
}
