// sidecar を Linux 上で動かす前提の @tauri-apps/api/path の代替
export const sep = () => "/";
export const isAbsolute = async (path: string) => path.startsWith("/");
