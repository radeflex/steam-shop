import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";
import { toast } from "react-toastify";

export default function RegisterPage() {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errors, setErrors] = useState({});

  const navigate = useNavigate();

  const submit = async (e) => {
    e.preventDefault();
    setErrors({});

    try {
      await api.post("/register", { username, email, password });

      toast.info("📩 Ссылка для подтверждения email отправлена на вашу почту", {
        autoClose: 3000,
      });

      setTimeout(() => {
        navigate("/products");
      }, 3100);
    } catch (err) {
      console.error(err);

      if (err.response?.data?.errors) {
        setErrors(err.response.data.errors);
      } else {
        setErrors({ general: "Ошибка при регистрации" });
      }
    }
  };

  return (
    <div className="container mt-5">
      <div className="row justify-content-center">
        <div className="col-md-5">
          <div className="card shadow-sm">
            <div className="card-body">
              <h3 className="text-center mb-4">Register</h3>

              {errors.general && (
                <div className="alert alert-danger">{errors.general}</div>
              )}

              <form onSubmit={submit}>
                <div className="mb-3">
                  <label className="form-label">Username</label>
                  <input
                    type="text"
                    className={`form-control ${errors.username ? "is-invalid" : ""}`}
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                    minLength={3}
                    maxLength={32}
                  />
                  {errors.username && (
                    <div className="invalid-feedback">{errors.username}</div>
                  )}
                </div>

                <div className="mb-3">
                  <label className="form-label">Email</label>
                  <input
                    type="email"
                    className={`form-control ${errors.email ? "is-invalid" : ""}`}
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                  {errors.email && (
                    <div className="invalid-feedback">{errors.email}</div>
                  )}
                </div>

                <div className="mb-3">
                  <label className="form-label">Password</label>
                  <input
                    type="password"
                    className={`form-control ${errors.password ? "is-invalid" : ""}`}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    minLength={8}
                  />
                  {errors.password && (
                    <div className="invalid-feedback">{errors.password}</div>
                  )}
                </div>

                <button type="submit" className="btn btn-primary w-100">
                  Sign Up
                </button>
              </form>

            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
