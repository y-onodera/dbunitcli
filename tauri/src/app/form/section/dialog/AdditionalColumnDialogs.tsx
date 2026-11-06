import type { Dispatch, SetStateAction } from "react";
import { useState } from "react";
import {
	DialogActions,
	DialogField,
	DialogTitle,
	ModalOverlay,
	Select,
	StatusMessage,
	Text,
} from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import { TextArea } from "../../../../components/element/Input";
import {
	type ColumnExpressionFunction,
	type ColumnExpressionOption,
	newColumnExpressionOption,
	toColumnExpression,
} from "../../../../model/ColumnExpression";
import ColumnSelectDialog from "./ColumnSelectDialog";

function ColumnExpressionFields({
	option,
	setOption,
}: {
	option: ColumnExpressionOption;
	setOption: Dispatch<SetStateAction<ColumnExpressionOption>>;
}) {
	return (
		<DialogField>
			<Select
				name="function"
				defaultValue={option.fn}
				handleOnChange={async (fn) =>
					setOption((cur) => ({ ...cur, fn: fn as ColumnExpressionFunction }))
				}
			>
				<option value="none">none</option>
				<option value="tryToNum">tryToNum</option>
				<option value="formatDate">formatDate</option>
			</Select>
			{option.fn === "formatDate" && (
				<>
					<Text
						name="from"
						value={option.from}
						handleChange={(ev) =>
							setOption((cur) => ({ ...cur, from: ev.target.value }))
						}
					/>
					<Text
						name="to"
						value={option.to}
						handleChange={(ev) =>
							setOption((cur) => ({ ...cur, to: ev.target.value }))
						}
					/>
				</>
			)}
			{option.fn !== "none" && (
				<Text
					name="default"
					value={option.defaultValue}
					handleChange={(ev) =>
						setOption((cur) => ({ ...cur, defaultValue: ev.target.value }))
					}
				/>
			)}
		</DialogField>
	);
}

// 選んだカラムごとに、カラム名を名前・カラムを参照する式を値とした組を返す
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
	const [column, setColumn] = useState(columns[0] ?? "");
	const [option, setOption] = useState(() =>
		newColumnExpressionOption(defaultValue),
	);

	function renderAppend() {
		if (columns.length === 0) {
			return (
				<StatusMessage kind="empty">
					Load columns to append a column.
				</StatusMessage>
			);
		}
		return (
			<>
				<Select
					name="column"
					defaultValue={column}
					handleOnChange={async (selected) => setColumn(selected)}
				>
					{columns.map((c) => (
						<option key={c} value={c}>
							{c}
						</option>
					))}
				</Select>
				<ColumnExpressionFields option={option} setOption={setOption} />
				<DialogField>
					<BlueButton
						title="Append"
						handleClick={() =>
							setText((cur) => cur + toColumnExpression(column, option))
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
					id="expression"
					value={text}
					spellCheck={false}
					onChange={(ev) => setText(ev.target.value)}
				/>
			</DialogField>
			{renderAppend()}
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
