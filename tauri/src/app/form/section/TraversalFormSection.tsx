import { useState } from "react";
import {
	SectionFieldset,
	SectionOptionHeader,
} from "../../../components/dialog";
import type { CommandOption, SrcElements } from "../../../model/CommandOption";
import { RegexFilterButton } from "./dialog/RegexFilterDialog";
import Check from "./element/Check";
import PlainText from "./element/PlainText";

export default function TraversalFormSection({
	srcElements,
	handleValueChange,
	handleToggleChecked,
	regexFilter = true,
}: {
	srcElements: SrcElements;
	handleValueChange: (param: CommandOption) => (newValue: string) => void;
	handleToggleChecked: (param: CommandOption) => (checked: boolean) => void;
	// falseならテーブル一覧から選ぶボタンを出さない（データセットを読まないrun）
	regexFilter?: boolean;
}) {
	const [showOptional, setShowOptional] = useState(false);
	const toggleOptional = () => setShowOptional(!showOptional);
	const prefix = srcElements.prefix;
	return (
		<SectionFieldset>
			<legend>traversal</legend>
			<SectionOptionHeader
				showOptional={showOptional}
				toggleOptional={toggleOptional}
				caption="traversal option"
				command="sections/traversal"
				label="Traversal"
			/>
			{srcElements.extension && (
				<PlainText
					prefix={prefix}
					element={srcElements.extension}
					handleValueChange={handleValueChange(srcElements.extension)}
					hidden={!showOptional}
				/>
			)}
			<Check
				prefix={prefix}
				element={srcElements.recursive}
				handleOnChange={handleToggleChecked(srcElements.recursive)}
				hidden={!showOptional}
			/>
			<PlainText
				prefix={prefix}
				element={srcElements.regInclude}
				handleValueChange={handleValueChange(srcElements.regInclude)}
				hidden={!showOptional}
			>
				{regexFilter
					? ({ path, setPath }) => (
							<RegexFilterButton
								kind="path"
								fieldName={srcElements.regInclude.name}
								value={path}
								onApply={setPath}
							/>
						)
					: undefined}
			</PlainText>
			<PlainText
				prefix={prefix}
				element={srcElements.regExclude}
				handleValueChange={handleValueChange(srcElements.regExclude)}
				hidden={!showOptional}
			>
				{regexFilter
					? ({ path, setPath }) => (
							<RegexFilterButton
								kind="path"
								fieldName={srcElements.regExclude.name}
								value={path}
								onApply={setPath}
							/>
						)
					: undefined}
			</PlainText>
		</SectionFieldset>
	);
}
