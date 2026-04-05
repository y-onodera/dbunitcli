import { useDatasetSrcInfo } from "../../../context/DatasetSrcInfoProvider";
import type {
	CommandParam,
	CsvqTypeSettings,
	CsvTypeSettings,
	FixedTypeSettings,
	RegTypeSettings,
	SrcTypeSettings,
	TableSqlTypeSettings,
	XlsTypeSettings,
} from "../../../model/CommandParam";
import CsvFormSection from "./srctype/CsvFormSection";
import CsvqFormSection from "./srctype/CsvqFormSection";
import FixedFormSection from "./srctype/FixedFormSection";
import RegFormSection from "./srctype/RegFormSection";
import TableSqlFormSection from "./srctype/TableSqlFormSection";
import XlsFormSection from "./srctype/XlsFormSection";

export default function DatasetCommandFormSection({
	srcType,
	commandParams,
	handleValueChange,
	handleToggleChecked,
}: {
	srcType: string;
	commandParams: SrcTypeSettings;
	handleValueChange: (param: CommandParam) => (newValue: string) => void;
	handleToggleChecked: (param: CommandParam) => (checked: boolean) => void;
}) {
	if (srcType === "csv") {
		return (
			<CsvFormSection
				settings={commandParams as CsvTypeSettings}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	if (srcType === "csvq") {
		return (
			<CsvqFormSection
				settings={commandParams as CsvqTypeSettings}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	if (srcType === "table" || srcType === "sql") {
		return (
			<TableSqlFormSection
				srcType={srcType}
				settings={commandParams as TableSqlTypeSettings}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	if (srcType === "reg") {
		return (
			<RegFormSection
				settings={commandParams as RegTypeSettings}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	if (srcType === "fixed") {
		return (
			<FixedFormSection
				settings={commandParams as FixedTypeSettings}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	if (srcType === "xlsx" || srcType === "xls") {
		return (
			<XlsFormSection
				srcType={srcType}
				settings={commandParams as XlsTypeSettings}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	return null;
}
