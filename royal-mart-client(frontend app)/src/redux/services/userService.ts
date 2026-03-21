// src/redux/services/userService.ts

import axios from 'axios';
import { USER_ENDPOINTS } from '../../constants/apiEndpoints';

export const fetchUsersApi = async (
  page: number,
  size: number,
  token: string
) => {
  const res = await axios.get(USER_ENDPOINTS.ALL_USERS, {
    params: { page, size, sortBy: 'createdAt', direction: 'desc' },
    headers: { Authorization: `Bearer ${token}` },
    withCredentials: true,
  });
  return res.data.data;
};

export const updateUserStatusApi = async (
  userId: string,
  status: string,
  token: string
) => {
  const res = await axios.patch(
    USER_ENDPOINTS.UPDATE_STATUS(userId),
    { status },
    { headers: { Authorization: `Bearer ${token}` } }
  );
  return res.data.data;
};

export const updateUserRoleApi = async (
  userId: string,
  role: string,
  token: string
) => {
  const res = await axios.patch(
    USER_ENDPOINTS.UPDATE_ROLE(userId),
    { role },
    { headers: { Authorization: `Bearer ${token}` } }
  );
  return res.data.data;
};