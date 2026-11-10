import { type ReactNode, useState } from "react";
import type { ResourceLocation } from "../../model/ResourceLocation";
import {
	type OperationResult,
	reportError,
	saveOnSuccess,
} from "../../utils/fetchUtils";
import { useResettableState } from "../../utils/useResettableState";
import { BlueButton, WhiteButton } from "../element/Button";
import {
	AddButton,
	EditButton,
	FactCheckButton,
	RemoveButton,
} from "../element/ButtonIcon";
import {
	CheckBox,
	ControllTextBox,
	InputLabel,
	SelectBox,
} from "../element/Input";
import { DialogBody } from "./DialogLayout";
import { FullDialog } from "./FullDialog";
import { DialogFooter, DialogTitle } from "./ModalOverlay";

type SaveTarget = "overwrite" | "defaultFolder";

// 開いたファイルが規定フォルダ直下なら、上書きと規定フォルダへの保存は同じになる
function inDefaultFolder(location: ResourceLocation): boolean {
	return location.folder === location.defaultFolder;
}

// 開いたファイルへ上書きするか、規定フォルダへ同じファイル名で保存するか。場所が無い（新規・名前変更）なら入力した名前で新規保存
function savePathOf(
	fileName: string,
	location: ResourceLocation | null,
	saveTarget: SaveTarget,
): { path: string; overwrite: boolean } {
	if (location === null) {
		return { path: fileName, overwrite: false };
	}
	if (saveTarget === "defaultFolder" && !inDefaultFolder(location)) {
		return { path: location.fileName, overwrite: false };
	}
	return { path: fileName, overwrite: true };
}

// 開いたファイルのフォルダと保存先の選択。規定フォルダ直下のファイルなら選択肢は同じになるため出さない
function SaveLocationFields({
	opened,
	location,
	saveTarget,
	onChange,
}: {
	opened: ResourceLocation | null;
	location: ResourceLocation | null;
	saveTarget: SaveTarget;
	onChange: (target: SaveTarget) => void;
}) {
	if (opened === null) {
		return null;
	}
	if (location === null) {
		return (
			<p className="text-xs text-content-muted pb-2">
				Name changed: saved as a new file (default folder:{" "}
				{opened.defaultFolder})
			</p>
		);
	}
	return (
		<div className="text-sm pb-2">
			<p
				className="text-xs text-content-muted truncate"
				title={location.folder}
			>
				Opened from: {location.folder}
			</p>
			{!inDefaultFolder(location) && (
				<fieldset className="flex flex-col gap-1 pt-1">
					<label className="flex items-center gap-1">
						<input
							type="radio"
							name="saveTarget"
							className="w-4 h-4 text-primary"
							checked={saveTarget === "overwrite"}
							onChange={() => onChange("overwrite")}
						/>
						Overwrite the opened file
					</label>
					<label className="flex items-center gap-1">
						<input
							type="radio"
							name="saveTarget"
							className="w-4 h-4 text-primary"
							checked={saveTarget === "defaultFolder"}
							onChange={() => onChange("defaultFolder")}
						/>
						<span className="truncate" title={location.defaultFolder}>
							Save to the default folder: {location.defaultFolder}
							{location.defaultFolderHasFile &&
								` (replaces the existing ${location.fileName})`}
						</span>
					</label>
				</fieldset>
			)}
		</div>
	);
}

type CommonProps = {
	handleDialogClose: () => void;
	children: ReactNode;
	commitLabel?: string;
	commitDisabled?: boolean;
	title?: string;
	helpCommand?: string;
	helpLabel?: string;
	// タイトル行に最大化ボタンを出す。編集用 TextArea を置くダイアログで使う
	maximizable?: boolean;
};

// 選んだ保存先へ保存し、成功したらonSaved(入力欄へ入れるパス)を呼ぶ
export type SaveThen = (onSaved: (path: string) => void) => void;

type SettingMode<T> = {
	setting: T;
	handleCommit: (newSettings: T) => void;
	fileName?: never;
	save?: never;
	handleSave?: never;
	location?: never;
	// Commitの右に並べる追加ボタン
	footerActions?: () => ReactNode;
};

type FileMode = {
	setting?: never;
	handleCommit?: never;
	fileName: string;
	// pathへ保存する。overwriteなら開いたファイルへ上書き、そうでなければ規定フォルダ基準で新規保存。
	// 成功したときだけhandleSaveを呼ぶ（失敗時はダイアログを開いたまま）
	save: (path: string, overwrite: boolean) => Promise<OperationResult>;
	handleSave: (path: string) => void;
	// 開いたファイルの場所（見つからない・未保存ならnull）
	location: ResourceLocation | null;
	// Saveの右に並べる追加ボタン。保存が必要ならsaveThenを使う
	footerActions?: (saveThen: SaveThen) => ReactNode;
};

