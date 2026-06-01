import {Component, OnInit} from '@angular/core';
import {User} from '../../user.model';
import {UserService} from '../../user.service';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './users.component.html',
  styleUrl: './users.component.css'
})
export class UsersComponent implements OnInit {
  users: User[] = [];
  error: string | null = null;
  loading = true;

  constructor(private userService: UserService) {}

  ngOnInit(): void{
    this.userService.getUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.loading = false;
        if (data.length === 0) {
          this.error = "no users";
        } else {
          this.error = null;
        }
      },
      error: () => {
        this.error = "nestáhli se uživatelé";
        this.loading = false;
      }
    });
  }
}
