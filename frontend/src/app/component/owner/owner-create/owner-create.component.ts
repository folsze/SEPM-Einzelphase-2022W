import { Component } from '@angular/core';
import {OwnerService} from '../../../service/owner.service';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {Observable, of} from 'rxjs';
import {AbstractControl, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Owner} from '../../../dto/owner';

@Component({
  selector: 'app-owner-create',
  templateUrl: './owner-create.component.html',
  styleUrls: ['./owner-create.component.scss']
})
export class OwnerCreateComponent {

  public ownerForm: FormGroup = this.formBuilder.group({
    firstName: ['', [Validators.required, this.noWhitespaceInsideValidator]],
    lastName: ['', [Validators.required, this.noWhitespaceInsideValidator]],
    email: [null, Validators.email]
  });

  constructor(
    private service: OwnerService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
    private formBuilder: FormBuilder
  ) {
  }

  public get firstName(): AbstractControl {
    return this.ownerForm.controls.firstName;
  }

  public get lastName(): AbstractControl {
    return this.ownerForm.controls.lastName;
  }

  public get email(): AbstractControl {
    return this.ownerForm.controls.email;
  }

  public noWhitespaceInsideValidator(control: AbstractControl) {
    const containsWhitespace = (control.value || '').trim().match(/\s/g);
    const isValid = !containsWhitespace;
    return isValid ? null : { whitespace: true };
  }

  public ownerSuggestions = (input: string) => (input === '')
    ? of([])
    : this.service.searchByName(input, 5);

  public formatOwnerName(owner: Owner | null | undefined): string { // todo Fragestunde: what if owner undefined?
    return (owner == null) ? '' : `${this.firstName.value} ${this.lastName.value}`;
  }

  public onSubmit(): void {
    const observable: Observable<Owner> = this.service.create({
      firstName: this.firstName.value.trim(),
      lastName: this.lastName.value.trim(),
      email: this.email.value?.trim()
    });
    observable.subscribe({
      next: owner => {
        this.notification.success(`Owner ${this.formatOwnerName(owner)} successfully created.`);
        this.router.navigate(['/owners']);
      },
      error: error => {
        console.error('Error creating owner', error);
        // TODO show an error message to the user. Include and sensibly present the info from the backend!
      }
    });
  }
}
