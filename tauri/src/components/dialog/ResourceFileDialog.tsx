import { BlueButton, WhiteButton } from "../element/Button";
import { ControllTextBox, InputLabel } from "../element/Input";

interface DialogProps {
    children: React.ReactNode;
    handleDialogClose: () => void;
    fileName: string;
    setFileName: (fileName: string) => void;
    handleSave: (path: string) => void;
}

export default function ResourceFileDialog({ children, handleDialogClose, fileName, setFileName, handleSave }: DialogProps) {

    return (
        <dialog
            open
            onClose={handleDialogClose}
            className="overflow-y-auto overflow-x-hidden fixed top-0 right-0 left-0 z-50 bg-white border border-gray-200"
        >
            <div className="relative overflow-x-auto">{children}</div>
            <div className="right-1 w-full flex items-center justify-end">
                <div className="grid grid-cols-5 pb-2">
                    <InputLabel id="fileNameLabel" text="name" required={false} wStyle="p-2.5 w=1/5" />
                    <ControllTextBox
                        name="fileName"
                        id="fileName"
                        required={true}
                        wStyle="col-start-2 col-span-4 mr-2"
                        value={fileName}
                        handleChange={(ev) => setFileName(ev.target.value)}
                    />
                </div>
                <div className="pb-2">
                    <BlueButton title="Save" handleClick={() => handleSave(fileName)} />
                    <WhiteButton title="Close" handleClick={handleDialogClose} />
                </div>
            </div>
        </dialog>
    );
}