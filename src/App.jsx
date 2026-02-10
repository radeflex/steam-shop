import { Routes, Route, Navigate, Outlet } from "react-router-dom";
import AppNavbar from "./components/AppNavbar";
import { ToastContainer } from "react-toastify";
import AdminLayout from "./layouts/AdminLayout";

import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import ProductsPage from "./pages/ProductsPage";
import CartPage from "./pages/CartPage";
import CurrentUserPage from "./pages/CurrentUserPage";
import UserEditPage from "./pages/UserEditPage";
import ConfirmEmailPage from "./pages/ConfirmEmailPage";

// Admin pages
import AdminProductsPage from "./pages/admin/ProductsPage";
import AdminNotificationCreatePage from "./pages/admin/NotificationCreatePage";
import AdminProductFormPage from "./pages/admin/ProductFormPage";
import AdminNotificationsPage from "./pages/admin/NotificationsPage";
import AdminRoute from "./components/admin/AdminRoute";
import AdminAccountsPage from "./pages/admin/AccountsPage";

export default function App() {
  return (
    <>
    <Routes>
      {/* Public layout */}
      <Route element={<><AppNavbar /><Outlet /></>}>
        <Route path="/" element={<Navigate to="/products" />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/products" element={<ProductsPage />} />
        <Route path="/profile/edit" element={<UserEditPage />} />
        <Route path="/cart" element={<CartPage />} />
        <Route path="/profile" element={<CurrentUserPage />} />
      </Route>

      {/* Admin layout */}
      <Route path="/admin" element={<AdminRoute />}>
        <Route element={<AdminLayout><Outlet /></AdminLayout>}>
          {/* Products */}
          <Route path="products" element={<AdminProductsPage />} />
          <Route path="products/create" element={<AdminProductFormPage />} />
          <Route path="products/edit/:id" element={<AdminProductFormPage />} />

          {/* Notifications */}
          <Route path="notifications/create" element={<AdminNotificationCreatePage />} />
          <Route path="notifications" element={<AdminNotificationsPage />} />
          <Route path="accounts" element={<AdminAccountsPage />} />
          {/* Default */}
          <Route index element={<Navigate to="products" />} />
        </Route>
      </Route>
      <Route path="/confirm-email" element={<ConfirmEmailPage />} />
    </Routes>
    <ToastContainer
        position="top-right"
        autoClose={3000}
        hideProgressBar
        closeOnClick
        pauseOnHover={false}
        draggable={false}
      />
    </>
  );
}
