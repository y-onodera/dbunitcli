import { useEffect, useRef, useState } from "react";
import {
	DialogActions,
	DialogTitle,
	ModalOverlay,
} from "../../../../components/dialog";
import { BlueButton, WhiteButton } from "../../../../components/element/Button";
import { useDatasetSrcInfo } from "../../../../context/DatasetSrcInfoProvider";
import { useColumnNamesFetcher } from "../../../../hooks/useDatasetSettings";
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
	const abortControllerRef = useRef<AbortController | null>(null);

	useEffect(() => {
		const controller = new AbortController();
		abortControllerRef.current = controller;
		return () => {
			controller.abort();
		};
	}, []);

	const handleLoadColumns = (tableName: string) => {
		if (loadingTable !== null) {
			return;
		}
		if (!abortControllerRef.current) {
			return;
		}
		setLoadingTable(tableName);
		const { signal } = abortControllerRef.current;
		fetchColumnNames(srcInfo, tableName, signal).then((columns) => {
			if (!abortControllerRef.current?.signal.aborted) {
				onLoad(columns);
				onClose();
			}
		});
	};

	function renderRow(name: string) {
		return (
			<tr key={name} className="border-t border-border-faint">
				<td className="px-3 py-1.5 text-sm text-content-muted">{name}</td>
				<td className="px-3 py-1.5 text-right">
					<BlueButton
						title={loadingTable === name ? "Loading..." : "Load Columns"}
						disabled={loadingTable !== null}
						handleClick={() => handleLoadColumns(name)}
					/>
				</td>
			</tr>
		);
	}

	function renderBody() {
		if (filteredTables.length === 0) {
			return (
				<p className="text-sm text-content-disabled py-2">
					No matching tables.
				</p>
			);
		}
		return (
			<div className="overflow-y-auto max-h-72 border border-border-subtle rounded mb-4">
				<table className="w-full text-sm text-left">
					<tbody>{filteredTables.map(renderRow)}</tbody>
				</table>
			</div>
		);
	}

	return (
		<ModalOverlay width="w-[32rem]" zClass="z-modal-nested">
			<DialogTitle>Load Column Datalist</DialogTitle>
			{renderBody()}
			<DialogActions>
				<WhiteButton title="Close" handleClick={onClose} />
			</DialogActions>
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
