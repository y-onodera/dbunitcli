import { useState } from "react";
import {
	DialogBody,
	DialogField,
	InlineActions,
	SettingDialog,
} from "../../../../components/dialog";
import { ButtonWithIcon } from "../../../../components/element/Button";
import { BlueEditButton } from "../../../../components/element/ButtonIcon";
import {
	ControllTextBox,
	InputLabel,
	PreviewField,
	ResourceDatalist,
	SelectBox,
} from "../../../../components/element/Input";
import {
	buildTemplateExpression,
	NAME_FORMATS,
	type TemplateExpressionSyntax,
} from "../../../../model/TemplateExpression";
import { replaceSelection } from "../../../../utils/textInsert";
import { useTextareaInsert } from "./useTextareaInsert";

const VALUE_ID = "resultPathTemplate_value";
const VARIABLE_ID = "resultPathTemplate_variable";
const FORMAT_ID = "resultPathTemplate_format";
const VARIABLE_CANDIDATES = ["tableName", "rowNumber"];

type ResultPathTemplateDialogProps = {
	currentValue: string;
	syntax: TemplateExpressionSyntax;
	handleDialogClose: () => void;
	handleSave: (value: string) => void;
};

export default function ResultPathTemplateDialog({
	currentValue,
	syntax,
	handleDialogClose,
	handleSave,
}: ResultPathTemplateDialogProps) {
	const { value, setValue, textareaRef, insert } =
		useTextareaInsert<HTMLInputElement>(currentValue);
	const [variable, setVariable] = useState("tableName");
	const [format, setFormat] = useState("");
	const expression = variable
		? buildTemplateExpression(syntax, variable, format)
		: "";

	const handleInsert = () => {
		if (expression) {
			insert((v, start, end) => replaceSelection(v, expression, start, end));
		}
	};

	return (
		<SettingDialog
			title="Result Path Template"
			helpCommand="dialogs/result-path-template"
			helpLabel="Result Path Template"
			setting={value}
			handleDialogClose={handleDialogClose}
			handleCommit={handleSave}
			commitLabel="Apply"
		>
			<DialogBody size="sm">
				<DialogField>
					<InputLabel text="resultPath" id={VALUE_ID} required={false} />
					<ControllTextBox
						ref={textareaRef}
						name={VALUE_ID}
						id={VALUE_ID}
						required={false}
						value={value}
						handleChange={(ev) => setValue(ev.target.value)}
					/>
				</DialogField>

				<DialogField>
					<InputLabel text="Variable" id={VARIABLE_ID} required={true} />
					<ControllTextBox
						name={VARIABLE_ID}
						id={VARIABLE_ID}
						required={true}
						list={`${VARIABLE_ID}_list`}
						value={variable}
						handleChange={(ev) => setVariable(ev.target.value)}
					/>
					<ResourceDatalist id={VARIABLE_ID} resources={VARIABLE_CANDIDATES} />
				</DialogField>

				<DialogField>
					<InputLabel text="Format" id={FORMAT_ID} required={false} />
					<SelectBox
						name={FORMAT_ID}
						id={FORMAT_ID}
						required={false}
						wStyle="w-full"
						defaultValue={format}
						handleOnChange={setFormat}
					>
						<option value="">(none)</option>
						{NAME_FORMATS.map((it) => (
							<option key={it} value={it}>
								{it}
							</option>
						))}
					</SelectBox>
				</DialogField>

				<DialogField last>
					<PreviewField
						id="resultPathTemplate_expression"
						label="Expression"
						value={expression}
						placeholder="(Enter a variable)"
					/>
					<InlineActions>
						<ButtonWithIcon
							id="resultPathTemplate_insert"
							handleClick={handleInsert}
						>
							Insert
						</ButtonWithIcon>
					</InlineActions>
				</DialogField>
			</DialogBody>
		</SettingDialog>
	);
}

export function ResultPathTemplateButton({
	value,
	setValue,
	syntax,
}: {
	value: string;
	setValue: (value: string) => void;
	syntax: TemplateExpressionSyntax;
}) {
	const [showDialog, setShowDialog] = useState(false);
	return (
		<>
			<BlueEditButton
				title="template variable"
				handleClick={() => setShowDialog(true)}
			/>
			{showDialog && (
				<ResultPathTemplateDialog
					currentValue={value}
					syntax={syntax}
					handleDialogClose={() => setShowDialog(false)}
					handleSave={(newValue) => {
						setValue(newValue);
						setShowDialog(false);
					}}
				/>
			)}
		</>
	);
}
