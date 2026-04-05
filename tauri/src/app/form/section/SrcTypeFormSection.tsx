import {
	type CommandParam,
	isCsvqType,
	isCsvType,
	isFixedType,
	isRegType,
	isSqlType,
	isTableType,
	isXlsType,
	isXlsxType,
	type SrcTypeSettings,
} from "../../../model/CommandParam";
import CsvFormSection from "./srctype/CsvFormSection";
import CsvqFormSection from "./srctype/CsvqFormSection";
import DBFormSection from "./srctype/DBFormSection";
import ExcelFormSection from "./srctype/ExcelFormSection";
import FixedFormSection from "./srctype/FixedFormSection";
import RegFormSection from "./srctype/RegFormSection";

export default function SrcTypeFormSection({
	commandParams,
	handleValueChange,
	handleToggleChecked,
}: {
	commandParams: SrcTypeSettings;
	handleValueChange: (param: CommandParam) => (newValue: string) => void;
	handleToggleChecked: (param: CommandParam) => (checked: boolean) => void;
}) {
	if (isCsvType(commandParams)) {
		return (
			<CsvFormSection
				settings={commandParams}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	if (isCsvqType(commandParams)) {
		return (
			<CsvqFormSection
				settings={commandParams}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	if (isTableType(commandParams) || isSqlType(commandParams)) {
		return (
			<DBFormSection
				settings={commandParams}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	if (isRegType(commandParams)) {
		return (
			<RegFormSection
				settings={commandParams}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	if (isFixedType(commandParams)) {
		return (
			<FixedFormSection
				settings={commandParams}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	if (isXlsType(commandParams) || isXlsxType(commandParams)) {
		return (
			<ExcelFormSection
				settings={commandParams}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	return null;
}
