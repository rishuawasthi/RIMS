import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { AuthResponse, AuthUser, LoginRequest } from '../models/user.model';

const TOKEN_KEY = 'rims_auth_token';
const USER_KEY = 'rims_auth_user';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly baseUrl = '/api/auth';

  readonly currentUser = signal<AuthUser | null>(this.getStoredUser());
  readonly token = signal<string | null>(this.getStoredToken());
  readonly isAuthenticated = computed(() => !!this.token());

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, credentials).pipe(
      tap(response => {
        if (response && response.token) {
          const user: AuthUser = {
            username: response.username,
            role: response.role,
            fullName: response.fullName,
          };
          this.setSession(response.token, user);
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.token.set(null);
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return this.token() ?? this.getStoredToken();
  }

  hasRole(role: string): boolean {
    return this.currentUser()?.role === role;
  }

  private setSession(token: string, user: AuthUser): void {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    this.token.set(token);
    this.currentUser.set(user);
  }

  private getStoredToken(): string | null {
    try {
      return localStorage.getItem(TOKEN_KEY);
    } catch {
      return null;
    }
  }

  private getStoredUser(): AuthUser | null {
    try {
      const stored = localStorage.getItem(USER_KEY);
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  }
}
