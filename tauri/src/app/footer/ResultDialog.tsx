import type { ReactNode } from "react";

export default function ResultDialog(props: {
	children: ReactNode;
	hidden: boolean;
}) {
	return (
		<div
			id="popup-modal"
			hidden={props.hidden}
			className="overflow-y-auto overflow-x-hidden fixed top-0 right-0 left-0 z-50 "
		>
			<div className="relative p-4 w-full max-w-md max-h-full">
				<div className="relative bg-white rounded-lg shadow dark:bg-gray-700">
					{props.children}
				</div>
			</div>
		</div>
	);
}
