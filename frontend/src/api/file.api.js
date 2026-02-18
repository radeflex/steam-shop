import api from "./axios"

export const getImageUrl = uuid => `${api.defaults.baseURL}/files/${uuid}`;
