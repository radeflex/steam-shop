import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { toast } from "react-toastify";

import {
  getCurrentUser,
  getPurchaseHistory,
  topUpBalance,
  deleteAvatar
} from "../api/user.api";

import { getImageUrl } from "../api/file.api";
import api from "../api/axios";

export default function CurrentUserPage() {
  const [user, setUser] = useState(null);
  const [history, setHistory] = useState(null);

  const [sending, setSending] = useState(false);

  // пополнение
  const [showTopUp, setShowTopUp] = useState(false);
  const [topUpAmount, setTopUpAmount] = useState("");
  const [topUpLoading, setTopUpLoading] = useState(false);

  // удаление аватара
  const [avatarDeleting, setAvatarDeleting] = useState(false);

  useEffect(() => {
    getCurrentUser().then(r => setUser(r.data));
    getPurchaseHistory({ page: 0, size: 10 }).then(r => setHistory(r.data));
  }, []);

  const sendConfirmationEmail = async () => {
    if (sending) return;

    try {
      setSending(true);
      await api.post("/send-email-confirmation");

      toast.info("📩 Ссылка для подтверждения email отправлена на вашу почту", {
        autoClose: 3000,
      });
    } catch (err) {
      toast.error("❌ Не удалось отправить письмо. Попробуйте позже.");
      console.error(err);
    } finally {
      setSending(false);
    }
  };

  const handleTopUp = async () => {
    if (topUpLoading) return;

    const amount = parseInt(topUpAmount, 10);

    if (!amount || amount <= 0) {
      toast.warn("Введите сумму больше 0");
      return;
    }

    if (amount > 100000) {
      toast.warn("Слишком большая сумма 😅");
      return;
    }

    try {
      setTopUpLoading(true);

      const r = await topUpBalance(amount);
      const url = r.data?.url;

      if (!url) {
        toast.error("❌ Сервер не вернул ссылку на оплату");
        return;
      }

      toast.info("⏳ Перенаправляем на оплату...", { autoClose: 1500 });
      window.location.href = url;

    } catch (err) {
      const status = err?.response?.status;
      const data = err?.response?.data;

      if (status === 400 && data?.errors) {
        const errors = Array.isArray(data.errors)
            ? data.errors
            : Object.values(data.errors).flat();

        if (errors.length > 0) {
          errors.forEach(e => toast.error(`❌ ${e}`));
        } else {
          toast.error("❌ Ошибка валидации");
        }

        return;
      }

      toast.error("❌ Не удалось создать платеж. Попробуйте позже.");
      console.error(err);

    } finally {
      setTopUpLoading(false);
    }
  };

  const handleDeleteAvatar = async () => {
    if (avatarDeleting) return;

    const confirmDelete = window.confirm("Удалить аватар?");
    if (!confirmDelete) return;

    try {
      setAvatarDeleting(true);

      await deleteAvatar();

      setUser(prev => ({
        ...prev,
        avatarUrl: "no-image"
      }));

      toast.success("🗑 Аватар удалён");

    } catch (err) {
      toast.error("❌ Не удалось удалить аватар");
      console.error(err);
    } finally {
      setAvatarDeleting(false);
    }
  };

  if (!user) return <p className="mt-3">Loading user...</p>;

  return (
      <div className="container mt-4">
        <h2 className="mb-4">Profile</h2>

        {/* User info */}
        <div className="card mb-3" style={{ maxWidth: "600px" }}>
          <div className="card-body d-flex align-items-start">

            <div className="d-flex flex-column align-items-center me-3">
                <img
                    src={getImageUrl(user.avatarUrl)}
                    alt={`${user.username}'s avatar`}
                    style={{
                      width: "120px",
                      height: "120px",
                      borderRadius: "50%",
                      objectFit: "cover"
                    }}
                />
                {user.avatarUrl !== 'no-image' &&
                    <button
                        className="btn btn-sm btn-outline-danger mt-2"
                        onClick={handleDeleteAvatar}
                        disabled={avatarDeleting}
                    >
                      {avatarDeleting ? "Удаление..." : "Удалить аватар"}
                    </button>}
            </div>

            <div className="flex-grow-1">
              <p><strong>ID:</strong> {user.id}</p>
              <p><strong>Username:</strong> {user.username}</p>

              <p>
                <strong>Email:</strong> {user.email}{" "}
                {user.confirmed ? (
                    <span className="badge bg-success ms-2">✔ подтверждён</span>
                ) : (
                    <button
                        className="btn btn-sm btn-outline-warning mb-2"
                        onClick={sendConfirmationEmail}
                        disabled={sending}
                    >
                      {sending ? "Отправка..." : "Подтвердить"}
                    </button>
                )}
              </p>

              <p className="mb-2">
                <strong>Balance:</strong> {user.balance} ₽{" "}
                <button
                    className="btn btn-sm btn-outline-success ms-2"
                    onClick={() => setShowTopUp(v => !v)}
                >
                  💳 Пополнить
                </button>
              </p>

              {showTopUp && (
                  <div className="border rounded p-2 mb-2" style={{ maxWidth: "280px" }}>
                    <label className="form-label mb-1">
                      Сумма пополнения (₽)
                    </label>

                    <input
                        type="number"
                        className="form-control form-control-sm"
                        value={topUpAmount}
                        min={1}
                        placeholder="Минимум 10₽"
                        onChange={(e) => setTopUpAmount(e.target.value)}
                        disabled={topUpLoading}
                    />

                    <div className="d-flex gap-2 mt-2">
                      <button
                          className="btn btn-sm btn-success"
                          onClick={handleTopUp}
                          disabled={topUpLoading}
                      >
                        {topUpLoading ? "Создание..." : "Перейти к оплате"}
                      </button>

                      <button
                          className="btn btn-sm btn-outline-secondary"
                          onClick={() => {
                            setShowTopUp(false);
                            setTopUpAmount("");
                          }}
                          disabled={topUpLoading}
                      >
                        Отмена
                      </button>
                    </div>
                  </div>
              )}

              <p><strong>Points:</strong> {user.points}</p>
              <p>
                <strong>Joined at:</strong>{" "}
                {new Date(user.createdAt).toLocaleString()}
              </p>

              <Link to="/profile/edit" className="btn btn-primary mt-2">
                Edit Profile
              </Link>
            </div>
          </div>
        </div>

        {/* Purchase history */}
        <h4>Purchase History</h4>

        {!history ? (
            <p>Loading purchase history...</p>
        ) : history.content.length === 0 ? (
            <p>Empty.</p>
        ) : (
            <div className="table-responsive" style={{ maxWidth: "800px" }}>
              <table className="table table-striped">
                <thead>
                <tr>
                  <th>Product ID</th>
                  <th>Title</th>
                  <th>Quantity</th>
                  <th>Price per unit</th>
                  <th>Total</th>
                </tr>
                </thead>
                <tbody>
                {history.content.map(p => (
                    <tr key={p.id}>
                      <td>{p.id}</td>
                      <td>{p.title}</td>
                      <td>{p.quantity}</td>
                      <td>{p.price} ₽</td>
                      <td>{p.price * p.quantity} ₽</td>
                    </tr>
                ))}
                </tbody>
              </table>
            </div>
        )}
      </div>
  );
}