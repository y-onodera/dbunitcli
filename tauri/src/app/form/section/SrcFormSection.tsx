import type {
	CommandOption,
	JdbcOption,
	SrcElements,
	SrcType,
} from "../../../model/CommandOption";
import { isSqlRelatedType } from "../../../model/QueryDatasource";
import EncodingText from "./element/EncodingText";
import FileText from "./element/FileText";
import SqlSrcText from "./element/SqlSrcText";
import JdbcFormSection from "./JdbcFormSection";

export default function SrcFormSection({
	srcElements,
	srcType,
	jdbcOption,
	handleValueChange,
}: {
	srcElements: SrcElements;
	srcType?: SrcType;
	jdbcOption?: JdbcOption;
	handleValueChange: (param: CommandOption) => (newValue: string) => void;
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
				<EncodingText
					prefix={prefix}
					element={srcElements.encoding}
					handleValueChange={handleValueChange(srcElements.encoding)}
					hidden={false}
				/>
			)}
		</>
	);
}
