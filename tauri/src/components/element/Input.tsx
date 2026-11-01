import { type ReactNode, useRef, useState } from "react";
import { ClearIcon } from "./Icon";

export function InputLabel(props: {
	text: string;
	id: string;
	required: boolean;
	hidden?: boolean;
	wStyle?: string;
}) {
	return (
		<label
			htmlFor={props.id}
			style={props.hidden ? { display: "none" } : {}}
			className={`block
                   ${props.wStyle ? props.wStyle : "w-full"}
                   font-medium text-sm text-content wrap-anywhere
                   `}
		>
			{props.text}
			{props.required && "*"}
		</label>
	);
}
// 手で全部消したときと同じinputイベントを起こし、onChangeや親でまとめて受けるchange（検証エラーの消去等）をそのまま動かす
function clearInput(input: HTMLInputElement | null) {
	if (!input) {
		return;
	}
	Object.getOwnPropertyDescriptor(
		HTMLInputElement.prototype,
		"value",
	)?.set?.call(input, "");
	input.dispatchEvent(new Event("input", { bubbles: true }));
	input.focus();
}
// 入力欄の右端に重ねる、入力を消すボタン。マウス操作用でTab移動の対象にはしない
function ClearButton(props: { label: string; handleClick: () => void }) {
	return (
		<button
			type="button"
			tabIndex={-1}
			aria-label={props.label}
			className="absolute inset-y-0 end-0 flex items-center pe-2 text-content-muted hover:text-link-hover"
			onClick={props.handleClick}
		>
			<ClearIcon title="clear" />
		</button>
	);
}
export function ControllTextBox(props: {
	name: string;
	id: string;
	required: boolean;
	value: string;
	wStyle?: string;
	list?: string;
	disabled?: boolean;
	hidden?: boolean;
	/** trueなら入力があるとき右端に入力を消すボタンを出す */
	clearable?: boolean;
	handleChange: (ev: React.ChangeEvent<HTMLInputElement>) => void;
	handleBlur?: (ev: React.FocusEvent<HTMLInputElement>) => void;
}) {
	const inputRef = useRef<HTMLInputElement>(null);
	const wStyle = props.wStyle ? props.wStyle : "w-full";
	const showClear =
		props.clearable && props.value !== "" && !props.disabled && !props.hidden;
	const input = (
		<input
			ref={inputRef}
			name={props.name}
			id={props.id}
			type="text"
			list={props.list ?? ""}
			style={props.hidden ? { display: "none" } : {}}
			className={
				props.clearable
					? `${inputStyle("w-full")} ${showClear ? "pe-8" : ""}`
					: inputStyle(wStyle)
			}
			required={props.required}
			disabled={!!props.disabled}
			value={props.value}
			onChange={props.handleChange}
			onBlur={(ev) => props.handleBlur?.(ev)}
			autoComplete={props.list ? "off" : "on"}
		/>
	);
	if (!props.clearable) {
		return input;
	}
	// 格子配置などの幅指定は、入力欄ではなくボタンを重ねるための枠に付ける
	return (
		<div
			className={`relative ${wStyle}`}
			style={props.hidden ? { display: "none" } : {}}
		>
			{input}
			{showClear && (
				<ClearButton
					label={`clear ${props.name}`}
					handleClick={() => clearInput(inputRef.current)}
				/>
			)}
		</div>
	);
}
export function SelectBox(props: {
	name: string;
	id: string;
	required: boolean;
	hidden?: boolean;
	wStyle?: string;
	defaultValue?: string;
	handleOnChange?: (selected: string) => void | Promise<void>;
	children: ReactNode;
}) {
	const [selected, setSelected] = useState(
		props.defaultValue ? props.defaultValue : "",
	);
	return (
		<select
			name={props.name}
			id={props.id}
			className={inputStyle(props.wStyle ? props.wStyle : "w-40")}
			required={props.required}
			style={props.hidden ? { display: "none" } : {}}
			value={selected}
			onChange={(event) => {
				setSelected(event.currentTarget.value);
				props.handleOnChange?.(event.currentTarget.value);
			}}
		>
			{props.children}
		</select>
	);
}
export function CheckBox(props: {
	name: string;
	id: string;
	hidden?: boolean;
	defaultValue?: string;
	handleOnChange?: (checked: boolean) => void;
}) {
	const [checked, setChecked] = useState(props.defaultValue === "true");
	return (
		<>
			<input
				name={props.name}
				id={props.id}
				type="checkbox"
				className="w-4 h-4
                           text-primary
                           border border-border
                           ring-primary-ring
                           focus-visible:ring-3 "
				style={props.hidden ? { display: "none" } : {}}
				checked={checked}
				value={`${checked}`}
				onChange={() => {
					setChecked(!checked);
					props.handleOnChange?.(!checked);
				}}
			/>
			<input
				name={props.name}
				id={`${props.id}hidden`}
				type="hidden"
				value={`${checked}`}
			/>
		</>
	);
}
export function ResourceDatalist({
	id,
	resources,
}: {
	id: string;
	resources: string[];
}) {
	return (
		<datalist id={`${id}_list`}>
			{resources.map((resource) => (
				<option key={resource} value={resource} />
			))}
		</datalist>
	);
}
export function PreviewField({
	id,
	label,
	value,
	placeholder,
}: {
	id: string;
	label: string;
	value: string;
	placeholder?: string;
}) {
	return (
		<div>
			<InputLabel text={label} id={id} required={false} />
			<div
				id={id}
				className="block p-2.5 
                           w-full 
                           text-sm text-content-secondary 
                           rounded-lg 
                           bg-surface-muted 
                           border border-border 
                           font-mono 
                           break-all"
			>
				{value || <span className="text-content-disabled">{placeholder}</span>}
			</div>
		</div>
	);
}
export function TextArea({
	ref,
	...props
}: {
	id?: string;
	ref?: React.Ref<HTMLTextAreaElement>;
	value: string;
	readOnly?: boolean;
	placeholder?: string;
	spellCheck?: boolean;
	onChange?: (ev: React.ChangeEvent<HTMLTextAreaElement>) => void;
}) {
	return (
		<textarea
			id={props.id}
			ref={ref}
			className="p-2.5
                       w-full h-96
                       text-sm text-content
                       rounded-lg
                       bg-input
                       disabled:bg-input-disabled
                       border border-border
                       ring-primary-ring
                       focus-visible:ring-3 "
			value={props.value}
			readOnly={props.readOnly}
			placeholder={props.placeholder}
			spellCheck={props.spellCheck}
			onChange={props.onChange}
		/>
	);
}
export function FilterInput({
	value,
	onChange,
	placeholder,
}: {
	value: string;
	onChange: (value: string) => void;
	placeholder?: string;
}) {
	const inputRef = useRef<HTMLInputElement>(null);
	return (
		<div className="relative mb-1">
			<input
				ref={inputRef}
				type="text"
				value={value}
				onChange={(e) => onChange(e.target.value)}
				placeholder={placeholder}
				className={`w-full px-2 py-1 text-sm border border-border rounded bg-input focus-visible:ring-3 ring-primary-ring ${value === "" ? "" : "pe-8"}`}
			/>
			{value !== "" && (
				<ClearButton
					label="clear filter"
					handleClick={() => {
						onChange("");
						inputRef.current?.focus();
					}}
				/>
			)}
		</div>
	);
}
function inputStyle(wStyle: string) {
	return `block
               p-2.5
               ${wStyle}
               text-sm text-content
               rounded-lg
               bg-input
               disabled:bg-input-disabled
               border border-border
               ring-primary-ring
               focus-visible:ring-3 `;
}
