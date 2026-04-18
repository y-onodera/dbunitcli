import { JdbcConnectionProvider } from "../../context/JdbcConnectionProvider";
import type { RunOptions } from "../../model/SelectParameter";
import FileText from "./section/element/FileText";
import PlainText from "./section/element/PlainText";
import Select from "./section/element/Select";
import JdbcFormSection from "./section/JdbcFormSection";
import SrcFormSection from "./section/SrcFormSection";
import TemplateFormSection from "./section/TemplateFormSection";

export function RunForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	run: RunOptions;
}) {
	const run = prop.run;
	const srcData = run.srcData;
	const templateOption = run.templateOption;
	const jdbcOption = run.jdbcOption;
	return (
		<fieldset className="border border-gray-200 p-3">
			<legend>run</legend>
			<Select
				handleTypeSelect={prop.handleTypeSelect}
				prefix=""
				element={run.scriptType}
			/>
			<JdbcConnectionProvider>
				{jdbcOption && <JdbcFormSection jdbcOption={jdbcOption} />}
				<SrcFormSection
					srcElements={srcData}
					srcType={run.scriptType.value === "sql" ? "sql" : undefined}
					handleValueChange={() => (_: string) => {}}
					handleToggleChecked={() => (_: boolean) => {}}
				/>
			</JdbcConnectionProvider>
			{(templateOption && (
				<TemplateFormSection templateOption={templateOption} />
			)) || (
				<>
					<FileText
						prefix=""
						element={run.baseDir}
						handleValueChange={() => {}}
						hidden={false}
					/>
					{run.scriptType.value === "ant" && (
						<PlainText
							prefix=""
							element={run.antTarget}
							handleValueChange={() => {}}
							hidden={false}
						/>
					)}
				</>
			)}
		</fieldset>
	);
}
