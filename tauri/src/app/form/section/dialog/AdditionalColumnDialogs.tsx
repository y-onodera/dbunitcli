import type { Dispatch, SetStateAction } from "react";
import { useLayoutEffect, useRef, useState } from "react";
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
import { insertInlineAtCursor } from "../../../../utils/textInsert";
import ColumnSelectDialog from "./ColumnSelectDialog";
import { OptionSelect, OptionText, type SelectOption } from "./SqlInsertParts";

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
	const [text, setText] = useState(value);
	const [operator, setOperator] = useState(expressionOperators[0].value);
	const [column, setColumn] = useState(columns[0] ?? "");
	const [option, setOption] = useState(() =>
		newColumnExpressionOption(defaultValue),
	);
	const textareaRef = useRef<HTMLTextAreaElement>(null);
	const pendingCursorRef = useRef<number | null>(null);

	const insertAtCursor = (inserted: string) => {
		const textarea = textareaRef.current;
		const start = textarea?.selectionStart ?? text.length;
		const end = textarea?.selectionEnd ?? text.length;
		const result = insertInlineAtCursor(text, [inserted], start, end);
		pendingCursorRef.current = result.cursor;
		setText(result.value);
	};

	// 続けて挿入したときに直前の挿入の後ろへ入るよう、描画後にカーソルを移す
	useLayoutEffect(() => {
		const cursor = pendingCursorRef.current;
		if (cursor === null) {
			return;
		}
		pendingCursorRef.current = null;
		textareaRef.current?.setSelectionRange(cursor, cursor);
	});

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
						handleClick={() =>
							insertAtCursor(toColumnExpression(column, option))
						}
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
					handleClick={() => insertAtCursor(operator)}
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
