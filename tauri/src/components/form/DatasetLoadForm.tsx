import MetadataSettingsProvider from "../../context/MetadataSettingsProvider";
import type { DatasetSource } from "../../model/CommandParam";
import CommandFormElements from "./CommandFormElement";

const traversaldetail = ["recursive", "regInclude", "regExclude", "extension"]
const datasetdetail = ["regTableInclude", "regTableExclude", "loadData", "includeMetaData"]
export function DatasetLoadForm(prop: {
	handleTypeSelect: () => Promise<void>;
	name: string;
	srcData: DatasetSource;
}) {
	return (
		<MetadataSettingsProvider>
			<CommandFormElements
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				prefix={prop.srcData.prefix}
				elements={prop.srcData.elements.slice(0, prop.srcData.indexOfSetting())}
				optional={traversaldetail}
				optionCaption={{ caption: "traversal detail", name: "recursive" }}
			/>
			<CommandFormElements
				handleTypeSelect={prop.handleTypeSelect}
				name={prop.name}
				prefix={prop.srcData.prefix}
				elements={prop.srcData.elements.slice(prop.srcData.indexOfSetting())}
				optional={datasetdetail}
				optionCaption={{ caption: "dataset detail", name: "regTableInclude" }}
			/>
		</MetadataSettingsProvider>
	);
}
