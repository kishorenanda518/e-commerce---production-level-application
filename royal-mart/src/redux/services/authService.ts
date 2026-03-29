import axios from 'axios';
import { AUTH_ENDPOINTS } from '../../constants/apiEndpoints';

export const loginApi = async (payload: {
  usernameOrEmail: string;
  password: string;
}) => {
  const res = await axios.post(
    AUTH_ENDPOINTS.LOGIN,
    payload,
    { withCredentials: true }
  );
  console.log('Login API Response:', res.data);
  return res.data.data;
};

export const registerApi = async (payload: any) => {
  const res = await axios.post(AUTH_ENDPOINTS.REGISTER, payload);
  return res.data;
};

export const logoutApi = async () => {
  await axios.post(AUTH_ENDPOINTS.LOGOUT, {}, { withCredentials: true });
};