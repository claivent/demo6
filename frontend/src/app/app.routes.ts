import { Routes } from '@angular/router';
import {UsersComponent} from './users/users/users.component';
import { UserDelComponent } from './users/users/user.del.component/user.del.component';
import { UserAddComponent } from './users/users/user.add.component/user.add.component';

export const routes: Routes = [

  {path: 'users', component: UsersComponent},
    {
    path: 'user/add',
    component: UserAddComponent
  },
  {
    path: 'user/delete',
    component: UserDelComponent
  },
  {
    path: '',
    redirectTo: 'user/add',
    pathMatch: 'full'
  }
];
