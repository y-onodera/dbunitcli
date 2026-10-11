import { Fragment, type ReactNode, useState } from "react";
import { FilterInput } from "../../../../components/element/Input";

// フィルタ入力・全選択チェック・行クリックでの選択切替を持つ一覧（テーブル名・カラム名選択で共通）
export default function SelectableTable({
	items,
	selected,
	onToggle,
	onToggleAll,
	columnLabel,
	filterPlaceholder,
	maxHeightClass = "max-h-72",
	emptyMessage,
	renderItem = (item) => item,
	renderExtraRows,
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
	// 行の直下へ差し込む追加行（展開したカラム一覧など）
	renderExtraRows?: (item: string) => ReactNode;
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
			<div
				className={`relative overflow-x-auto ${maxHeightClass} overflow-y-auto border border-border-subtle rounded`}
			>
				{items.length === 0 && emptyMessage ? (
					<p className="text-sm text-content-muted px-3 py-2">{emptyMessage}</p>
				) : (
					<table className="w-full text-sm text-left">
						<thead className="bg-surface-subtle sticky top-0">
							<tr>
								<th className="px-3 py-2 w-8">
									<input
										type="checkbox"
										checked={allSelected}
										onChange={(e) =>
											onToggleAll(filteredItems, e.target.checked)
										}
										className="w-4 h-4 accent-primary-hover"
									/>
								</th>
								<th className="px-3 py-2 font-medium text-content-secondary">
									{columnLabel}
								</th>
							</tr>
						</thead>
						<tbody>
							{filteredItems.map((item) => (
								<Fragment key={item}>
									<tr
										className="hover:bg-surface-subtle cursor-pointer border-t border-border-faint"
										onClick={() => onToggle(item)}
									>
										<td className="px-3 py-1.5">
											<input
												type="checkbox"
												checked={selected.has(item)}
												onChange={() => onToggle(item)}
												onClick={(e) => e.stopPropagation()}
												className="w-4 h-4 accent-primary-hover"
											/>
										</td>
										<td className="px-3 py-1.5">{renderItem(item)}</td>
									</tr>
									{renderExtraRows?.(item)}
								</Fragment>
							))}
						</tbody>
					</table>
				)}
			</div>
		</div>
	);
}
