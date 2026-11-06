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
	queryOnly = false,
	encoding,
}: {
	srcElements: SrcElements;
	srcType?: SrcType;
	jdbcOption?: JdbcOption;
	handleValueChange: (param: CommandOption) => (newValue: string) => void;
	// SQLエディタの挿入の用途からDMLを除く
	queryOnly?: boolean;
	// コマンド実行時にSQLを読む文字コード（SQLエディタの読み書きに使う）
	encoding: string;
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
					queryOnly={queryOnly}
					encoding={encoding}
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
