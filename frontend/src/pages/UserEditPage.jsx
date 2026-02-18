import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getCurrentUser, updateCurrentUser } from "../api/user.api";

export default function UserEditPage() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
  });
  const [changes, setChanges] = useState({});
  const [avatarFile, setAvatarFile] = useState(null);
  const [errors, setErrors] = useState({});

  useEffect(() => {
    getCurrentUser().then((r) => {
      setUser(r.data);
      setFormData({
        username: r.data.username || "",
        email: r.data.email || "",
        password: "",
      });
    });
  }, []);

  if (!user) return <p className="mt-3">Loading user...</p>;

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));

    // Добавляем только если значение отличается от исходного или это пароль
    if ((name === "password" && value) || value !== user[name]) {
      setChanges((prev) => ({ ...prev, [name]: value }));
    } else {
      setChanges((prev) => {
        const copy = { ...prev };
        delete copy[name];
        return copy;
      });
    }

    // При изменении поля очищаем ошибку
    setErrors((prev) => {
      const copy = { ...prev };
      delete copy[name];
      return copy;
    });
  };

  const handleFileChange = (e) => {
    setAvatarFile(e.target.files[0]);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    // Если нет изменений и аватара — просто идём на профиль
    if (Object.keys(changes).length === 0 && !avatarFile) {
      navigate("/user");
      return;
    }

    const form = new FormData();
    const blob = new Blob([JSON.stringify(changes)], { type: "application/json" });
    form.append("data", blob);

    if (avatarFile) form.append("image", avatarFile);

    try {
      await updateCurrentUser(form);
      navigate("/profile");
    } catch (err) {
      console.error(err);

      // Если бэкенд вернул объект ошибок по полям
      if (err.response?.data?.errors) {
        setErrors(err.response.data.errors);
      } else if (err.response?.status === 409) {
        setErrors({
          username: "Пользователь с таким именем уже существует",
          email: "Пользователь с таким email уже существует",
        });
      } else {
        setErrors({ general: "Ошибка при обновлении профиля" });
      }
    }
  };

  const handleDiscard = () => {
    navigate("/profile");
  };

  return (
    <div className="container mt-4">
      <h2 className="mb-4">Edit Profile</h2>

      <div className="card mb-4" style={{ maxWidth: "500px" }}>
        <div className="card-body">
          {errors.general && (
            <div className="alert alert-danger">{errors.general}</div>
          )}

          <form onSubmit={handleSubmit}>
            {/* Username */}
            <div className="mb-3">
              <label className="form-label">Username</label>
              <input
                type="text"
                className={`form-control ${errors.username ? "is-invalid" : ""}`}
                name="username"
                value={formData.username}
                onChange={handleChange}
                required
                minLength={3}
                maxLength={32}
              />
              {errors.username && (
                <div className="invalid-feedback">{errors.username}</div>
              )}
            </div>

            {/* Email */}
            <div className="mb-3">
              <label className="form-label">Email</label>
              <input
                type="email"
                className={`form-control ${errors.email ? "is-invalid" : ""}`}
                name="email"
                value={formData.email}
                onChange={handleChange}
                required
              />
              {errors.email && (
                <div className="invalid-feedback">{errors.email}</div>
              )}
            </div>

            {/* Password */}
            <div className="mb-3">
              <label className="form-label">Password</label>
              <input
                type="password"
                className={`form-control ${errors.password ? "is-invalid" : ""}`}
                name="password"
                value={formData.password}
                onChange={handleChange}
                minLength={8}
              />
              <small className="form-text text-muted">
                Leave blank to keep current password
              </small>
              {errors.password && (
                <div className="invalid-feedback">{errors.password}</div>
              )}
            </div>

            {/* Avatar */}
            <div className="mb-3">
              <label className="form-label">Avatar</label>
              <input
                type="file"
                className={`form-control ${avatarFile ? "border-warning" : ""}`}
                accept="image/*"
                onChange={handleFileChange}
              />
            </div>

            {/* Buttons */}
            <div className="d-flex gap-2">
              <button type="submit" className="btn btn-primary">
                Save Changes
              </button>
              <button type="button" className="btn btn-secondary" onClick={handleDiscard}>
                Discard
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
