import { useState } from "react";
import {
	DataTable,
	DialogActions,
	DialogField,
	DialogTitle,
	JobFailure,
	ModalOverlay,
	StatusMessage,
	Td,
	Tr,
} from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import { JobProgress } from "../../../../components/element/JobProgress";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useColumnNamesFetcher } from "../../../../hooks/useDatasetSettings";
import { useJob } from "../../../../hooks/useJob";
import type { DatasetSetting } from "../../../../model/DatasetSettings";

export default function ColumnDatalistDialog({
	tableNames,
	target,
	onLoad,
	onClose,
}: {
	tableNames: string[];
	target: DatasetSetting;
	onLoad: (columns: string[]) => void;
	onClose: () => void;
}) {
	const filteredTables = filterTablesByTarget(tableNames, target);
	// 読込中の表示に出すテーブル名
	const [loadingTable, setLoadingTable] = useState("");
	const srcInfo = useDatasetSrcInfo();
	const fetchColumnNames = useColumnNamesFetcher();
	// 失敗したら閉じずに、エラーと実行中のログを残す（別のテーブルを選び直せる）。閉じると（アンマウントでabort）sidecar側の読込も中断する
	const job = useJob();

	const handleLoadColumns = async (tableName: string) => {
		if (job.running) {
			return;
		}
		setLoadingTable(tableName);
		const result = await job.run(({ signal, onLog }) =>
			fetchColumnNames(srcInfo, tableName, signal, onLog),
		);
		if (result) {
			onLoad(result.value);
			onClose();
		}
	};

	function renderRow(name: string) {
		return (
			<Tr key={name}>
				<Td>{name}</Td>
				<Td align="right">
					<BlueButton
						title="Load Columns"
						handleClick={() => handleLoadColumns(name)}
					/>
				</Td>
			</Tr>
		);
	}

	function renderBody() {
		if (filteredTables.length === 0) {
			return <StatusMessage kind="empty">No matching tables.</StatusMessage>;
		}
		return (
			<DialogField last>
				<DataTable>{filteredTables.map(renderRow)}</DataTable>
			</DialogField>
		);
	}

	return (
		<ModalOverlay width="w-lg" zClass="z-modal-nested">
			<DialogTitle>Load Column Datalist</DialogTitle>
			{job.running ? (
				<JobProgress
					title={`Loading ${loadingTable}...`}
					lines={job.logs}
					onAbandon={onClose}
				/>
			) : (
				<>
					{job.error && <JobFailure message={job.error} lines={job.logs} />}
					{renderBody()}
					<DialogActions>
						<WhiteButton title="Close" handleClick={onClose} />
					</DialogActions>
				</>
			)}
		</ModalOverlay>
	);
}

function filterTablesByTarget(
	tableNames: string[],
	target: DatasetSetting,
): string[] {
	if (target.handler() === "name" && target.name && target.name.length > 0) {
		const nameSet = new Set(target.name.map((n) => n.toLowerCase()));
		return tableNames.filter((t) => nameSet.has(t.toLowerCase()));
	}
	if (target.handler() === "pattern" && target.pattern?.string) {
		try {
			const regex = new RegExp(target.pattern.string, "i");
			return tableNames.filter((t) => regex.test(t));
		} catch {
			return tableNames;
		}
	}
	return tableNames;
}
