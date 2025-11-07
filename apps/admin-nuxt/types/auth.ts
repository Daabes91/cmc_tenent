export type StaffLoginRequest = {
  email: string;
  password: string;
  twoFactorCode?: string | null;
};

export type AuthTokensResponse = {
  tokenType: string;
  accessToken: string;
  accessTokenExpiresAt: string;
  refreshToken: string;
  refreshTokenExpiresAt: string;
};
