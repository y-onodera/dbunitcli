import type { Dispatch, SetStateAction } from "react";
import { useState } from "react";
import {
	DialogActions,
	DialogField,
	DialogTitle,
	ModalOverlay,
	StatusMessage,
} from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import { TextArea } from "../../../../components/element/Input";
import {
	type ColumnExpressionFunction,
	type ColumnExpressionOption,
	columnExpressionArgs,
	expressionOperators,
	newColumnExpressionOption,
	toColumnExpression,
} from "../../../../model/ColumnExpression";
import { replaceSelection } from "../../../../utils/textInsert";
import ColumnSelectDialog from "./ColumnSelectDialog";
import { OptionSelect, OptionText, type SelectOption } from "./SqlInsertParts";
import { useTextareaInsert } from "./useTextareaInsert";

const functionOptions = Object.keys(columnExpressionArgs).map((fn) => ({
	value: fn,
	label: fn,
})) as SelectOption<ColumnExpressionFunction>[];

function ColumnExpressionFields({
	option,
	setOption,
}: {
	option: ColumnExpressionOption;
	setOption: Dispatch<SetStateAction<ColumnExpressionOption>>;
}) {
	return (
		<>
			<DialogField>
				<OptionSelect
					id="function"
					label="function"
					options={functionOptions}
					onChange={(fn) => setOption((cur) => ({ ...cur, fn }))}
				/>
			</DialogField>
			{columnExpressionArgs[option.fn].map((arg) => (
				<DialogField key={arg}>
					<OptionText
						id={arg}
						label={arg}
						value={option[arg]}
						onChange={(value) => setOption((cur) => ({ ...cur, [arg]: value }))}
					/>
				</DialogField>
			))}
		</>
	);
}

export function ColumnExpressionSelectDialog({
	columns,
	defaultValue,
	onApply,
	onClose,
}: {
	columns: string[];
	defaultValue: string;
	onApply: (entries: [string, string][]) => void;
	onClose: () => void;
}) {
	const [option, setOption] = useState(() =>
		newColumnExpressionOption(defaultValue),
	);
	return (
		<ColumnSelectDialog
			columns={columns}
			onApply={(selected) =>
				onApply(
					selected.map((column) => [
						column,
						toColumnExpression(column, option),
					]),
				)
			}
			onClose={onClose}
		>
			<ColumnExpressionFields option={option} setOption={setOption} />
		</ColumnSelectDialog>
	);
}

export function ExpressionEditDialog({
	name,
	value,
	columns,
	defaultValue,
	onApply,
	onClose,
}: {
	name: string;
	value: string;
	columns: string[];
	defaultValue: string;
	onApply: (value: string) => void;
	onClose: () => void;
}) {
	const {
		value: text,
		setValue: setText,
		textareaRef,
		insert,
	} = useTextareaInsert(value);
	const [operator, setOperator] = useState(expressionOperators[0].value);
	const [column, setColumn] = useState(columns[0] ?? "");
	const [option, setOption] = useState(() =>
		newColumnExpressionOption(defaultValue),
	);

	const insertText = (inserted: string) =>
		insert((current, start, end) =>
			replaceSelection(current, inserted, start, end),
		);

	function renderInsertColumn() {
		if (columns.length === 0) {
			return (
				<StatusMessage kind="empty">
					Load columns to insert a column.
				</StatusMessage>
			);
		}
		return (
			<>
				<DialogField>
					<OptionSelect
						id="column"
						label="column"
						options={columns.map((c) => ({ value: c, label: c }))}
						onChange={setColumn}
					/>
				</DialogField>
				<ColumnExpressionFields option={option} setOption={setOption} />
				<DialogField>
					<BlueButton
						title="Insert Column"
						handleClick={() => insertText(toColumnExpression(column, option))}
					/>
				</DialogField>
			</>
		);
	}

	return (
		<ModalOverlay width="w-2xl" zClass="z-modal-nested">
			<DialogTitle>{`Edit ${name}`}</DialogTitle>
			<DialogField>
				<TextArea
					ref={textareaRef}
					id="expression"
					value={text}
					spellCheck={false}
					onChange={(ev) => setText(ev.target.value)}
				/>
			</DialogField>
			<DialogField>
				<OptionSelect
					id="operator"
					label="operator"
					options={expressionOperators}
					onChange={setOperator}
				/>
			</DialogField>
			<DialogField>
				<BlueButton
					title="Insert Operator"
					handleClick={() => insertText(operator)}
				/>
			</DialogField>
			{renderInsertColumn()}
			<DialogActions>
				<BlueButton
					title="Apply"
					handleClick={() => {
						onApply(text);
						onClose();
					}}
				/>
				<WhiteButton title="Close" handleClick={onClose} />
			</DialogActions>
		</ModalOverlay>
	);
}
