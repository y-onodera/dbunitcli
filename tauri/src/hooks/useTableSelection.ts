import { useState } from "react";

export function useTableSelection(tables: string[], initial: string[] = []) {
	const [selected, setSelected] = useState<Set<string>>(
		() => new Set(initial),
	);

	const toggle = (table: string) => {
		setSelected((prev) => {
			const next = new Set(prev);
			if (next.has(table)) {
				next.delete(table);
			} else {
				next.add(table);
			}
			return next;
		});
	};

	const toggleAll = (checked: boolean) => {
		if (checked) {
			setSelected(new Set(tables));
		} else {
			setSelected(new Set());
		}
	};

	return { selected, toggle, toggleAll };
}
