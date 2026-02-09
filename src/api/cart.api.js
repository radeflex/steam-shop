import api from "./axios";

export const getCart = pageable =>
  api.get("/cart", { params: pageable });

export const addToCart = productId =>
  api.post(`/cart/${productId}`);

export const updateQuantity = (id, quantity) =>
  api.put(`/cart/${id}/quantity/${quantity}`);

export const removeFromCart = id =>
  api.delete(`/cart/${id}`);

export const purchase = (data) =>
  api.post("/purchase", data);

export const purchaseViaBalance = (data) =>
  api.post("/purchase-balance", data);
