import type { ReactNode } from "react";

export function ButtonWithIcon(props: {
	id: string;
	handleClick: React.MouseEventHandler<HTMLButtonElement>;
	children: ReactNode;
}) {
	return (
		<Button
			buttonstyle="p-2.5 ms-2"
			bgcolor="bg-primary hover:bg-primary-hover"
			textstyle="font-medium text-sm text-content-on-primary"
			border="border border-border"
			handleClick={props.handleClick}
		>
			{props.children}
		</Button>
	);
}
export function BlueButton(props: {
	title: string;
	handleClick: React.MouseEventHandler<HTMLButtonElement>;
	disabled?: boolean;
	id?: string;
}) {
	return (
		<Button
			buttonstyle=""
			bgcolor="bg-primary hover:bg-primary-hover"
			textstyle="text-center text-sm font-semibold text-content-on-primary"
			border="border border-border"
			disabled={props.disabled}
			id={props.id}
			handleClick={props.handleClick}
		>
			{props.title}
		</Button>
	);
}
export function WhiteButton(props: {
	title: string;
	handleClick: React.MouseEventHandler<HTMLButtonElement>;
}) {
	return (
		<Button
			buttonstyle=""
			bgcolor="bg-surface hover:bg-surface-disabled-input"
			textstyle="text-center text-sm font-semibold text-content-muted"
			border="border border-border"
			handleClick={props.handleClick}
		>
			{props.title}
		</Button>
	);
}
export function LinkButton(props: {
	title: string;
	handleClick: React.MouseEventHandler<HTMLButtonElement>;
}) {
	return (
		<Button
			buttonstyle="flex items-center justify-start w-full p-1 ms-2"
			bgcolor="hover:bg-surface-muted"
			textstyle="text-left text-content-muted hover:text-link-hover"
			border="outline-hidden"
			handleClick={props.handleClick}
		>
			{props.title}
		</Button>
	);
}
export function Button(props: {
	buttonstyle: string;
	bgcolor: string;
	textstyle: string;
	border: string;
	children: ReactNode;
	disabled?: boolean;
	id?: string;
	ariaLabel?: string;
	handleClick: React.MouseEventHandler<HTMLButtonElement>;
}) {
	return (
		<button
			type="button"
			id={props.id}
			aria-label={props.ariaLabel}
			disabled={props.disabled}
			className={`${props.buttonstyle}
                         ${props.textstyle}
                         ${props.bgcolor}
                         rounded-lg
                         ${props.border}
                         transition duration-100
                         ring-ring
                         focus-visible:ring-3
                         disabled:bg-surface-disabled disabled:text-content-disabled disabled:border-surface-disabled disabled:cursor-not-allowed disabled:hover:bg-surface-disabled`}
			onClick={props.handleClick}
		>
			{props.children}
		</button>
	);
}
