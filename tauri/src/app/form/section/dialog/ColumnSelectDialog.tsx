import { useState } from "react";
import {
	DataTable,
	DialogActions,
	DialogField,
	DialogTitle,
	ModalOverlay,
	StatusMessage,
	Td,
	Tr,
} from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";

export default function ColumnSelectDialog({
	columns,
	onApply,
	onClose,
}: {
	columns: string[];
	onApply: (selected: string[]) => void;
	onClose: () => void;
}) {
	const [selected, setSelected] = useState<string[]>([]);

	const toggle = (column: string) =>
		setSelected((cur) =>
			cur.includes(column) ? cur.filter((c) => c !== column) : [...cur, column],
		);

	function renderRow(column: string) {
		return (
			<Tr key={column}>
				<Td>
					<input
						type="checkbox"
						aria-label={column}
						className="w-4 h-4 text-primary border border-border ring-primary-ring focus-visible:ring-3"
						checked={selected.includes(column)}
						onChange={() => toggle(column)}
					/>
				</Td>
				<Td>{column}</Td>
			</Tr>
		);
	}

	function renderBody() {
		if (columns.length === 0) {
			return <StatusMessage kind="empty">No columns loaded.</StatusMessage>;
		}
		return (
			<DialogField last>
				<DataTable>{columns.map(renderRow)}</DataTable>
			</DialogField>
		);
	}

	return (
		<ModalOverlay width="w-lg" zClass="z-modal-nested">
			<DialogTitle>Select Columns</DialogTitle>
			{renderBody()}
			<DialogActions>
				<BlueButton
					title="Apply"
					disabled={selected.length === 0}
					handleClick={() => {
						onApply(selected);
						onClose();
					}}
				/>
				<WhiteButton title="Close" handleClick={onClose} />
			</DialogActions>
		</ModalOverlay>
	);
}
