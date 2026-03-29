import { useDatasetSrcInfo } from "../../../context/DatasetSrcInfoProvider";
import type { CommandParam, CommandParams } from "../../../model/CommandParam";
import {
	CsvqTypeSettingsImpl,
	CsvTypeSettingsImpl,
	FixedTypeSettingsImpl,
	RegTypeSettingsImpl,
	TableSqlTypeSettingsImpl,
	XlsTypeSettingsImpl,
} from "../../../model/CommandParam";
import CsvFormSection from "./srctype/CsvFormSection";
import CsvqFormSection from "./srctype/CsvqFormSection";
import FixedFormSection from "./srctype/FixedFormSection";
import RegFormSection from "./srctype/RegFormSection";
import TableSqlFormSection from "./srctype/TableSqlFormSection";
import XlsFormSection from "./srctype/XlsFormSection";

export default function DatasetCommandFormSection({
	commandParams,
	handleValueChange,
	handleToggleChecked,
}: {
	commandParams: CommandParams;
	handleValueChange: (param: CommandParam) => (newValue: string) => void;
	handleToggleChecked: (param: CommandParam) => (checked: boolean) => void;
}) {
	const datasetSrcInfo = useDatasetSrcInfo();
	const srcType = datasetSrcInfo?.srcType ?? "";

	if (srcType === "csv") {
		return (
			<CsvFormSection
				settings={new CsvTypeSettingsImpl(commandParams)}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	if (srcType === "csvq") {
		return (
			<CsvqFormSection
				settings={new CsvqTypeSettingsImpl(commandParams)}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	if (srcType === "table" || srcType === "sql") {
		return (
			<TableSqlFormSection
				settings={new TableSqlTypeSettingsImpl(commandParams)}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	if (srcType === "reg") {
		return (
			<RegFormSection
				settings={new RegTypeSettingsImpl(commandParams)}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	if (srcType === "fixed") {
		return (
			<FixedFormSection
				settings={new FixedTypeSettingsImpl(commandParams)}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	if (srcType === "xls" || srcType === "xlsx") {
		return (
			<XlsFormSection
				settings={new XlsTypeSettingsImpl(commandParams)}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	return null;
}
