import defaultColors from "tailwindcss/colors";
import tailwindConfig from "../../tailwind.config.mjs";

const mergedColors = {
  ...defaultColors,
  ...(tailwindConfig?.theme?.colors ?? {}),
  ...(tailwindConfig?.theme?.extend?.colors ?? {})
};

export default mergedColors;
