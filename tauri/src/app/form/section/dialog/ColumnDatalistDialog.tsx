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
import { JobProgress } from "../../../../components/element/JobProgress";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useAbortSignalRef } from "../../../../hooks/useAbortSignalRef";
import { useColumnNamesFetcher } from "../../../../hooks/useDatasetSettings";
import { useJobLog } from "../../../../hooks/useJobLog";
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
	const [loadingTable, setLoadingTable] = useState<string | null>(null);
	const srcInfo = useDatasetSrcInfo();
	const fetchColumnNames = useColumnNamesFetcher();
	const abortControllerRef = useAbortSignalRef();
	const jobLog = useJobLog();

	// 閉じると（アンマウントでabort）sidecar側の読込も中断する
	const handleLoadColumns = (tableName: string) => {
		if (loadingTable !== null) {
			return;
		}
		if (!abortControllerRef.current) {
			return;
		}
		setLoadingTable(tableName);
		jobLog.reset();
		const { signal } = abortControllerRef.current;
		fetchColumnNames(srcInfo, tableName, signal, jobLog.append).then(
			(columns) => {
				if (!abortControllerRef.current?.signal.aborted) {
					onLoad(columns);
					onClose();
				}
			},
		);
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
			{loadingTable !== null ? (
				<JobProgress
					title={`Loading ${loadingTable}...`}
					lines={jobLog.logs}
					onAbandon={onClose}
				/>
			) : (
				<>
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
