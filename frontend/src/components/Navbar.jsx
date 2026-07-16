import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <header className="topbar">
      <div className="topbar-inner">
        <div className="wordmark" onClick={() => navigate('/dashboard')} style={{ cursor: 'pointer' }}>
          <span className="wordmark-mark" aria-hidden="true"></span>
          Team Task Manager
        </div>
        <div className="topbar-right">
          {user && (
            <span className="user-chip">
              <strong>{user.name}</strong> · {user.email}
            </span>
          )}
          <button className="btn btn-ghost" onClick={handleLogout}>Log out</button>
        </div>
      </div>
    </header>
  );
}
