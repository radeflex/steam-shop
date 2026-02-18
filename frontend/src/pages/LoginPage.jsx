import { useState } from "react";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";

export default function LoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate("");
  const [error, setError] = useState(null);
  const { login } = useAuth();

  const submit = async (e) => {
    e.preventDefault();
    setError(null);

    try {
      // логинимся, сервер установит куку stsh_jwt
      await api.post("/login", { username, password });

      // подтягиваем пользователя с сервера через куку
      const res = await api.get("/users/current"); 
      login(res.data); // передаём объект пользователя в контекст
      navigate("/products");
    } catch (err) {
      if (err.response?.status === 403) {
        setError("Неверный логин или пароль");
      } else {
        setError("Ошибка сервера. Попробуйте позже.");
        console.error(err);
      }
    }
  };

  return (
    <div className="container mt-5">
      <div className="row justify-content-center">
        <div className="col-md-4">
          <div className="card shadow-sm">
            <div className="card-body">
              <h3 className="text-center mb-4">Login</h3>

              {/* Alert */}
              {error && (
                <div className="alert alert-danger text-center">
                  {error}
                </div>
              )}

              <form onSubmit={submit}>
                {/* Username */}
                <div className="mb-3">
                  <label className="form-label">Username</label>
                  <input
                    type="text"
                    className="form-control"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    placeholder="Enter username"
                    required
                  />
                </div>

                {/* Password */}
                <div className="mb-3">
                  <label className="form-label">Password</label>
                  <input
                    type="password"
                    className="form-control"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Enter password"
                    required
                  />
                </div>

                <button type="submit" className="btn btn-primary w-100">
                  Sign In
                </button>
              </form>

            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
