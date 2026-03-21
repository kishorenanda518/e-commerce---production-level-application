import React from 'react';
import { useAppDispatch, useAppSelector } from '../../hooks/reduxHooks';
import { selectUser } from '../../redux/slices/authSlice';
import { logoutThunk } from '../../redux/slices/authSlice';
import { useNavigate } from 'react-router-dom';
import './HomePage.css';

const HomePage: React.FC = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const user     = useAppSelector(selectUser);

  const handleLogout = async () => {
    await dispatch(logoutThunk());
    navigate('/login', { replace: true });
  };

  return (
    <div className="home">
      <div className="home__card">
        <div className="home__crown">♛</div>
        <h1 className="home__title">
          Welcome back, <span>{user?.firstName}!</span>
        </h1>
        <p className="home__sub">
          You are signed in as <strong>{user?.email}</strong>
        </p>
        <div className="home__roles">
          {(user?.roles || []).map((r: string) => (
            <span key={r} className="home__role-badge">
              {r.replace('ROLE_', '')}
            </span>
          ))}
        </div>
        <div className="home__actions">
          <button className="home__btn home__btn--gold">
            🛍️ Go to Shop
          </button>
          <button className="home__btn home__btn--outline" onClick={handleLogout}>
            Sign Out
          </button>
        </div>
      </div>
    </div>
  );
};

export default HomePage;