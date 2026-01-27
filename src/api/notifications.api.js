// src/api/notifications.api.js
import api from "./axios";

const pageableParams = ({ page = 0, size = 20, sort = [] } = {}) => ({
  "pageable.page": page,
  "pageable.size": size,
  "pageable.sort": sort,
});

/* ---------- GET ---------- */

export const getAdminNotifications = (pageable) =>
  api.get("/notifications/admin", {
    params: pageableParams(pageable),
  });

export const getNotifications = (pageable) =>
  api.get("/notifications", {
    params: pageableParams(pageable),
  });

export const getUnreadNotifications = (pageable) =>
  api.get("/notifications/unread", {
    params: pageableParams(pageable),
  });

/* ---------- PUT ---------- */

export const markNotificationAsRead = (id) =>
  api.put(`/notifications/${id}/read`);

/* ---------- POST ---------- */

export const sendNotificationToAll = (data) =>
  api.post("/notifications", data);

export const sendNotificationToUser = (userId, data) =>
  api.post(`/notifications/${userId}`, data);

/* ---------- DELETE ---------- */

export const deleteNotification = (id) =>
  api.delete(`/notifications/${id}`);