import { Outlet } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import ForbiddenPage from "../../pages/status/ForbiddenPage";

export default function AdminRoute() {
  const { user, loading } = useAuth();

  if (loading) return null; // ждем загрузки пользователя

  // Если пользователь не авторизован или не имеет роли ADMIN — рендерим 403
  if (!user || user.role !== "ADMIN") {
    return <ForbiddenPage />;
  }

  // Если всё ок — рендерим дочерние маршруты
  return <Outlet />;
}
