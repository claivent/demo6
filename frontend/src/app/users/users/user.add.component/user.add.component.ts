import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../../user.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-user-add',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './user.add.component.html',
  styleUrl: './user.add.component.css',
})
export class UserAddComponent {
  name = '';
  email = '';
  message = '';
  error = '';

  constructor(private userService: UserService) {}

  addUser(): void {
    this.message = '';
    this.error = '';

    this.userService.addUser({ name: this.name, email: this.email }).subscribe({
      next: (created) => {
        this.message = `Uživatel přidán (ID: ${created.id})`;
        this.name = '';
        this.email = '';
      },
      error: () => {
        this.error = 'Nepodařilo se přidat uživatele';
      }
    });
  }
}
