import { SectionFieldset } from "../../../components/dialog";
import { SectionLegend } from "../../../components/dialog/SectionFieldset";
import type { CommandOption } from "../../../model/CommandOption";
import type { GenerateOptions } from "../../../model/SelectParameter";
import PlainText from "./element/PlainText";

// generateType=xlsxTemplate・templateType=compareで、比べる2テーブルと突合の列を指定する
export default function XlsxCompareFormSection({
	generate,
}: {
	generate: GenerateOptions;
}) {
	const fields = [
		generate.oldTable,
		generate.newTable,
		generate.keys,
		generate.compares,
		generate.compareSheetName,
	].filter((field): field is CommandOption => field != null);
	if (fields.length === 0) {
		return null;
	}
	return (
		<SectionFieldset>
			<SectionLegend
				title="compare"
				command="sections/xlsx-compare"
				label="Compare"
			/>
			{fields.map((field) => (
				<PlainText key={field.name} prefix="" element={field} />
			))}
		</SectionFieldset>
	);
}
