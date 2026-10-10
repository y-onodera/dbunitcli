import type {
	CommandOption,
	JdbcOption,
	SrcElements,
	SrcType,
	TemplateOption,
} from "../../../model/CommandOption";
import { isSqlRelatedType } from "../../../model/QueryDatasource";
import FileText from "./element/FileText";
import PlainText from "./element/PlainText";
import SqlSrcText from "./element/SqlSrcText";
import JdbcFormSection from "./JdbcFormSection";
import TemplateFormSection from "./TemplateFormSection";
import TraversalFormSection from "./TraversalFormSection";

export default function SrcFormSection({
	srcElements,
	srcType,
	jdbcOption,
	templateOption,
	handleValueChange,
	handleToggleChecked,
}: {
	srcElements: SrcElements;
	srcType?: SrcType;
	jdbcOption?: JdbcOption;
	templateOption?: TemplateOption;
	handleValueChange: (param: CommandOption) => (newValue: string) => void;
	handleToggleChecked: (param: CommandOption) => (checked: boolean) => void;
}) {
	const prefix = srcElements.prefix;

	return (
		<>
			{jdbcOption && <JdbcFormSection jdbcOption={jdbcOption} />}
			{(srcType && isSqlRelatedType(srcType) && (
				<SqlSrcText
					prefix={prefix}
					element={srcElements.src}
					handleValueChange={handleValueChange(srcElements.src)}
					hidden={false}
					srcType={srcType}
				/>
			)) || (
				<FileText
					prefix={prefix}
					element={srcElements.src}
					handleValueChange={handleValueChange(srcElements.src)}
					hidden={false}
				/>
			)}
			{srcElements.encoding && (
				<PlainText
					prefix={prefix}
					element={srcElements.encoding}
					handleValueChange={handleValueChange(srcElements.encoding)}
					hidden={false}
				/>
			)}
			<TraversalFormSection
				srcElements={srcElements}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
			{templateOption && (
				<TemplateFormSection
					templateOption={templateOption}
					showEncoding={!srcElements.encoding}
					handleValueChange={handleValueChange}
				/>
			)}
		</>
	);
}
