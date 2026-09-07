import registrationApi from "./contactApi";

export const registerUser = (registration) =>
  registrationApi.post("/users/register", registration);
