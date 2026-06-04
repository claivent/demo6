import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { UserService } from '../../../user.service';

@Component({
  selector: 'app-user-del',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './user.del.component.html',
  styleUrl: './user.del.component.css',
})
export class UserDelComponent {
  id: number = 0;
  message = '';
  error = '';

  constructor(private userService: UserService) {}

  deleteUser(): void {
    this.message = '';
    this.error = '';

    if (!this.id || this.id <= 0) {
      this.error = 'Zadej platné ID uživatele';
      return;
    }

    this.userService.deleteUser(this.id).subscribe({
      next: (res) => {
        this.message = res.message;
        this.id = 0;
      },
      error: () => {
        this.error = 'Nepodařilo se smazat uživatele (ID neexistuje?)';
      }
    });
  }
}
