import { useEffect, useRef, useState } from "react";
import { useMetadataSettings, useSetMetadataSettings } from "../../../context/MetadataSettingsProvider";
import { type MetadataSetting, type MetadataSettings, newMetadataSetting } from "../../../model/MetadataSettings";
import { BlueButton, WhiteButton } from "../../element/Button";
import { AddButton, CopyButton, DeleteButton, EditButton } from "../../element/ButtonIcon";
import { ControllTextBox, InputLabel } from "../../element/Input";
import SettingDaialog from "./SettingDialog";

export default function SettingsDaialog(props: {
	settingName: string
	handleDialogClose: () => void;
	handleSave: (path: string) => void;
}) {
	const dialogRef = useRef<HTMLDialogElement>(null);
	useEffect(() => { dialogRef.current?.showModal() }, [])
	const [fileName, setFileName] = useState(props.settingName);
	const metaDataSettings = useMetadataSettings();
	const setMetadataSettings = useSetMetadataSettings();
	return (
		<dialog ref={dialogRef} onClose={props.handleDialogClose}
			className="overflow-y-auto overflow-x-hidden fixed 
                        top-0 right-0 left-0 z-50 
                        bg-white
                        border border-gray-200"
		>
			<div className="relative overflow-x-auto">
				<SettingsTable caption="Add Metadata Settings" settings={metaDataSettings.settings}
					setMetadataSettings={setMetadataSettings}
					addSettings={(current, settings) => current.add(settings)}
					updateSettings={(current, before, after) => current.update(before, after)}
					deleteSettings={(current, settings) => current.delete(settings)}
				/>
				<SettingsTable caption="Common Settings" settings={metaDataSettings.commonSettings}
					setMetadataSettings={setMetadataSettings}
					addSettings={(current, settings) => current.addCommon(settings)}
					updateSettings={(current, before, after) => current.updateCommon(before, after)}
					deleteSettings={(current, settings) => current.deleteCommon(settings)}
				/>
				<div className="right-1 w-full flex items-center justify-end">
					<div className="grid grid-cols-5 pb-2">
						<InputLabel id="fileNameLabel" name="name" required={false} wStyle="p-2.5 w=1/5" />
						<ControllTextBox name="fileName" id="fileName" required={true} wStyle="col-start-2 col-span-4 mr-2"
							value={fileName}
							handleChange={ev => setFileName(ev.target.value)}
						/>
					</div>
					<div className="pb-2">
						<BlueButton title="Save" handleClick={() => props.handleSave(fileName)} />
						<WhiteButton title="Close" handleClick={props.handleDialogClose} />
					</div>
				</div>
			</div>
		</dialog>
	);
}
export function SettingsTable(props: {
	caption: string
	, settings: MetadataSetting[]
	, setMetadataSettings: (value: React.SetStateAction<MetadataSettings>) => void
	, addSettings: (current: MetadataSettings, newSettings: MetadataSetting) => MetadataSettings
	, updateSettings: (current: MetadataSettings, beforeSettings: MetadataSetting, newSettings: MetadataSetting) => MetadataSettings
	, deleteSettings: (current: MetadataSettings, targetSettings: MetadataSetting) => MetadataSettings
}) {
	const defaultState = { setting: undefined as unknown as MetadataSetting, action: "" };
	const [selectSettings, setSelectSettings] = useState(defaultState);
	return (
		<>
			{selectSettings.action &&
				<SettingDaialog
					setting={selectSettings.setting}
					handleDialogClose={() => setSelectSettings(defaultState)}
					handleCommit={(newSettings: MetadataSetting) => {
						props.setMetadataSettings(cur => {
							if (selectSettings.action === "add") {
								return props.addSettings(cur, newSettings)
							}
							return props.updateSettings(cur, selectSettings.setting, newSettings)
						})
						setSelectSettings(defaultState)
					}}
				/>
			}
			<table className="table-fixed">
				<caption className="caption-top">
					{props.caption}
				</caption>
				<thead className="text-xs text-gray-700 uppercase bg-gray-50">
					<tr>
						<th scope="col" className="px-6 py-3 border-4">
							Target
						</th>
						<th scope="col" className="px-6 py-3 border-4">
							Action
						</th>
					</tr>
				</thead>
				<tbody>
					{props.settings?.map((setting) => {
						return (
							<>
								<tr key={setting.target()}>
									<th scope="row" className="px-6 min-w-80 max-w-80 text-left text-sm text-gray-900 border-4">
										{setting.target()}
									</th>
									<td className="px-6 border-4">
										<div className="flex">
											<EditButton handleClick={() => setSelectSettings({ setting, action: "update" })} />
											<DeleteButton handleClick={() => props.setMetadataSettings(current => props.deleteSettings(current, setting))} />
											<CopyButton handleClick={() => setSelectSettings({ setting, action: "add" })} />
										</div>
									</td>
								</tr>
							</>)
					})}
					<tr>
						<th scope="row" className="px-6 py-4">
							<AddButton handleClick={() => setSelectSettings({ setting: newMetadataSetting(), action: "add" })} />
						</th>
						<td className="px-6 py-4">
						</td>
					</tr>
				</tbody>
			</table>
		</>
	)
}