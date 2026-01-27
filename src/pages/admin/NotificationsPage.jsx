import { useEffect, useState } from 'react';
import { getAdminNotifications, deleteNotification } from '../../api/notifications.api';
import { Link } from 'react-router-dom';

export default function AdminNotificationsPage() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0); // текущая страница
  const [hasMore, setHasMore] = useState(true); // есть ли ещё уведомления

  const PAGE_SIZE = 20;

  const fetchNotifications = async (nextPage = 0) => {
    if (!hasMore && nextPage !== 0) return;

    setLoading(true);
    try {
      const res = await getAdminNotifications({ page: nextPage, size: PAGE_SIZE });
      const newNotifications = res.data.content || [];

      if (nextPage === 0) {
        setNotifications(newNotifications);
      } else {
        setNotifications(prev => [...prev, ...newNotifications]);
      }

      setHasMore(newNotifications.length === PAGE_SIZE); // если меньше PAGE_SIZE — больше страниц нет
      setPage(nextPage);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this notification?')) {
      await deleteNotification(id);
      fetchNotifications(0); // обновляем с первой страницы после удаления
    }
  };

  useEffect(() => { fetchNotifications(0); }, []);

  if (loading && page === 0) return <div className="text-center p-3">Loading...</div>;

  return (
    <div className="container mt-4">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h3>Notifications</h3>
        <Link to="/admin/notifications/create" className="btn btn-success">Create Notification</Link>
      </div>

      <table className="table table-striped table-hover">
        <thead className="table-dark">
          <tr>
            <th>ID</th>
            <th>Title</th>
            <th>Text</th>
            <th>Type</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {notifications.length ? notifications.map(n => (
            <tr key={n.id}>
              <td>{n.id}</td>
              <td>{n.title}</td>
              <td>{n.text || '-'}</td>
              <td>{n.type}</td>
              <td>
                <button onClick={() => handleDelete(n.id)} className="btn btn-danger btn-sm">Delete</button>
              </td>
            </tr>
          )) : "No notifications found."}
        </tbody>
      </table>

      {hasMore && (
        <div className="text-center my-3">
          <button
            className="btn btn-outline-primary"
            onClick={() => fetchNotifications(page + 1)}
            disabled={loading}
          >
            {loading ? 'Loading...' : 'Показать ещё'}
          </button>
        </div>
      )}
    </div>
  );
}
