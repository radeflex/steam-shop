import api from "./axios";

export const getUsers = (filter, pageable) =>
  api.get("/users", {
    params: {
      ...filter,
      ...pageable
    }
  });

export const getCurrentUser = () => api.get("/users/current");

export const updateCurrentUser = (formData) => {
  return api.put("/users/current", formData);
};

export const deleteAvatar = () =>
  api.delete("/users/current/avatar");

export const getPurchaseHistory = pageable =>
  api.get("/users/current/product-history", { params: pageable });
