import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from './user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private base = 'http://localhost:9111/api/db';

  constructor(private http: HttpClient) {}

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.base}/users`);
  }

  addUser(user: { name: string; email: string }): Observable<User> {
    return this.http.post<User>(`${this.base}/add`, user);
  }

  deleteUser(id: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.base}/delete/${id}`);
  }

  modifyUser(id: number, patch: { name?: string; email?: string }): Observable<User> {
    return this.http.patch<User>(`${this.base}/modify/${id}`, patch);
  }
}
