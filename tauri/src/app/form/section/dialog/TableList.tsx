import { ButtonIcon } from "../../../../components/element/ButtonIcon";
import { PreviewIcon } from "../../../../components/element/Icon";
import SelectableTable from "./SelectableTable";

export interface TableListProps {
	tables: string[];
	selected: Set<string>;
	onToggleAll: (tables: string[], checked: boolean) => void;
	onToggle: (table: string) => void;
	// 行の「Select columns」アイコンで、そのテーブルの列選択を開く
	onSelectTable: (table: string) => void;
}

export default function TableList({
	tables,
	selected,
	onToggleAll,
	onToggle,
	onSelectTable,
}: TableListProps) {
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
					<ButtonIcon
						handleClick={(e) => {
							e.stopPropagation();
							onSelectTable(table);
						}}
					>
						<PreviewIcon title="Select columns" />
					</ButtonIcon>
				</div>
			)}
		/>
	);
}
