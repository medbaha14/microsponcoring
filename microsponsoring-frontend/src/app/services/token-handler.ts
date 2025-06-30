export class TokenHandler {
  static saveToken(token: string) {
    localStorage.setItem('token', token);
  }

  static getToken(): string | null {
    return localStorage.getItem('token');
  }

  static saveUser(user: any) {
    localStorage.setItem('user', JSON.stringify(user));
    if (user && user.userType) {
      localStorage.setItem('userType', user.userType);
    }
  }

  static getUser(): any {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  }

  static clear() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('userType');
  }
} 