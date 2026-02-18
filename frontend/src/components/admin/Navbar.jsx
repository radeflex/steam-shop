import { useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

export default function AdminNavbar() {
  const { user, logout } = useAuth();
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();

  const navLinkClass = ({ isActive }) =>
    `nav-link ${isActive ? 'active text-white' : 'text-light'}`;

  const goToShop = () => {
    navigate("/products");
  };
  
  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
      <div className="container-fluid">
        <NavLink className="navbar-brand" to="/admin/products">Admin Panel</NavLink>

        <button
          className="navbar-toggler"
          type="button"
          onClick={() => setCollapsed(!collapsed)}
        >
          <span className="navbar-toggler-icon"></span>
        </button>

        <div className={`collapse navbar-collapse ${collapsed ? 'show' : ''}`}>
          <ul className="navbar-nav me-auto mb-2 mb-lg-0">
            <li className="nav-item">
              <NavLink to="/admin/products" className={navLinkClass}>Products</NavLink>
            </li>
            <li className="nav-item">
              <NavLink to="/admin/notifications" className={navLinkClass}>Notifications</NavLink>
            </li>
            <li className="nav-item">
              <NavLink to="/admin/accounts" className={navLinkClass}>Accounts</NavLink>
            </li>
          </ul>

          <div className="d-flex align-items-center gap-2">
            {user ? (
              <>
                <button className="btn btn-warning me-2" onClick={goToShop}>
                  Shop
                </button>
                <span className="text-light me-3">{user.username}</span>
                <button className="btn btn-danger" onClick={logout}>Logout</button>
              </>
            ) : (
              navigate("/login")
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}
