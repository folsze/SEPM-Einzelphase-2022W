import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HorseFormComponent} from './component/horse/horse-form/horse-form.component';
import {HorseComponent} from './component/horse/horse.component';
import {OwnerComponent} from './component/owner/owner.component';
import {OwnerCreateComponent} from './component/owner/owner-create/owner-create.component';
import {FormMode} from './enum/formMode';

const routes: Routes = [
  {path: '', redirectTo: 'horses', pathMatch: 'full'},
  {path: 'horses', children: [
    {path: '', component: HorseComponent},
    {path: 'create', component: HorseFormComponent, data: {mode: FormMode.create}},
    {path: ':id', component: HorseFormComponent, data: {mode: FormMode.readonly}},
    {path: ':id/edit', component: HorseFormComponent, data: {mode: FormMode.edit}},
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
