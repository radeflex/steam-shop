import AdminNavbar from "../components/admin/Navbar";

export default function AdminLayout({ children }) {
  return (
    <>
      <AdminNavbar />
      <div className="container mt-4">
        {children}
      </div>
    </>
  );
}
