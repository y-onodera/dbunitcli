import type { ComponentType } from "react";
import { Fragment, useState } from "react";
import { ExpandButton } from "../../../components/element/ButtonIcon";
import type {
	CommandParam,
	CommandParams,
	DatasetSrcInfo,
	SrcInfo,
} from "../../../model/CommandParam";
import Check from "./Check";
import type { Prop } from "./FormElementProp";
import Select from "./Select";
import Text from "./TextFormElement";

export function buildSrcInfo(elements: CommandParam[]): SrcInfo {
	const find = (name: string) => elements.find((e) => e.name === name);
	return {
		srcPath: find("src")?.value ?? "",
		encoding: find("encoding")?.value,
		regTableInclude: find("regTableInclude")?.value ?? "",
		regTableExclude: find("regTableExclude")?.value ?? "",
		recursive: find("recursive")?.value ?? "",
		regInclude: find("regInclude")?.value ?? "",
		regExclude: find("regExclude")?.value ?? "",
		extension: find("extension")?.value ?? "",
	};
}

export function buildDatasetSrcInfo(elements: CommandParam[]): DatasetSrcInfo {
	const find = (name: string) => elements.find((e) => e.name === name);
	return {
		...buildSrcInfo(elements),
		srcType: find("srcType")?.value ?? "",
		xlsxSchema: find("xlsxSchema")?.value ?? "",
		fixedLength: find("fixedLength")?.value ?? "",
		regHeaderSplit: find("regHeaderSplit")?.value ?? "",
		regDataSplit: find("regDataSplit")?.value ?? "",
		encoding: find("encoding")?.value ?? "",
		delimiter: find("delimiter")?.value ?? "",
		ignoreQuoted: find("ignoreQuoted")?.value === "true",
		headerName: find("headerName")?.value ?? "",
		startRow: find("startRow")?.value ?? "",
		addFileInfo: find("addFileInfo")?.value === "true",
	};
}

export default function CommandFormElements(
	prop: {
		handleTypeSelect: (selected: string) => Promise<void>;
		hideDatasetSettingEdit?: boolean;
		textComponent?: ComponentType<Prop>;
	} & CommandParams,
) {
	const TextComponent = prop.textComponent ?? Text;
	const [showOptional, setShowOptional] = useState(false);
	const toggleOptional = () => setShowOptional(!showOptional);

	const firstOptionalName = prop.optionCaption
		? prop.elements.find((e) => prop.optional?.(e.name))?.name
		: undefined;

	return (
		<>
			{prop.elements.map((element) => {
				const showExpandButton = element.name === firstOptionalName;
				if (element.attribute.type === "FLG") {
					return (
						<Fragment key={prop.name + prop.prefix + element.name}>
							{showExpandButton && (
								<div className="pt-2.5">
									<ExpandButton
										toggleOptional={toggleOptional}
										showOptional={showOptional}
										caption={prop.optionCaption?.caption}
									/>
								</div>
							)}
							<Check
								prefix={prop.prefix}
								element={element}
								hidden={prop.optional?.(element.name) && !showOptional}
							/>
						</Fragment>
					);
				}
				if (element.attribute.type === "ENUM") {
					return (
						<Fragment key={prop.name + prop.prefix + element.name}>
							{showExpandButton && (
								<div className="pt-2.5">
									<ExpandButton
										toggleOptional={toggleOptional}
										showOptional={showOptional}
										caption={prop.optionCaption?.caption}
									/>
								</div>
							)}
							<Select
								handleTypeSelect={prop.handleTypeSelect}
								prefix={prop.prefix}
								element={element}
								hidden={prop.optional?.(element.name) && !showOptional}
							/>
						</Fragment>
					);
				}
				return (
					<Fragment key={prop.name + prop.prefix + element.name}>
						{showExpandButton && (
							<div className="pt-2.5">
								<ExpandButton
									toggleOptional={toggleOptional}
									showOptional={showOptional}
									caption={prop.optionCaption?.caption}
								/>
							</div>
						)}
						<TextComponent
							prefix={prop.prefix}
							element={element}
							hidden={prop.optional?.(element.name) && !showOptional}
							hideDatasetSettingEdit={prop.hideDatasetSettingEdit}
						/>
					</Fragment>
				);
			})}
		</>
	);
}
