import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  sendNotificationToAll,
  sendNotificationToUser
} from '../../api/notifications.api';
import { getUsers } from '../../api/user.api';
import { useEffect } from 'react';

export default function AdminNotificationCreatePage() {
  const [title, setTitle] = useState('');
  const [text, setText] = useState('');
  const [type, setType] = useState('INFO');
  const [users, setUsers] = useState([]);
  const [filter, setFilter] = useState({username: ''});
  const [selectedUser, setSelectedUser] = useState(null);
  const [skipSearch, setSkipSearch] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [sendToAll, setSendToAll] = useState(true);
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();
  useEffect(() => {
    if (sendToAll) {
      setUsers([]);
      setSelectedUser(null);
      setSkipSearch(false);
      return;
    }

    if (!filter.username.trim() || skipSearch) {
      setUsers([]);
      return;
    }

    const timeout = setTimeout(async () => {
      try {
        const { data } = await getUsers(
          filter,
          { page: 0, size: 5 }
        );

        setUsers(data.content ?? data);
      } catch (e) {
        console.error(e);
      }
    }, 1000);

    return () => clearTimeout(timeout);
    }, [filter.username, sendToAll, skipSearch]);

    const handleInputChange = (value) => {
      setFilter({ username: value });
      setSelectedUser(null);
      setSkipSearch(false); // снова разрешаем поиск
    };

    const handleSubmit = async (e) => {
      e.preventDefault();
      setLoading(true);
      
    try {
      setErrorMessage(''); // сброс ошибки перед отправкой

      if (sendToAll) {
        await sendNotificationToAll({ title, text, type });
      } else {
        if (!selectedUser) {
          setErrorMessage('Выберите пользователя из списка');
          return;
        }

        await sendNotificationToUser(selectedUser.id, {
          title,
          text,
          type
        });
      }

      navigate('/admin/notifications');
      } catch (err) {
        console.error(err);
        if (err.response?.status === 404) {
          setErrorMessage('Пользователь не найден');
        } else {
          setErrorMessage('Не удалось отправить уведомление');
        }
      } finally {
        setLoading(false);
      }
    };
    const handleSelectUser = (user) => {
      setSelectedUser(user);
      setFilter({ username: user.username });
      setUsers([]);
    };


  return (
    <div className="container mt-4">
      <h3>Create Notification</h3>

    {errorMessage && (
      <div className="alert alert-danger" role="alert">
        {errorMessage}
      </div>
    )}
      <form onSubmit={handleSubmit} className="mt-3">
        {/* TITLE */}
        <div className="mb-3">
          <label className="form-label">Title</label>
          <input
            type="text"
            className="form-control"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
            minLength={3}
            maxLength={32}
          />
        </div>

        {/* TEXT */}
        <div className="mb-3">
          <label className="form-label">Text</label>
          <textarea
            className="form-control"
            value={text}
            onChange={(e) => setText(e.target.value)}
            maxLength={500}
            rows={4}
          />
        </div>

        {/* USERNAME */}
        <div className="mb-3">
          <label className="form-label">Username</label>
          <input
            type="text"
            className="form-control"
            value={selectedUser ? selectedUser.username : filter.username}
            onChange={(e) => handleInputChange(e.target.value)}
            disabled={sendToAll}
            required={!sendToAll}
            placeholder={sendToAll ? 'Disabled when sending to all' : 'Start typing username'}
          />
          {!sendToAll && users.length > 0 && (
            <div className="list-group mt-2">
              {users.map(user => (
                <button
                  type="button"
                  key={user.id}
                  className="list-group-item list-group-item-action d-flex align-items-center"
                  onClick={() => handleSelectUser(user)}
                >
                  <img
                    src={user.avatarUrl || '/avatar-placeholder.png'}
                    alt=""
                    width={32}
                    height={32}
                    className="rounded-circle me-2"
                  />
                  <span>{user.username}</span>
                </button>
              ))}
            </div>
          )}
        </div>

        {/* SEND TO ALL */}
        <div className="form-check mb-3">
          <input
            className="form-check-input"
            type="checkbox"
            id="sendToAll"
            checked={sendToAll}
            onChange={(e) => setSendToAll(e.target.checked)}
          />
          <label className="form-check-label" htmlFor="sendToAll">
            Send to all users
          </label>
        </div>

        {/* TYPE */}
        <div className="mb-3">
          <label className="form-label">Type</label>
          <select
            className="form-select"
            value={type}
            onChange={(e) => setType(e.target.value)}
          >
            <option value="INFO">INFO</option>
            <option value="PENDING">PENDING</option>
            <option value="SUCCESS">SUCCESS</option>
            <option value="FAIL">FAIL</option>
          </select>
        </div>

        <div className="d-flex gap-2">
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Sending...' : 'Send Notification'}
          </button>
          <button type="button" className="btn btn-secondary" onClick={() => navigate("/admin/notifications")}>
            Discard
          </button>
        </div>
      </form>
    </div>
  );
}
