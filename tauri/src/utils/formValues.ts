export type FormValues = { [k: string]: FormDataEntryValue };

export type CollectedFormValues = {
	values: FormValues;
	validationError: boolean;
};

// id指定の<form>からFormData経由で値を収集する。validate=trueならreportValidity()で必須検証を行う
export function collectFormValues(
	formId: string,
	validate: boolean,
): CollectedFormValues {
	const formElement = document.querySelector(`#${formId}`) as HTMLFormElement;
	if (validate && !formElement.reportValidity()) {
		return { values: {}, validationError: true };
	}
	return {
		values: Object.fromEntries(new FormData(formElement).entries()),
		validationError: false,
	};
}
