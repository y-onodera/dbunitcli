import { JdbcConnectionProvider } from "../../context/JdbcConnectionProvider";
import type { RunOptions } from "../../model/SelectParameter";
import FileText from "./section/element/FileText";
import PlainText from "./section/element/PlainText";
import Select from "./section/element/Select";
import SrcFormSection from "./section/SrcFormSection";

export function RunForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	run: RunOptions;
}) {
	const run = prop.run;
	return (
		<fieldset className="border border-gray-200 p-3">
			<legend>run</legend>
			<Select
				handleTypeSelect={prop.handleTypeSelect}
				prefix=""
				element={run.scriptType}
			/>
			<JdbcConnectionProvider>
				<SrcFormSection
					srcElements={run.srcData}
					srcType={run.scriptType.value === "sql" ? "sql" : undefined}
					jdbcOption={run.jdbcOption}
					templateOption={run.templateOption}
					handleValueChange={() => (_: string) => {}}
					handleToggleChecked={() => (_: boolean) => {}}
				/>
			</JdbcConnectionProvider>
			{!run.jdbcOption && (
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
