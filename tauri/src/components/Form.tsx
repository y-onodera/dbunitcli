import { Suspense } from "react";
import CommandForm from "./form/CommandForm";
import Footer from "./form/footer/Footer";

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
		<>
			<div className="p-4 rounded-lg mt-14">
				<form
					id={formid}
					className="grid gap-6 mb-6 grid-cols-1"
					noValidate
					onSubmit={(e) => {
						e.preventDefault();
					}}
				>
					<CommandForm formData={formData} />
					<Suspense
						fallback={
							<div className="overflow-y-auto overflow-x-hidden fixed top-0 right-0 left-0 z-50 ">
								<div className="relative p-4 w-full max-w-md max-h-full">
									<div className="relative bg-white rounded-lg shadow dark:bg-gray-700">
										<div className="p-4 md:p-5 flex flex-col justify-center items-center">
											<h3 className="mb-5 text-lg font-normal text-gray-500 dark:text-gray-400">
												Now Execution
											</h3>
											<div className="block animate-spin h-10 w-10 border-4 border-blue-500 rounded-full border-t-transparent" />
										</div>
									</div>
								</div>
							</div>
						}
					>
						<Footer formData={formData} />
					</Suspense>
				</form>
			</div>
		</>
	);
}
