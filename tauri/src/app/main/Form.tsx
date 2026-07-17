import ParameterInputProvider from "../../context/ParameterInputProvider";
import {
	type CollectedFormValues,
	collectFormValues,
} from "../../utils/formValues";
import Footer from "../footer/Footer";
import CommandForm from "../form/CommandForm";

const formid = "commandForm";
export default function Form() {
	const formData = (validate: boolean): CollectedFormValues =>
		collectFormValues(formid, validate);
	return (
		<div className="p-2 rounded-lg mt-10">
			<ParameterInputProvider>
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
			</ParameterInputProvider>
		</div>
	);
}
