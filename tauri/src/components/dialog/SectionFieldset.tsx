import type { ReactNode } from "react";

export function SectionFieldset({ children }: { children: ReactNode }) {
	return (
		<fieldset className="border border-border-subtle p-3">{children}</fieldset>
	);
}
