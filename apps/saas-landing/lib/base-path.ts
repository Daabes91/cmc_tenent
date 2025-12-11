const rawBasePath = process.env.NEXT_PUBLIC_BASE_PATH ?? "/landing";
const normalizedBasePath =
  !rawBasePath || rawBasePath === "/" ? "" : rawBasePath.replace(/\/$/, "");

/**
 * Prefixes asset and navigation paths with the configured base path.
 * External URLs are returned untouched.
 */
export function withBasePath(path?: string) {
  if (!path) return path ?? "";
  if (/^https?:\/\//.test(path)) return path;

  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  if (normalizedBasePath && normalizedPath.startsWith(`${normalizedBasePath}/`)) {
    return normalizedPath;
  }
  return `${normalizedBasePath}${normalizedPath}`;
}
