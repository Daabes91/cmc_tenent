import defaultTheme from "tailwindcss/defaultTheme";
import forms from "@tailwindcss/forms";

/** @type {import('tailwindcss').Config} */
export default {
  darkMode: "class",
  content: [
    "./components/**/*.{vue,js,ts}",
    "./layouts/**/*.vue",
    "./pages/**/*.{vue,js,ts}",
    "./composables/**/*.{js,ts}",
    "./plugins/**/*.{js,ts}",
    "./app.vue",
    "./error.vue"
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ["Inter", ...defaultTheme.fontFamily.sans]
      },
      boxShadow: {
        card: "0 4px 24px -8px rgba(15, 23, 42, 0.12)"
      },
      borderRadius: {
        "4xl": "2rem"
      },
      animation: {
        shake: "shake 0.4s ease-in-out"
      },
      keyframes: {
        shake: {
          "0%, 100%": { transform: "translateX(0)" },
          "25%": { transform: "translateX(-5px)" },
          "75%": { transform: "translateX(5px)" }
        }
      },
      colors: {
        violet: {
          50: "#e6fbfa",
          100: "#c5f4f1",
          200: "#9ce8e4",
          300: "#6cd7d4",
          400: "#3dc3be",
          500: "#13b0a9",
          600: "#109189",
          700: "#0d7b72",
          800: "#0d7377",
          900: "#0a5755",
          950: "#063d3b"
        }
      }
    }
  },
  plugins: [forms]
};
