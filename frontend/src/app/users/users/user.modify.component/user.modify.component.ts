import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { UserService } from '../../../user.service';

@Component({
  selector: 'app-user-modify',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './user.modify.component.html',
  styleUrl: './user.modify.component.css',
})
export class UserModifyComponent {
  id: number = 0;
  name = '';
  email = '';
  message = '';
  error = '';

  constructor(private userService: UserService) {}

  modifyUser(): void {
    this.message = '';
    this.error = '';

    if (!this.id || this.id <= 0) {
      this.error = 'Zadej platné ID uživatele';
      return;
    }

    const patch: { name?: string; email?: string } = {};
    if (this.name.trim())  patch.name  = this.name.trim();
    if (this.email.trim()) patch.email = this.email.trim();

    if (Object.keys(patch).length === 0) {
      this.error = 'Vyplň alespoň jméno nebo email';
      return;
    }

    this.userService.modifyUser(this.id, patch).subscribe({
      next: (updated) => {
        this.message = `Uživatel ${updated.id} upraven: ${updated.name} / ${updated.email}`;
        this.id = 0;
        this.name = '';
        this.email = '';
      },
      error: () => {
        this.error = 'Nepodařilo se upravit uživatele (ID neexistuje?)';
      }
    });
  }
}
