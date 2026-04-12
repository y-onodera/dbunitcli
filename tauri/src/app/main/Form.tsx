import Footer from "../footer/Footer";
import CommandForm from "../form/CommandForm";

const formid = "commandForm";
export default function Form() {
	const formData = (
		validate: boolean,
	): {
		values: { [k: string]: FormDataEntryValue };
		validationError: boolean;
	} => {
		const formElement = document.querySelector(`#${formid}`) as HTMLFormElement;
		if (validate && !formElement.reportValidity()) {
			return { values: {}, validationError: true };
		}
		const inputForm = new FormData(formElement);
		return {
			values: Object.fromEntries(inputForm.entries()),
			validationError: false,
		};
	};
	return (
		<div className="p-2 rounded-lg mt-10">
			<form
				id={formid}
				className="grid gap-6 mb-6 pb-20 grid-cols-1"
				noValidate
				onSubmit={(e) => {
					e.preventDefault();
				}}
			>
				<CommandForm formData={formData} />
				<Footer formData={formData} />
			</form>
		</div>
	);
}