export type SettingDialogProps<T> = CommonProps & (SettingMode<T> | FileMode);

export function SettingDialog<T>(props: SettingDialogProps<T>) {
	const [fileNameValue, setFileNameValue] = useResettableState(
		props.fileName ?? "",
		props.fileName,
	);
	const [saveTarget, setSaveTarget] = useState<SaveTarget>("overwrite");
	const isFileMode = props.fileName !== undefined;
	const fileNameMissing = isFileMode && fileNameValue.trim() === "";
	// 名前を変えたら開いたファイルとは別の新規保存にする
	const location =
		props.location && fileNameValue === props.fileName ? props.location : null;
	const saveThen: SaveThen = (onSaved) => {
		if (isFileMode && !fileNameMissing) {
			const { path, overwrite } = savePathOf(
				fileNameValue,
				location,
				saveTarget,
			);
			saveOnSuccess(
				() =>
					props.save(path, overwrite).catch((error): OperationResult => {
						reportError("Save failed", error);
						return "failed";
					}),
				() => onSaved(path),
			);
		}
	};

	return (
		<FullDialog
			onClose={props.handleDialogClose}
			maximizable={props.maximizable}
		>
			{props.title && (
				<div className="px-4 pt-4">
					<DialogTitle command={props.helpCommand} label={props.helpLabel}>
						{props.title}
					</DialogTitle>
				</div>
			)}
			{isFileMode ? (
				<div className="relative overflow-x-auto flex flex-col gap-4 grow shrink-0">
					{props.children}
				</div>
			) : (
				<DialogBody size="fluid">{props.children}</DialogBody>
			)}
			{isFileMode && (
				<div className="px-4 pt-4">
					<div className="grid grid-cols-5 pb-2">
						<InputLabel
							id="fileNameLabel"
							text="name"
							required={false}
							wStyle="p-2.5"
						/>
						<ControllTextBox
							name="fileName"
							id="fileName"
							required={true}
							wStyle="col-start-2 col-span-4"
							value={fileNameValue}
							handleChange={(ev) => setFileNameValue(ev.target.value)}
						/>
					</div>
					<SaveLocationFields
						opened={props.location ?? null}
						location={location}
						saveTarget={saveTarget}
						onChange={setSaveTarget}
					/>
				</div>
			)}
			<DialogFooter>
				<BlueButton
					title={props.commitLabel ?? "Save"}
					disabled={props.commitDisabled || fileNameMissing}
					handleClick={() => {
						if (isFileMode) {
							saveThen(props.handleSave);
						} else {
							props.handleCommit(props.setting as T);
						}
					}}
				/>
				{isFileMode ? props.footerActions?.(saveThen) : props.footerActions?.()}
				<WhiteButton title="Close" handleClick={props.handleDialogClose} />
			</DialogFooter>
		</FullDialog>
	);
}

export function Fieldset(props: { children: ReactNode; legend?: string }) {
	return (
		<fieldset className="border border-border-subtle p-2.5 m-2">
			{props.legend && (
				<legend className="text-caption font-semibold px-1">
					{props.legend}
				</legend>
			)}
			{props.children}
		</fieldset>
	);
}

export function Check(props: {
	handleOnChange: (checked: boolean) => void;
	name: string;
	value: string;
}) {
	return (
		<div className="grid grid-cols-5 justify-center">
			<InputLabel
				id={props.name}
				text={props.name}
				required={false}
				wStyle="p-2.5"
			/>
			<div className="p-2.5">
				<CheckBox
					name={props.name}
					id={props.name}
					defaultValue={props.value}
					handleOnChange={props.handleOnChange}
				/>
			</div>
		</div>
	);
}

export function Text(props: {
	name: string;
	value: string;
	handleChange: (text: React.ChangeEvent<HTMLInputElement>) => void;
	handleBlur?: (ev: React.FocusEvent<HTMLInputElement>) => void;
	ignoreLabel?: boolean;
	list?: string;
}) {
	return (
		<div className="grid grid-cols-5 justify-center pb-2">
			{!props.ignoreLabel && (
				<InputLabel
					id={props.name}
					text={props.name}
					required={false}
					wStyle="p-2.5"
				/>
			)}
			<ControllTextBox
				name={props.name}
				id={props.name}
				required={true}
				wStyle="col-start-2 col-span-3"
				value={props.value}
				handleChange={props.handleChange}
				handleBlur={props.handleBlur}
				list={props.list}
			/>
		</div>
	);
}

