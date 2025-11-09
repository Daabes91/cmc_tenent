import defaultTheme from "tailwindcss/defaultTheme";
import forms from "@tailwindcss/forms";
import type { Config } from 'tailwindcss'

export default <Partial<Config>>{
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
        // Blue primary color palette (similar to admin-nuxt's violet)
        blue: {
          50: "#eff6ff",
          100: "#dbeafe",
          200: "#bfdbfe",
          300: "#93c5fd",
          400: "#60a5fa",
          500: "#3b82f6",
          600: "#2563eb",
          700: "#1d4ed8",
          800: "#1e40af",
          900: "#1e3a8a",
          950: "#172554"
        }
      },
      backgroundImage: {
        'primary-gradient': 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)',
      }
    }
  },
  plugins: [
    forms,
    // Generate outline-primary utility for Nuxt UI
    function ({ addUtilities, theme }: any) {
      const primaryColors = theme('colors.blue')
      const utilities: any = {}

      Object.keys(primaryColors).forEach((key) => {
        utilities[`.outline-primary-${key}`] = {
          outlineColor: primaryColors[key]
        }
      })

      // Add base outline-primary (uses 500 by default)
      utilities['.outline-primary'] = {
        outlineColor: primaryColors['500']
      }

      addUtilities(utilities)
    }
  ]
};
