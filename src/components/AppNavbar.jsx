import { useEffect, useRef, useState } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCartShopping, faBell } from "@fortawesome/free-solid-svg-icons";
import { useCart } from "../context/CartContext";
import { useAuth } from "../context/AuthContext";
import {
  getUnreadNotifications,
  getNotifications,
  markNotificationAsRead,
} from "../api/notifications.api";

export default function AppNavbar() {
  const { user, logout, loading } = useAuth();
  const { cartCount } = useCart();
  const navigate = useNavigate();

  const [notifications, setNotifications] = useState([]); // непрочитанные
  const [allNotifications, setAllNotifications] = useState(null); // все уведомления
  const [showNotifications, setShowNotifications] = useState(false);
  const bellRef = useRef(null);

  const markAsRead = async (id) => {
    try {
      await markNotificationAsRead(id);
      setNotifications(prev => prev.filter(n => n.id !== id));

      if (allNotifications) {
        setAllNotifications(prev => prev.map(n => n.id === id ? { ...n, read: true } : n));
      }
    } catch (e) {
      console.error("Failed to mark notification as read", e);
    }
  };

  // Подгружаем непрочитанные уведомления
  useEffect(() => {
    if (!user) return;

    getUnreadNotifications({ page: 0, size: 20 })
      .then(res => setNotifications(res.data.content || []))
      .catch(() => setNotifications([]));
  }, [user]);

  // Закрытие уведомлений по клику вне
  useEffect(() => {
    const handler = e => {
      if (bellRef.current && !bellRef.current.contains(e.target)) {
        setShowNotifications(false);
      }
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, []);

  const navLinkClass = ({ isActive }) =>
    `nav-link px-2 ${isActive ? "active-link" : "text-light"}`;

  if (loading) return null;

  const goToAdmin = () => navigate("/admin/products");

  const showAllNotificationsHandler = async () => {
    try {
      const res = await getNotifications({ page: 0, size: 50 });
      const fetched = res.data.content || [];

      // объединяем с текущими уведомлениями, фильтруем дубликаты
      const merged = [
        ...notifications,
        ...fetched.filter(n => !notifications.some(un => un.id === n.id))
      ];

      setAllNotifications(merged);
    } catch (err) {
      console.error("Failed to load all notifications", err);
    }
  };

  const displayNotifications = allNotifications || notifications;

  // Кнопка Show all видна, если ещё не подгружены все уведомления
  const showShowAllButton = !allNotifications;

  return (
    <nav className="navbar navbar-dark bg-dark px-3 d-flex justify-content-between">
      <Link className="navbar-brand" to="/products">Steam Shop</Link>

      <div className="d-flex align-items-center gap-3">
        <NavLink to="/products" className={navLinkClass}>Products</NavLink>
      </div>

      <div className="d-flex align-items-center gap-2">
        {user ? (
          <>
            {user.role === "ADMIN" && (
              <button className="btn btn-warning me-2" onClick={goToAdmin}>
                Admin Panel
              </button>
            )}

            {/* Notifications */}
            <div className="position-relative me-2" ref={bellRef}>
              <button
                className="btn btn-link text-decoration-none position-relative"
                onClick={() => {
                  setShowNotifications(v => !v);
                  // при повторном открытии сбрасываем allNotifications
                  if (!showNotifications) {
                    setAllNotifications(null);
                  }
                }}
              >
                <FontAwesomeIcon icon={faBell} fontSize={25} />
                {notifications.length > 0 && (
                  <span className="position-absolute top-0 start-120 translate-middle badge rounded-pill bg-danger" style={{ fontSize: "0.6rem" }}>
                    {notifications.length}
                  </span>
                )}
              </button>


              {showNotifications && (
                <div
                  className="position-absolute end-0 mt-2 bg-dark text-light rounded shadow"
                  style={{ width: 320, maxHeight: 300, overflowY: "auto", zIndex: 200 }}
                >
                  {displayNotifications.length === 0 && (
                    <div className="p-3 text-center text-muted">No notifications</div>
                  )}

                  {displayNotifications.map(n => {
                  const isUnread = notifications.some(un => un.id === n.id);

                  return <div
                      key={n.id}
                      className="p-3 border-bottom border-secondary d-flex justify-content-between align-items-start gap-2"
                    >
                      <div>
                        <div className={`fw-bold text-${n.type.toLowerCase()}`}>
                          {n.title}
                        </div>
                        {n.text && <div className="small text-muted mt-1">{n.text}</div>}
                      </div>

                      {/* Показываем кнопку только для непрочитанных */}
                      {isUnread && (
                        <button
                          className="btn btn-sm btn-outline-success"
                          title="Mark as read"
                          onClick={() => markAsRead(n.id)}
                        >
                          ✓
                        </button>
                      )}
                    </div>
                  })}
                  {showShowAllButton && (
                    <div className="p-2 border-top border-secondary text-center">
                      <button
                        className="btn btn-sm btn-outline-light w-100"
                        onClick={showAllNotificationsHandler}
                      >
                        Show all
                      </button>
                    </div>
                  )}
                </div>

              )}
            </div>

            {/* Cart */}
            <NavLink to="/cart" className={navLinkClass}>
              <FontAwesomeIcon icon={faCartShopping} fontSize={25} />
              {cartCount > 0 && (
                <span className="position-absolute top-20 start-200 translate-middle badge rounded-pill bg-danger" style={{ fontSize: "0.6rem" }}>
                  {cartCount}
                </span>
              )}
            </NavLink>

            {/* User info */}
            <NavLink to="/profile" className={navLinkClass}>
              <div className="d-flex flex-column align-items-start me-2">
                <span>{user.username}</span>
                <small className="text-warning">{user.balance} ₽ | {user.points} pts</small>
              </div>
            </NavLink>

            {/* Logout */}
            <button className="btn btn-danger ms-2" onClick={logout}>Logout</button>
          </>
        ) : (
          <>
            <Link className="btn btn-success me-2" to="/login">Sign in</Link>
            <Link className="btn btn-primary" to="/register">Sign up</Link>
          </>
        )}
      </div>
    </nav>
  );
}
