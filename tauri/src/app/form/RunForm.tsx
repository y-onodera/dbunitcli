import { SectionFieldset } from "../../components/dialog";
import { SectionLegend } from "../../components/dialog/SectionFieldset";
import { JdbcConnectionProvider } from "../../context/JdbcConnectionProvider";
import { ResourceEncodingProvider } from "../../context/ResourceEncodingProvider";
import type { CommandOption } from "../../model/CommandOption";
import type { RunOptions } from "../../model/SelectParameter";
import FileText from "./section/element/FileText";
import PlainText from "./section/element/PlainText";
import Select from "./section/element/Select";
import SrcFormSection from "./section/SrcFormSection";
import TemplateFormSection, {
	useTemplateOptionValues,
} from "./section/TemplateFormSection";
import TraversalFormSection from "./section/TraversalFormSection";

// runは入力値を他の欄で使わないため、値の変化を追わない
const ignoreValueChange = () => () => {};

export function RunForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	run: RunOptions;
}) {
	const run = prop.run;
	const templateEncoding = useTemplateOptionValues(run.templateOption);
	return (
		<SectionFieldset>
			<SectionLegend title="run" command="commands/run" label="Run" />
			<Select
				handleTypeSelect={prop.handleTypeSelect}
				prefix=""
				element={run.scriptType}
			/>
			{run.parallel && <PlainText prefix="" element={run.parallel} />}
			<ResourceEncodingProvider
				encodings={{
					sql: templateEncoding.encoding,
					template: templateEncoding.encoding,
				}}
			>
				<JdbcConnectionProvider>
					<SrcFormSection
						srcElements={run.srcData}
						srcType={run.scriptType.value === "sql" ? "sql" : undefined}
						jdbcOption={run.jdbcOption}
						handleValueChange={ignoreValueChange}
					/>
					<TraversalFormSection
						srcElements={run.srcData}
						handleValueChange={ignoreValueChange}
						handleToggleChecked={ignoreValueChange}
					/>
					{run.templateOption && (
						<TemplateFormSection
							templateOption={run.templateOption}
							showEncoding={!run.srcData.encoding}
							handleValueChange={templateEncoding.handleValueChange}
						/>
					)}
				</JdbcConnectionProvider>
			</ResourceEncodingProvider>
			{!run.jdbcOption && (
				<>
					<FileText prefix="" element={run.baseDir as CommandOption} />
					{run.scriptType.value === "ant" && (
						<PlainText prefix="" element={run.antTarget as CommandOption} />
					)}
				</>
			)}
		</SectionFieldset>
	);
}
