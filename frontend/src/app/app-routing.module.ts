import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {FormMode, HorseCreateEditComponent} from './component/horse/horse-create-edit/horse-create-edit.component';
import {HorseComponent} from './component/horse/horse.component';
import {OwnerComponent} from './component/owner/owner.component';
import {OwnerCreateComponent} from './component/owner/owner-create/owner-create.component';
import {HorseDetailComponent} from './component/horse-detail/horse-detail.component';

const routes: Routes = [
  {path: '', redirectTo: 'horses', pathMatch: 'full'},
  {path: 'horses', children: [
    {path: '', component: HorseComponent},
    {path: 'create', component: HorseCreateEditComponent, data: {mode: FormMode.create}},
    {path: ':id', component: HorseDetailComponent},
    {path: ':id/edit', component: HorseCreateEditComponent, data: {mode: FormMode.edit}},
  ]},
  {path: 'owners', children: [
      {path: '', component: OwnerComponent},
      {path: 'create', component: OwnerCreateComponent},
    ]
  },
  {path: '**', redirectTo: 'horses'},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
