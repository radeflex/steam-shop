import api from "./axios";

export const getProducts = (filter, pageable) =>
  api.get("/products", {
    params: { ...filter, ...pageable }
  });

export const getProduct = id =>
  api.get(`/products/${id}`);

export const createProduct = (data, image) => {
  const form = new FormData();
  form.append(
    "data",
    new Blob([JSON.stringify(data)], { type: "application/json" })
  );
  if (image) form.append("image", image);
  return api.post("/products", form);
};

export const updateProduct = (id, data, image) => {
  const form = new FormData();
  form.append(
    "data",
    new Blob([JSON.stringify(data)], { type: "application/json" })
  );
  if (image) form.append("image", image);
  return api.put(`/products/${id}`, form);
};

export const deleteProduct = id =>
  api.delete(`/products/${id}`);

export const purchaseProducts = orders =>
  api.post("/products/purchase", orders);
