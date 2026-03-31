import { useDatasetSrcInfo } from "../../../context/DatasetSrcInfoProvider";
import type { CommandParam, SrcTypeSettings } from "../../../model/CommandParam";
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
	commandParams: SrcTypeSettings | null;
	handleValueChange: (param: CommandParam) => (newValue: string) => void;
	handleToggleChecked: (param: CommandParam) => (checked: boolean) => void;
}) {
	const { srcType } = useDatasetSrcInfo();

	if (commandParams instanceof CsvTypeSettingsImpl) {
		return (
			<CsvFormSection
				settings={commandParams}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	if (commandParams instanceof CsvqTypeSettingsImpl) {
		return (
			<CsvqFormSection
				settings={commandParams}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	if (commandParams instanceof TableSqlTypeSettingsImpl) {
		return (
			<TableSqlFormSection
				srcType={srcType}
				settings={commandParams}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	if (commandParams instanceof RegTypeSettingsImpl) {
		return (
			<RegFormSection
				settings={commandParams}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	if (commandParams instanceof FixedTypeSettingsImpl) {
		return (
			<FixedFormSection
				settings={commandParams}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	if (commandParams instanceof XlsTypeSettingsImpl) {
		return (
			<XlsFormSection
				srcType={srcType}
				settings={commandParams}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	return null;
}
