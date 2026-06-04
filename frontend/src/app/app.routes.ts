import { Routes } from '@angular/router';
import { UsersComponent } from './users/users/users.component';
import { UserAddComponent } from './users/users/user.add.component/user.add.component';
import { UserDelComponent } from './users/users/user.del.component/user.del.component';
import { UserModifyComponent } from './users/users/user.modify.component/user.modify.component';

export const routes: Routes = [
  { path: 'users',        component: UsersComponent },
  { path: 'user/add',     component: UserAddComponent },
  { path: 'user/delete',  component: UserDelComponent },
  { path: 'user/modify',  component: UserModifyComponent },
  { path: '', redirectTo: 'users', pathMatch: 'full' }
];
