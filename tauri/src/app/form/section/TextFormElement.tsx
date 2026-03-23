import { isSqlRelatedType } from "../../../model/QueryDatasource";
import DatasetSettingText from "./DatasetSettingText";
import FileText from "./FileText";
import type { Prop } from "./FormElementProp";
import ResourceText from "./ResourceText";
import SqlSrcText from "./SqlSrcText";
import TemplateText from "./TemplateText";
import XlsxSchemaText from "./XlsxSchemaText";

export default function Text(prop: Prop) {
	if (prop.element.name === "setting") {
		return <DatasetSettingText {...prop} />;
	}
	if (prop.element.name === "xlsxSchema") {
		return <XlsxSchemaText {...prop} />;
	}
	if (prop.element.name === "src" && isSqlRelatedType(prop.srcType ?? "")) {
		return <SqlSrcText {...prop} />;
	}
	if (prop.element.name === "templateGroup") {
		return <TemplateText {...prop} />;
	}
	if (
		prop.element.attribute.type.includes("FILE") ||
		prop.element.attribute.type.includes("DIR")
	) {
		return <FileText {...prop} />;
	}
	return <ResourceText {...prop} />;
}
