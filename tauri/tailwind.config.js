/** @type {import('tailwindcss').Config} */
import forms from "@tailwindcss/forms";

export default {
  content: ["./src/**/*.tsx","./src/*-src.css"],
  theme: {
    extend: {},
  },
  plugins: [forms],
};