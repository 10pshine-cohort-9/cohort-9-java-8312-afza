import axios from "axios";

const baseURL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

const contactApi = axios.create({
    baseURL,
    withCredentials: true,
    withXSRFToken: true,
    xsrfCookieName: "XSRF-TOKEN",
    xsrfHeaderName: "X-XSRF-TOKEN",
    headers: {
        "Content-Type": "application/json",
    },
});

let unauthorizedHandler;

export const setUnauthorizedHandler = (handler) => {
    unauthorizedHandler = handler;
};

contactApi.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401 && !error.config?.suppressUnauthorizedHandler) {
            unauthorizedHandler?.();
        }
        return Promise.reject(error);
    },
);

export default contactApi;
