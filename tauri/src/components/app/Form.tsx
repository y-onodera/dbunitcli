import { ReactNode } from "react";

const formid="commandForm"
export function formData(){
    const formData = new FormData(document.querySelector('#'+formid) as HTMLFormElement);
    return Object.fromEntries(formData.entries());
}
export default function Form(props:{children:ReactNode}){
  return (
    <>
      <div className="p-4 rounded-lg mt-14">
        <form id={formid} className="grid gap-6 mb-6 grid-cols-1"
          onSubmit={(e) => {
            e.preventDefault();
          }}
        >
          {props.children}
        </form>
      </div>
    </>
  );
}  