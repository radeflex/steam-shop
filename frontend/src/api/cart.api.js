import api from "./axios";

export const getCart = pageable =>
  api.get("/cart", { params: pageable });

export const addToCart = productId =>
  api.post(`/cart/${productId}`);

export const updateQuantity = (id, quantity) =>
  api.put(`/cart/${id}/quantity/${quantity}`);

export const removeFromCart = id =>
  api.delete(`/cart/${id}`);

export const purchaseViaCard = (id) =>
  api.post(`/purchase-card/${id}`);

export const purchaseViaBalance = (id) =>
  api.post(`/purchase-balance/${id}`);

export const purchaseCartViaCard = () =>
  api.post(`/purchase-card`);

export const purchaseCartViaBalance = () =>
  api.post(`/purchase-balance`);