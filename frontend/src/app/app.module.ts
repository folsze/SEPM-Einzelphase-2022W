import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {HttpClientModule} from '@angular/common/http';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {ToastrModule} from 'ngx-toastr';

import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {AutocompleteComponent} from './component/autocomplete/autocomplete.component';
import {HeaderComponent} from './component/header/header.component';
import {HorseFormComponent} from './component/horse/horse-form/horse-form.component';
import {HorseComponent} from './component/horse/horse.component';
import { OwnerComponent } from './component/owner/owner.component';
import { OwnerCreateComponent } from './component/owner/owner-create/owner-create.component';
import { ConfirmDeleteModalContentComponent } from './component/confirm-delete-modal-content/confirm-delete-modal-content.component';
import { FamilyTreeComponent } from './component/horse/family-tree/family-tree.component';

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    HorseComponent,
    HorseFormComponent,
    AutocompleteComponent,
    OwnerComponent,
    OwnerCreateComponent,
    ConfirmDeleteModalContentComponent,
    FamilyTreeComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    NgbModule,
    ToastrModule.forRoot({
      timeOut: 5000,
      extendedTimeOut: 0,
      closeButton: true,
      tapToDismiss: false,
      autoDismiss: true,
      preventDuplicates: true,
      maxOpened: 5,
      progressBar: true
    }),
    BrowserAnimationsModule,
    ReactiveFormsModule,
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
