import Form from "./app/Form";
import CommandForm from "./app/CommandForm";
import Footer from "./app/Footer";
import { useSelectParameter } from "../context/SelectParameterProvider";
import "../App.css";

export default function App() {
  const parameter = useSelectParameter();
  return (
    <Form>
      <CommandForm {...parameter} />
      <Footer {...parameter} />
    </Form>
  );
}