export function Select(props: {
	defaultValue: string;
	handleOnChange: (selected: string) => Promise<void>;
	name: string;
	children: ReactNode;
}) {
	return (
		<div className="grid grid-cols-5 justify-center pb-2">
			<InputLabel
				id={props.name}
				text={props.name}
				required={false}
				wStyle="p-2.5"
			/>
			<SelectBox
				name={props.name}
				id={props.name}
				required={true}
				wStyle="col-start-2 col-span-2"
				defaultValue={props.defaultValue}
				handleOnChange={props.handleOnChange}
			>
				{props.children}
			</SelectBox>
		</div>
	);
}

// 値が空のときとAdd中は、末尾に空行を出す（blankIndex。出さないときは-1）。
// selectなど外から行数が変われば、Add中は自然に終わる
function useBlankRow(count: number) {
	const [addingAt, setAddingAt] = useState<number | null>(null);
	const adding = addingAt === count;
	return {
		adding,
		blankIndex: adding || count === 0 ? count : -1,
		startAdding: () => setAddingAt(count),
		endAdding: () => setAddingAt(null),
	};
}

// 複数行の入力欄で行ごとに一意なid。見出しのlabelは先頭行を指す
function rowInputId(name: string, index: number): string {
	return `${name}_${index}`;
}

// index以外の行に同じ値があるか
export function isDuplicated(
	values: string[],
	text: string,
	index: number,
): boolean {
	return values.some((v, i) => i !== index && v === text);
}

export function Arrays(props: {
	name: string;
	values: string[];
	handleChange: (text: string, index: number) => void;
	handleRemove: (index: number) => void;
	ignoreLabel?: boolean;
	// 先頭行の上に見出しとして出す。expressionは入力欄を広げる
	kind?: "column" | "expression";
	list?: string;
	handleSelect?: (index: number) => void;
	handleEdit?: (index: number) => void;
}) {
	// モデルは空の値を保持しないため、空行は画面だけに出す
	const { adding, blankIndex, startAdding, endAdding } = useBlankRow(
		props.values.length,
	);
	const rows = blankIndex < 0 ? props.values : [...props.values, ""];
	const lastValue = props.values[props.values.length - 1];
	const canAdd = !!lastValue && !adding;
	return (
		<>
			{rows.map((val, index) => {
				const blank = index === blankIndex;
				return (
					<ArraysText
						// biome-ignore lint/suspicious/noArrayIndexKey: 読込んだ設定に同じ値が重複していても一意にし、位置か値が変われば入力中のstateを作り直す
						key={`${index}:${val}`}
						name={props.name}
						val={val}
						index={index}
						handleChange={(text, i) => {
							if (blank) {
								endAdding();
							}
							props.handleChange(text, i);
						}}
						handleRemove={blank ? () => endAdding() : props.handleRemove}
						isDuplicated={(text) => isDuplicated(props.values, text, index)}
						ignoreLabel={props.ignoreLabel}
						kind={props.kind}
						list={props.list}
						handleSelect={props.handleSelect}
						handleEdit={props.handleEdit}
					/>
				);
			})}
			<div className="grid grid-cols-5 justify-center pb-2">
				{canAdd && (
					<div className="col-start-2">
						<AddButton handleClick={() => startAdding()} />
					</div>
				)}
			</div>
		</>
	);
}

export function ArraysText(props: {
	name: string;
	val: string;
	index: number;
	handleChange: (text: string, index: number) => void;
	handleRemove: (index: number) => void;
	isDuplicated?: (text: string) => boolean;
	ignoreLabel?: boolean;
	kind?: "column" | "expression";
	list?: string;
	handleSelect?: (index: number) => void;
	handleEdit?: (index: number) => void;
}) {
	const [text, setText] = useState(props.val);
	// 他の行と同じ値は確定させず、入力前の値に戻す
	const handleBlur = (newVal: React.FocusEvent<HTMLInputElement>) => {
		if (props.isDuplicated?.(newVal.target.value)) {
			setText(props.val);
			return;
		}
		props.handleChange(newVal.target.value, props.index);
	};
	const handleRemove = () => props.handleRemove(props.index);
	const inputId = rowInputId(props.name, props.index);
	const showLabel = !props.ignoreLabel && props.index === 0;
	const wide = props.kind === "expression";
	return (
		<>
			{showLabel && props.kind && (
				<div className="grid grid-cols-5 pb-1">
					<span className="col-start-2 text-caption">{props.kind}</span>
				</div>
			)}
			<div className="grid grid-cols-5 pb-2">
				{showLabel && (
					<InputLabel
						id={inputId}
						text={props.name}
						required={false}
						wStyle="p-2.5"
					/>
				)}
				<ControllTextBox
					name={props.name}
					id={inputId}
					required={true}
					wStyle={wide ? "col-start-2 col-span-2" : "col-start-2"}
					value={text}
					handleChange={(ev) => setText(ev.target.value)}
					handleBlur={handleBlur}
					list={props.list}
				/>
				{(props.handleSelect || props.handleEdit || props.index > 0) && (
					<div
						className={`${wide ? "col-start-4" : "col-start-3"} flex items-center`}
					>
						{props.handleSelect && (
							<FactCheckButton
								title="select"
								handleClick={() => props.handleSelect?.(props.index)}
							/>
						)}
						{props.handleEdit && (
							<EditButton handleClick={() => props.handleEdit?.(props.index)} />
						)}
						{props.index > 0 && <RemoveButton handleClick={handleRemove} />}
					</div>
				)}
			</div>
		</>
	);
}

