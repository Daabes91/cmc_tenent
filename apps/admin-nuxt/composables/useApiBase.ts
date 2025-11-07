export function useApiBase() {
  const config = useRuntimeConfig();
  return process.server
    ? (config.apiBase ?? config.public.apiBase)
    : config.public.apiBase;
}
