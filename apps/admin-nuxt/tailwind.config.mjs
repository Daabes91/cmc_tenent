import defaultTheme from "tailwindcss/defaultTheme";
import forms from "@tailwindcss/forms";

const brandMint = {
  50: "#e6f6ed",
  100: "#c4edd6",
  200: "#96deb7",
  300: "#63cc93",
  400: "#34b16d",
  500: "#00a43c",
  600: "#008b35",
  700: "#006f2c",
  800: "#005223",
  900: "#003b1a",
  950: "#01250f"
};

const evergreen = {
  50: "#e7f0ea",
  100: "#c6dfcf",
  200: "#9ac2a8",
  300: "#6ca382",
  400: "#4a8765",
  500: "#326b4c",
  600: "#25533c",
  700: "#1b3e2d",
  800: "#132a20",
  900: "#0a1813",
  950: "#040c08"
};

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
        mint: brandMint,
        evergreen,
        violet: brandMint
      }
    }
  },
  plugins: [forms]
};
