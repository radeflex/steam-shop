// UserPage.jsx
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getCurrentUser, getPurchaseHistory } from "../api/user.api";
import { getImageUrl } from "../api/file.api";

export default function CurrentUserPage() {
  const [user, setUser] = useState(null);
  const [history, setHistory] = useState(null);

  useEffect(() => {
    getCurrentUser().then(r => setUser(r.data));
    getPurchaseHistory({ page: 0, size: 10 }).then(r => setHistory(r.data));
  }, []);

  if (!user) return <p className="mt-3">Loading user...</p>;

  return (
    <div className="container mt-4">
      <h2 className="mb-4">Profile</h2>

      {/* User info */}
      <div className="card mb-3" style={{ maxWidth: "600px" }}>
        <div className="card-body d-flex align-items-start">
          {user.avatarUrl && (
            <img
              src={getImageUrl(user.avatarUrl)}
              alt={`${user.username}'s avatar`}
              style={{
                width: "120px",
                height: "120px",
                borderRadius: "50%",
                marginRight: "20px",
                objectFit: "cover"
              }}
            />
          )}
          <div className="flex-grow-1">
            <p><strong>ID:</strong> {user.id}</p>
            <p><strong>Username:</strong> {user.username}</p>
            <p><strong>Balance:</strong> {user.balance} ₽</p>
            <p><strong>Points:</strong> {user.points}</p>
            <p><strong>Joined at:</strong> {new Date(user.createdAt).toLocaleString()}</p>

            {/* Кнопка редактирования */}
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
