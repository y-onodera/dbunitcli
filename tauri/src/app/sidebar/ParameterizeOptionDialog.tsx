import { useState } from "react";
import {
	DataTable,
	DialogActions,
	DialogField,
	DialogTitle,
	ModalOverlay,
	StatusMessage,
	Td,
	Th,
	Tr,
} from "../../components/dialog";
import { BlueButton, WhiteButton } from "../../components/element/Button";
import {
	type ParameterizeCandidate,
	useParameterizeCandidates,
} from "../../hooks/useSelectParameter";

const CHECKBOX_CLASS =
	"w-4 h-4 text-primary border border-border ring-primary-ring focus-visible:ring-3";

export default function ParameterizeOptionDialog({
	command,
	name,
	onApply,
	onClose,
}: {
	command: string;
	name: string;
	onApply: (parameterizeKeys: string[]) => void;
	onClose: () => void;
}) {
	const { candidates, loading } = useParameterizeCandidates(command, name);
	if (loading) {
		return (
			<ModalOverlay width="w-2xl">
				<StatusMessage kind="loading">Loading...</StatusMessage>
			</ModalOverlay>
		);
	}
	return (
		<Dialog
			name={name}
			candidates={candidates}
			onApply={onApply}
			onClose={onClose}
		/>
	);
}

function Dialog({
	name,
	candidates,
	onApply,
	onClose,
}: {
	name: string;
	candidates: ParameterizeCandidate[];
	onApply: (parameterizeKeys: string[]) => void;
	onClose: () => void;
}) {
	const [selected, setSelected] = useState<string[]>(
		candidates.map((it) => it.key),
	);
	const allSelected =
		candidates.length > 0 && selected.length === candidates.length;
	const toggle = (key: string) =>
		setSelected((cur) =>
			cur.includes(key) ? cur.filter((k) => k !== key) : [...cur, key],
		);
	const toggleAll = () =>
		setSelected(allSelected ? [] : candidates.map((it) => it.key));

	function renderRow(candidate: ParameterizeCandidate) {
		return (
			<Tr key={candidate.key}>
				<Td>
					<input
						type="checkbox"
						aria-label={candidate.key}
						className={CHECKBOX_CLASS}
						checked={selected.includes(candidate.key)}
						onChange={() => toggle(candidate.key)}
					/>
				</Td>
				<Td nowrap>{candidate.key}</Td>
				<Td>{candidate.value}</Td>
			</Tr>
		);
	}

	function renderBody() {
		if (candidates.length === 0) {
			return (
				<StatusMessage kind="empty">No options to parameterize.</StatusMessage>
			);
		}
		return (
			<DialogField last>
				<DataTable
					head={
						<>
							<Th narrow>
								<input
									type="checkbox"
									aria-label="select all"
									className={CHECKBOX_CLASS}
									checked={allSelected}
									onChange={toggleAll}
								/>
							</Th>
							<Th>option</Th>
							<Th>value</Th>
						</>
					}
				>
					{candidates.map(renderRow)}
				</DataTable>
			</DialogField>
		);
	}

	return (
		<ModalOverlay width="w-2xl">
			<DialogTitle>Parameterize {name}</DialogTitle>
			<p className="text-caption text-content-muted mb-2">
				Checked options become parameters. Unchecked options keep the saved
				value in the template.
			</p>
			{renderBody()}
			<DialogActions>
				<BlueButton
					title="Parameterize"
					disabled={selected.length === 0}
					handleClick={() => {
						// 送信順をチェックした順ではなく保存順に揃える
						onApply(
							candidates
								.map((it) => it.key)
								.filter((key) => selected.includes(key)),
						);
						onClose();
					}}
				/>
				<WhiteButton title="Close" handleClick={onClose} />
			</DialogActions>
		</ModalOverlay>
	);
}
