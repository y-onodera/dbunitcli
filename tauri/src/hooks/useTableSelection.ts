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

	const toggleAll = (targets: string[], checked: boolean) => {
		if (checked) {
			setSelected((prev) => {
				const next = new Set(prev);
				for (const t of targets) {
					next.add(t);
				}
				return next;
			});
		} else {
			setSelected((prev) => {
				const next = new Set(prev);
				for (const t of targets) {
					next.delete(t);
				}
				return next;
			});
		}
	};

	return { selected, toggle, toggleAll };
}
