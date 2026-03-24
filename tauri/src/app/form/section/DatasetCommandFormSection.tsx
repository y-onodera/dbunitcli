import { useDatasetSrcInfo } from "../../../context/DatasetSrcInfoProvider";
import type { CommandParams } from "../../../model/CommandParam";
import {
	CsvqTypeSettingsImpl,
	CsvTypeSettingsImpl,
	FixedTypeSettingsImpl,
	RegTypeSettingsImpl,
	TableSqlTypeSettingsImpl,
	XlsTypeSettingsImpl,
} from "../../../model/CommandParam";
import CsvFormSection from "./CsvFormSection";
import CsvqFormSection from "./CsvqFormSection";
import FixedFormSection from "./FixedFormSection";
import RegFormSection from "./RegFormSection";
import TableSqlFormSection from "./TableSqlFormSection";
import XlsFormSection from "./XlsFormSection";

export default function DatasetCommandFormSection({
	commandParams,
}: {
	commandParams: CommandParams;
}) {
	const datasetSrcInfo = useDatasetSrcInfo();
	const srcType = datasetSrcInfo?.srcType ?? "";

	if (srcType === "csv") {
		return <CsvFormSection settings={new CsvTypeSettingsImpl(commandParams)} />;
	}
	if (srcType === "csvq") {
		return (
			<CsvqFormSection settings={new CsvqTypeSettingsImpl(commandParams)} />
		);
	}
	if (srcType === "table") {
		return (
			<TableSqlFormSection
				settings={new TableSqlTypeSettingsImpl(commandParams)}
			/>
		);
	}
	if (srcType === "sql") {
		return (
			<TableSqlFormSection
				settings={new TableSqlTypeSettingsImpl(commandParams)}
			/>
		);
	}
	if (srcType === "reg") {
		return <RegFormSection settings={new RegTypeSettingsImpl(commandParams)} />;
	}
	if (srcType === "fixed") {
		return (
			<FixedFormSection settings={new FixedTypeSettingsImpl(commandParams)} />
		);
	}
	if (srcType === "xls") {
		return <XlsFormSection settings={new XlsTypeSettingsImpl(commandParams)} />;
	}
	if (srcType === "xlsx") {
		return <XlsFormSection settings={new XlsTypeSettingsImpl(commandParams)} />;
	}
	return null;
}
