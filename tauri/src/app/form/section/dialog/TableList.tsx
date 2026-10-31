import { useState } from "react";
import { Td, Tr } from "../../../../components/dialog";
import { ButtonIcon } from "../../../../components/element/ButtonIcon";
import { ExpandIcon, PreviewIcon } from "../../../../components/element/Icon";
import { useAbortSignalRef } from "../../../../hooks/useAbortSignalRef";
import { getErrorMessage } from "../../../../utils/fetchUtils";
import SelectableTable from "./SelectableTable";

export interface TableListProps {
	tables: string[];
	selected: Set<string>;
	onToggleAll: (tables: string[], checked: boolean) => void;
	onToggle: (table: string) => void;
	onQueryColumns?: (table: string) => Promise<string[]>;
	onSelectTable?: (table: string) => void;
}

export default function TableList({
	tables,
	selected,
	onToggleAll,
	onToggle,
	onQueryColumns,
	onSelectTable,
}: TableListProps) {
	const [columnMap, setColumnMap] = useState<Map<string, string[] | "loading">>(
		new Map(),
	);
	const abortControllerRef = useAbortSignalRef();

	const handleQueryColumns = (table: string) => {
		if (!onQueryColumns) {
			return;
		}
		if (columnMap.get(table) === "loading") {
			return;
		}
		if (columnMap.has(table)) {
			setColumnMap((prev) => {
				const next = new Map(prev);
				next.delete(table);
				return next;
			});
			return;
		}
		setColumnMap((prev) => new Map(prev).set(table, "loading"));
		onQueryColumns(table).then(
			(columns) => {
				if (!abortControllerRef.current?.signal.aborted) {
					setColumnMap((prev) => new Map(prev).set(table, columns));
				}
			},
			// 一覧内の展開では読込ログを出さないため、失敗はalertで知らせる
			(ex) => {
				if (!abortControllerRef.current?.signal.aborted) {
					alert(getErrorMessage(ex));
					setColumnMap((prev) => {
						const next = new Map(prev);
						next.delete(table);
						return next;
					});
				}
			},
		);
	};

	return (
		<SelectableTable
			items={tables}
			selected={selected}
			onToggle={onToggle}
			onToggleAll={onToggleAll}
			columnLabel="Table Name"
			filterPlaceholder="Filter tables..."
			renderItem={(table) => (
				<div className="flex items-center gap-1">
					{table}
					{onQueryColumns && (
						<ButtonIcon
							handleClick={(e) => {
								e.stopPropagation();
								handleQueryColumns(table);
							}}
						>
							{columnMap.get(table) === "loading" ? (
								<span className="text-xs text-content-disabled w-3 h-3">…</span>
							) : (
								<ExpandIcon close={!columnMap.has(table)} />
							)}
						</ButtonIcon>
					)}
					{onSelectTable && (
						<ButtonIcon
							handleClick={(e) => {
								e.stopPropagation();
								onSelectTable(table);
							}}
						>
							<PreviewIcon title="Select columns" />
						</ButtonIcon>
					)}
				</div>
			)}
			renderExtraRows={(table) => {
				const columns = columnMap.get(table);
				if (!Array.isArray(columns)) {
					return null;
				}
				return columns.map((col) => (
					<Tr key={`${table}::${col}`} muted>
						<Td />
						<Td>
							<span className="text-xs">{col}</span>
						</Td>
					</Tr>
				));
			}}
		/>
	);
}
