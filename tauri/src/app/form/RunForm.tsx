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
						handleValueChange={() => (_: string) => {}}
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
