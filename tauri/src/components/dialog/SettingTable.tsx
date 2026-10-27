import { useState } from "react";
import {
	AddButton,
	CopyButton,
	DeleteButton,
	EditButton,
} from "../element/ButtonIcon";

export interface SettingDialogComponentProps<T> {
	setting: T;
	handleDialogClose: () => void;
	handleCommit: (newSettings: T) => void;
}

interface Displayable {
	displayName(): string;
}

interface SettingTableProps<T extends Displayable> {
	caption: string;
	settings: T[];
	setSettings: (convertFunction: (currentSettings: T[]) => T[]) => void;
	addSettings?: (current: T[], newSettings: T) => T[];
	updateSettings?: (current: T[], beforeSettings: T, newSettings: T) => T[];
	deleteSettings?: (current: T[], targetSettings: T) => T[];
	renderSetting?: (setting: T) => React.ReactNode;
	SettingDialogComponent: React.ComponentType<SettingDialogComponentProps<T>>;
	newSetting: () => T;
	getKey?: (setting: T) => string | number;
}

// 引数の既定値にアロー関数を書くとReact Compilerが未対応のため、モジュールレベルで定義する
function appendSetting<T>(current: T[], item: T): T[] {
	return [...current, item];
}
function replaceSetting<T>(current: T[], before: T, after: T): T[] {
	return current.map((s) => (s === before ? after : s));
}
function removeSetting<T>(current: T[], item: T): T[] {
	return current.filter((s) => s !== item);
}
function displayNameOf(setting: Displayable): string {
	return setting.displayName();
}

export default function SettingTable<T extends Displayable>({
	caption,
	settings,
	setSettings,
	addSettings = appendSetting,
	updateSettings = replaceSetting,
	deleteSettings = removeSetting,
	renderSetting = displayNameOf,
	SettingDialogComponent,
	newSetting,
	getKey = displayNameOf,
}: SettingTableProps<T>) {
	const defaultState = { setting: newSetting(), action: "" };
	const [selectSettings, setSelectSettings] = useState(defaultState);

	return (
		<>
			{selectSettings.action && (
				<SettingDialogComponent
					setting={selectSettings.setting}
					handleDialogClose={() => setSelectSettings(defaultState)}
					handleCommit={(newSettings: T) => {
						setSettings((cur) => {
							if (selectSettings.action === "add") {
								return addSettings(cur, newSettings);
							}
							return updateSettings(cur, selectSettings.setting, newSettings);
						});
						setSelectSettings(defaultState);
					}}
				/>
			)}
			<table className="table-fixed w-full">
				<caption className="caption-top text-label text-left px-2 py-1">
					{caption}
				</caption>
				<thead className="text-xs text-content uppercase bg-surface-subtle">
					<tr>
						<th scope="col" className="px-6 py-3 border">
							Target
						</th>
						<th scope="col" className="px-6 py-3 border">
							Action
						</th>
					</tr>
				</thead>
				<tbody>
					{settings.map((setting) => (
						<tr key={getKey(setting)}>
							<th
								scope="row"
								className="px-6 min-w-80 max-w-80 text-left text-sm text-content border"
							>
								{renderSetting(setting)}
							</th>
							<td className="px-6 border">
								<div className="flex">
									<EditButton
										handleClick={() =>
											setSelectSettings({ setting, action: "update" })
										}
									/>
									<DeleteButton
										handleClick={() =>
											setSettings((current) => deleteSettings(current, setting))
										}
									/>
									<CopyButton
										handleClick={() =>
											setSelectSettings({ setting, action: "add" })
										}
									/>
								</div>
							</td>
						</tr>
					))}
					<tr>
						<th scope="row" className="px-6 py-4">
							<AddButton
								handleClick={() =>
									setSelectSettings({ setting: newSetting(), action: "add" })
								}
							/>
						</th>
						<td className="px-6 py-4" />
					</tr>
				</tbody>
			</table>
		</>
	);
}