export function KeyValues(props: {
	name: string;
	values: object;
	handleChange: (index: number, value: { [prop: string]: string }) => void;
	handleRemove: (index: number) => void;
	// 複数の行をまとめて追加する
	handleSelect?: () => void;
	handleEdit?: (index: number) => void;
}) {
	// 名前が確定するまでモデルに入れないため、空行は画面だけに出す
	const entries = Object.entries(props.values);
	const { adding, blankIndex, startAdding, endAdding } = useBlankRow(
		entries.length,
	);
	const rows = blankIndex < 0 ? entries : [...entries, ["", ""]];
	return (
		<>
			{rows.map(([key, value], index) => {
				const blank = index === blankIndex;
				return (
					<KeyValueText
						// 編集ダイアログなど外から式が変われば入力中のstateを作り直す
						key={`${key}\u0000${value}`}
						propKey={key}
						name={props.name}
						value={String(value ?? "")}
						index={index}
						handleChange={(i, newValue) => {
							if (blank) {
								endAdding();
							}
							props.handleChange(i, newValue);
						}}
						handleRemove={blank ? () => endAdding() : props.handleRemove}
						handleEdit={blank ? undefined : props.handleEdit}
						isDuplicated={(text) =>
							isDuplicated(
								entries.map(([k]) => k),
								text,
								index,
							)
						}
					/>
				);
			})}
			<div className="grid grid-cols-5 justify-center pb-2">
				<div className="col-start-2 flex items-center">
					{entries.length > 0 && !adding && (
						<AddButton handleClick={() => startAdding()} />
					)}
					{props.handleSelect && (
						<FactCheckButton title="select" handleClick={props.handleSelect} />
					)}
				</div>
			</div>
		</>
	);
}

export function KeyValueText(props: {
	name: string;
	propKey: string;
	value: string;
	index: number;
	handleChange: (index: number, value: { [prop: string]: string }) => void;
	handleRemove: (index: number) => void;
	handleEdit?: (index: number) => void;
	isDuplicated?: (text: string) => boolean;
}) {
	const [key, setKey] = useState(props.propKey);
	const [value, setValue] = useState(props.value);
	// 他の行と同じ名前は確定させず、入力前の名前に戻す
	const handleKeyBlur = (newVal: React.FocusEvent<HTMLInputElement>) => {
		if (props.isDuplicated?.(newVal.target.value)) {
			setKey(props.propKey);
			return;
		}
		props.handleChange(
			props.index,
			newVal.target.value ? { [newVal.target.value]: value } : {},
		);
	};
	const inputId = rowInputId(props.name, props.index);
	const handleValueBlur = (newVal: React.FocusEvent<HTMLInputElement>) =>
		props.handleChange(props.index, { [key]: newVal.target.value });
	const handleRemove = () => props.handleRemove(props.index);
	return (
		<>
			{props.index === 0 && (
				<div className="grid grid-cols-5 pb-1">
					<span className="col-start-2 text-caption">name</span>
					<span className="col-start-3 col-span-2 ml-1 text-caption">
						expression
					</span>
				</div>
			)}
			<div className="grid grid-cols-5 pb-2">
				{props.index === 0 && (
					<InputLabel
						id={inputId}
						text={props.name}
						required={false}
						wStyle="p-2.5"
					/>
				)}
				<ControllTextBox
					name={props.name}
					id={inputId}
					required={true}
					wStyle="col-start-2"
					value={key}
					handleChange={(ev) => setKey(ev.target.value)}
					handleBlur={handleKeyBlur}
				/>
				<ControllTextBox
					name={props.name}
					id={`${inputId}_value`}
					required={true}
					wStyle="col-start-3 col-span-2 ml-1"
					value={value}
					disabled={!key || key === ""}
					handleChange={(ev) => setValue(ev.target.value)}
					handleBlur={handleValueBlur}
				/>
				{(props.handleEdit || props.index > 0) && (
					<div className="col-start-5 flex items-center">
						{props.handleEdit && (
							<EditButton handleClick={() => props.handleEdit?.(props.index)} />
						)}
						{props.index > 0 && <RemoveButton handleClick={handleRemove} />}
					</div>
				)}
			</div>
		</>
	);
}
