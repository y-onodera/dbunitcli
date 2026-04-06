import {
	type CommandOption,
	isCsvqType,
	isCsvType,
	isFixedType,
	isRegType,
	isSqlType,
	isTableType,
	isXlsType,
	isXlsxType,
	type SrcTypeOptions,
} from "../../../model/CommandParam";
import CsvFormSection from "./srctype/CsvFormSection";
import CsvqFormSection from "./srctype/CsvqFormSection";
import DBFormSection from "./srctype/DBFormSection";
import ExcelFormSection from "./srctype/ExcelFormSection";
import FixedFormSection from "./srctype/FixedFormSection";
import RegFormSection from "./srctype/RegFormSection";

export default function SrcTypeFormSection({
	options,
	handleValueChange,
	handleToggleChecked,
}: {
	options: SrcTypeOptions;
	handleValueChange: (param: CommandOption) => (newValue: string) => void;
	handleToggleChecked: (param: CommandOption) => (checked: boolean) => void;
}) {
	if (isCsvType(options)) {
		return (
			<CsvFormSection
				options={options}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	if (isCsvqType(options)) {
		return (
			<CsvqFormSection
				options={options}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	if (isTableType(options) || isSqlType(options)) {
		return (
			<DBFormSection
				options={options}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	if (isRegType(options)) {
		return (
			<RegFormSection
				options={options}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	if (isFixedType(options)) {
		return (
			<FixedFormSection
				options={options}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	if (isXlsType(options) || isXlsxType(options)) {
		return (
			<ExcelFormSection
				options={options}
				handleValueChange={handleValueChange}
				handleToggleChecked={handleToggleChecked}
			/>
		);
	}
	return null;
}
