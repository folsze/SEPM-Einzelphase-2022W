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

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    HorseComponent,
    HorseFormComponent,
    AutocompleteComponent,
    OwnerComponent,
    OwnerCreateComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    NgbModule,
    ToastrModule.forRoot({
      timeOut: 10000,
      tapToDismiss: false,
      closeButton: true,
      preventDuplicates: true,
      resetTimeoutOnDuplicate: true,
      extendedTimeOut: 0,
    }),
    BrowserAnimationsModule,
    ReactiveFormsModule,
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
