// src/pages/ForbiddenPage.jsx
import { Link } from "react-router-dom";

export default function ForbiddenPage() {
  return (
    <div className="container text-center mt-5">
      <h1 className="display-4 text-danger">403</h1>
      <p className="lead">Access Denied — You don't have permission to view this page.</p>
      <Link to="/products" className="btn btn-primary mt-3">Go Home</Link>
    </div>
  );
}
