import api from "./contactApi";

export const login = (credentials) => api.post("/auth/login", credentials);
export const getSession = (config) => api.get("/auth/session", config);
export const getProfile = () => api.get("/users/me");
export const logout = () => api.post("/auth/logout");
export const changePassword = (passwords) => api.put("/users/me/password", passwords);
