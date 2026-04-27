import { useState } from "react";

export function useTableSelection(initial: string[] = []) {
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
		setSelected((prev) => {
			const next = new Set(prev);
			if (checked) {
				for (const t of targets) {
					next.add(t);
				}
			} else {
				for (const t of targets) {
					next.delete(t);
				}
			}
			return next;
		});
	};

	return { selected, toggle, toggleAll };
}
