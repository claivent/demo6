import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../../user.service';
import { User } from '../../../user.model';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-user.add.component',
  imports: [FormsModule, CommonModule],
  templateUrl: './user.add.component.html',
  styleUrl: './user.add.component.css',
})
export class UserAddComponent {

  user: User = {
    id: 0,
    name: '',
    email: ''
  };

  message: string = '';
  error: string = '';

  constructor(private userService: UserService) {}

  addUser(): void {
    this.message = '';
    this.error = '';

    this.userService.addUser(this.user).subscribe({
      next: () => {
        this.message = 'Uživatel byl úspěšně přidán';

        this.user = {
          id: 0,
          name: '',
          email: ''
        };
      },
      error: () => {
        this.error = 'Nepodařilo se přidat uživatele';
      }
    });
  }
}
