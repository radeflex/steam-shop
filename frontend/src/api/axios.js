// api/axios.js
import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8443",
  withCredentials: true
});

export default api;
