import { type ReactNode, useState } from "react";
import {
	DataTable,
	StatusMessage,
	Td,
	Th,
	Tr,
} from "../../../../components/dialog";
import { FilterInput } from "../../../../components/element/Input";

// 引数の既定値にアロー関数を書くとReact Compilerが未対応のため、モジュールレベルで定義する
const renderAsText = (item: string): ReactNode => item;

// フィルタ入力・全選択チェック・行クリックでの選択切替を持つ一覧（テーブル名・カラム名選択で共通）
export default function SelectableTable({
	items,
	selected,
	onToggle,
	onToggleAll,
	columnLabel,
	filterPlaceholder,
	emptyMessage,
	renderItem = renderAsText,
}: {
	items: string[];
	selected: Set<string>;
	onToggle: (item: string) => void;
	onToggleAll: (items: string[], checked: boolean) => void;
	columnLabel: string;
	filterPlaceholder: string;
	maxHeightClass?: string;
	// 指定時、items が空ならテーブルの代わりに表示する
	emptyMessage?: string;
	renderItem?: (item: string) => ReactNode;
}) {
	const [filter, setFilter] = useState("");
	const filterLower = filter.toLowerCase();
	const filteredItems = filterLower
		? items.filter((it) => it.toLowerCase().includes(filterLower))
		: items;
	const allSelected =
		filteredItems.length > 0 && filteredItems.every((it) => selected.has(it));

	return (
		<div>
			<FilterInput
				value={filter}
				onChange={setFilter}
				placeholder={filterPlaceholder}
			/>
			{items.length === 0 && emptyMessage ? (
				<StatusMessage kind="empty">{emptyMessage}</StatusMessage>
			) : (
				<DataTable
					head={
						<>
							<Th narrow>
								<input
									type="checkbox"
									checked={allSelected}
									onChange={(e) => onToggleAll(filteredItems, e.target.checked)}
									className="w-4 h-4 text-primary"
								/>
							</Th>
							<Th>{columnLabel}</Th>
						</>
					}
				>
					{filteredItems.map((item) => (
						<Tr key={item} onClick={() => onToggle(item)}>
							<Td>
								<input
									type="checkbox"
									checked={selected.has(item)}
									onChange={() => onToggle(item)}
									onClick={(e) => e.stopPropagation()}
									className="w-4 h-4 text-primary"
								/>
							</Td>
							<Td>{renderItem(item)}</Td>
						</Tr>
					))}
				</DataTable>
			)}
		</div>
	);
}
