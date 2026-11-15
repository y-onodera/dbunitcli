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
const HINT_ID = "resultPathTemplate_hint";

type UnitVariables = { candidates: string[]; hint: string };

// 出力1回分のパラメータに入る変数（core の ParameterUnit）
const UNIT_VARIABLES: Record<string, UnitVariables> = {
	record: {
		candidates: ["tableName", "rowNumber"],
		hint: "unit=record: rowNumber is the row number within each table (from 0).",
	},
	table: {
		candidates: ["tableName", "rowNumber"],
		hint: "unit=table: rowNumber is the table number (from 0).",
	},
	dataset: {
		candidates: [],
		hint: "unit=dataset: output is a single file; tableName is not available and rowNumber is always 0.",
	},
};

// unitが返らない（固定成果物型）・未知の値のとき
const DEFAULT_VARIABLES: UnitVariables = {
	candidates: ["tableName", "rowNumber"],
	hint: "",
};

function unitVariables(unit: string | undefined): UnitVariables {
	return (unit ? UNIT_VARIABLES[unit] : undefined) ?? DEFAULT_VARIABLES;
}

type ResultPathTemplateDialogProps = {
	currentValue: string;
	syntax: TemplateExpressionSyntax;
	unit?: string;
	handleDialogClose: () => void;
	handleSave: (value: string) => void;
};

export default function ResultPathTemplateDialog({
	currentValue,
	syntax,
	unit,
	handleDialogClose,
	handleSave,
}: ResultPathTemplateDialogProps) {
	const { value, setValue, textareaRef, insert } =
		useTextareaInsert<HTMLInputElement>(currentValue);
	const { candidates, hint } = unitVariables(unit);
	const [variable, setVariable] = useState(candidates[0] ?? "");
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
					<ResourceDatalist id={VARIABLE_ID} resources={candidates} />
					{hint && (
						<p id={HINT_ID} className="text-sm text-content-muted mt-1">
							{hint}
						</p>
					)}
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
	unit,
}: {
	value: string;
	setValue: (value: string) => void;
	syntax: TemplateExpressionSyntax;
	unit?: string;
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
					unit={unit}
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
