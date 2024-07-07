/** @type {import('tailwindcss').Config} */
export default {
  content: ["./src/**/*.tsx","./src/*-src.css"],
  theme: {
    extend: {},
  },
  plugins: [require("@tailwindcss/forms")],
}

