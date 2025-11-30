import type { Config } from "tailwindcss"

const config = {
  darkMode: ["class"],
  content: [
    "./pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./components/**/*.{js,ts,jsx,tsx,mdx}",
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
    "*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    container: {
      center: true,
      padding: "2rem",
      screens: {
        "2xl": "1400px",
      },
    },
    extend: {
      colors: {
        border: "hsl(var(--border))",
        input: "hsl(var(--input))",
        ring: "hsl(var(--ring))",
        background: "hsl(var(--background))",
        foreground: "hsl(var(--foreground))",
        mintlify: {
          mint: "#00A43C",
          mintDark: "#00A43C",
          blue: "#00A43C",
          navy: "#00A43C",
          midnight: "#00A43C",
          offwhite: "#00A43C",
        },
        primary: {
          DEFAULT: "hsl(var(--primary))",
          foreground: "hsl(var(--primary-foreground))",
        },
        secondary: {
          DEFAULT: "hsl(var(--secondary))",
          foreground: "hsl(var(--secondary-foreground))",
        },
        destructive: {
          DEFAULT: "hsl(var(--destructive))",
          foreground: "hsl(var(--destructive-foreground))",
        },
        muted: {
          DEFAULT: "hsl(var(--muted))",
          foreground: "hsl(var(--muted-foreground))",
        },
        accent: {
          DEFAULT: "hsl(var(--accent))",
          foreground: "hsl(var(--accent-foreground))",
        },
        popover: {
          DEFAULT: "hsl(var(--popover))",
          foreground: "hsl(var(--popover-foreground))",
        },
        card: {
          DEFAULT: "hsl(var(--card))",
          foreground: "hsl(var(--card-foreground))",
        },
      },
      borderRadius: {
        lg: "var(--radius)",
        md: "calc(var(--radius) - 2px)",
        sm: "calc(var(--radius) - 4px)",
      },
      keyframes: {
        "accordion-down": {
          from: { height: "0" },
          to: { height: "var(--radix-accordion-content-height)" },
        },
        "accordion-up": {
          from: { height: "var(--radix-accordion-content-height)" },
          to: { height: "0" },
        },
        "slide-right": {
          "0%": { transform: "translateX(-100px)", opacity: "0" },
          "100%": { transform: "translateX(0)", opacity: "1" },
        },
        "expand": {
          "0%": { transform: "scale(0.8)", opacity: "0" },
          "100%": { transform: "scale(1)", opacity: "1" },
        },
        "marquee": {
          "0%": { transform: "translateX(0)" },
          "100%": { transform: "translateX(-33.333%)" },
        },
        "wave-slow": {
          "0%, 100%": { transform: "translateY(0)" },
          "50%": { transform: "translateY(-20px)" },
        },
        "wave-medium": {
          "0%, 100%": { transform: "translateY(0)" },
          "50%": { transform: "translateY(-15px)" },
        },
        "wave-fast": {
          "0%, 100%": { transform: "translateY(0)" },
          "50%": { transform: "translateY(-10px)" },
        },
        "float-slow": {
          "0%, 100%": { transform: "translate(0, 0)" },
          "50%": { transform: "translate(20px, -20px)" },
        },
        "float-medium": {
          "0%, 100%": { transform: "translate(0, 0)" },
          "50%": { transform: "translate(-15px, 15px)" },
        },
        "float-fast": {
          "0%, 100%": { transform: "translate(0, 0)" },
          "50%": { transform: "translate(10px, -10px)" },
        },
      },
      animation: {
        "accordion-down": "accordion-down 0.2s ease-out",
        "accordion-up": "accordion-up 0.2s ease-out",
        "slide-right": "slide-right 0.9s ease-out",
        "expand": "expand 0.9s ease-out",
        "marquee": "marquee 30s linear infinite",
        "wave-slow": "wave-slow 8s ease-in-out infinite",
        "wave-medium": "wave-medium 6s ease-in-out infinite",
        "wave-fast": "wave-fast 4s ease-in-out infinite",
        "float-slow": "float-slow 10s ease-in-out infinite",
        "float-medium": "float-medium 8s ease-in-out infinite",
        "float-fast": "float-fast 6s ease-in-out infinite",
      },
      animationDelay: {
        "0": "0ms",
        "400": "400ms",
        "800": "800ms",
        "1000": "1000ms",
        "1200": "1200ms",
        "1500": "1500ms",
      },
      transitionDuration: {
        "900": "900ms",
      },
      backgroundImage: {
        "mintlify-gradient":
          "linear-gradient(102deg, #00a43c 0%, #00a43c 51%, #00a43c 100%)",
        "mintlify-radial":
          "radial-gradient(circle at 20% 20%, rgba(0, 164, 60, 0.3), transparent 55%), radial-gradient(circle at 80% 10%, rgba(0, 164, 60, 0.2), transparent 45%)",
      },
    },
  },
  plugins: [
    require("tailwindcss-animate"),
    // Plugin for animation delay utilities
    function ({ matchUtilities, theme }: any) {
      matchUtilities(
        {
          "animation-delay": (value: string) => ({
            "animation-delay": value,
          }),
        },
        { values: theme("animationDelay") }
      )
    },
  ],
} satisfies Config

export default config
