import { createContext, useContext, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";
import { getCurrentUser } from "../api/user.api";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  // Получаем текущего пользователя при загрузке приложения
  useEffect(() => {
    getCurrentUser()
      .then(res => setUser(res.data))
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  // Логин: подтягиваем пользователя с сервера или используем данные
  const login = async (userData) => {
    if (userData) {
      setUser(userData);
    } else {
      const res = await getCurrentUser();
      setUser(res.data);
    }
    navigate("/products", { replace: true });
  };

  // Logout: POST /logout и очистка контекста
  const logout = async () => {
    try {
      await api.post("/logout"); // сервер удаляет JWT cookie
    } catch {
      // игнорируем ошибки, всё равно делаем logout на клиенте
    } finally {
      setUser(null);       // очищаем контекст
      navigate("/login", { replace: true });
    }
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
