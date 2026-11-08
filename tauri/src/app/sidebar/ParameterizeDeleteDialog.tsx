import {
	DataTable,
	DialogActions,
	DialogField,
	DialogTitle,
	ModalOverlay,
	Td,
	Th,
	Tr,
} from "../../components/dialog";
import { BlueButton, WhiteButton } from "../../components/element/Button";
import {
	type ParameterizeFile,
	useDeleteParameterize,
} from "../../hooks/useSelectParameter";
import { useTableSelection } from "../../hooks/useTableSelection";

const CHECKBOX_CLASS =
	"w-4 h-4 text-primary border border-border ring-primary-ring focus-visible:ring-3";

export default function ParameterizeDeleteDialog({
	name,
	files,
	onClose,
}: {
	name: string;
	files: ParameterizeFile[];
	onClose: () => void;
}) {
	const deleteParameterize = useDeleteParameterize();
	const { selected, toggle } = useTableSelection(
		files.filter((file) => file.usedBy.length === 0).map((file) => file.path),
	);

	function renderRow(file: ParameterizeFile) {
		const shared = file.usedBy.length > 0;
		return (
			<Tr key={file.path}>
				<Td>
					<input
						type="checkbox"
						aria-label={file.path}
						className={CHECKBOX_CLASS}
						checked={selected.has(file.path)}
						disabled={shared}
						onChange={() => toggle(file.path)}
					/>
				</Td>
				<Td nowrap>{file.option}</Td>
				<Td>
					<span className="break-all">{file.path}</span>
					{shared && (
						<div className="text-caption">used by {file.usedBy.join(", ")}</div>
					)}
				</Td>
			</Tr>
		);
	}

	return (
		<ModalOverlay width="w-2xl">
			<DialogTitle>Delete {name}</DialogTitle>
			<p className="text-caption text-content-muted mb-2">
				Checked files are deleted together with the option.
			</p>
			<DialogField last>
				<DataTable
					head={
						<>
							<Th narrow />
							<Th>option</Th>
							<Th>file</Th>
						</>
					}
				>
					{files.map(renderRow)}
				</DataTable>
			</DialogField>
			<DialogActions>
				<BlueButton
					title="Delete"
					handleClick={() => {
						deleteParameterize(
							name,
							files
								.map((file) => file.path)
								.filter((path) => selected.has(path)),
						);
						onClose();
					}}
				/>
				<WhiteButton title="Close" handleClick={onClose} />
			</DialogActions>
		</ModalOverlay>
	);
}
