import { Component } from '@angular/core';
import {OwnerService} from '../../../service/owner.service';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {Observable} from 'rxjs';
import {AbstractControl, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Owner} from '../../../dto/owner';
import {constructErrorMessageWithList} from '../../../shared/validator';

@Component({
  selector: 'app-owner-create',
  templateUrl: './owner-create.component.html',
  styleUrls: ['./owner-create.component.scss']
})
export class OwnerCreateComponent {

  public ownerForm: FormGroup = this.formBuilder.group({
    firstName: ['', [Validators.required]],
    lastName: ['', [Validators.required]],
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

  public formatOwnerName(owner: Owner | null | undefined): string {
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
        const errorMessage = error.status === 0
          ? 'Connection to the server failed.'
          : constructErrorMessageWithList(error);
        this.notification.error(errorMessage, 'Could not save owner', {enableHtml: true, timeOut: 0});
      }
    });
  }
}
