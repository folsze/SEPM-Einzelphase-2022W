import {Component, OnInit} from '@angular/core';
import {AbstractControl, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {Observable, of} from 'rxjs';
import {Horse} from 'src/app/dto/horse';
import {Owner} from 'src/app/dto/owner';
import {Sex} from 'src/app/dto/sex';
import {HorseService} from 'src/app/service/horse.service';
import {OwnerService} from 'src/app/service/owner.service';

export enum HorseCreateEditMode {
  create,
  edit,
}

@Component({
  selector: 'app-horse-create-edit',
  templateUrl: './horse-create-edit.component.html',
  styleUrls: ['./horse-create-edit.component.scss']
})
export class HorseCreateEditComponent implements OnInit {

  public mode: HorseCreateEditMode = HorseCreateEditMode.create;

  public horseForm: FormGroup = this.formBuilder.group({
    id: [null],
    name: [null, [Validators.required, this.noWhitespaceInsideValidator]],
    description: [null],
    dateOfBirth: [null, [Validators.required, this.noDateInFutureValidator]],
    sex: [null, [Validators.required]],
    ownerFullName: [''],
    owner: [null], // owner gets selected by using the ownerFullName control
    motherName: [''],
    mother: [null],
    fatherName: [''],
    father: [null]
  });

  public horse: Horse = {
    name: '',
    description: '',
    dateOfBirth: new Date(),
    sex: Sex.female,
  };

  constructor(
    private service: HorseService,
    private ownerService: OwnerService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
    private formBuilder: FormBuilder,
  ) { }

  public get name(): AbstractControl {
    return this.horseForm.controls.name;
  }

  public get description(): AbstractControl {
    return this.horseForm.controls.description;
  }

  public get dateOfBirth(): AbstractControl {
    return this.horseForm.controls.dateOfBirth;
  }

  public get sex(): AbstractControl {
    return this.horseForm.controls.sex;
  }

  public get ownerFullName(): AbstractControl {
    return this.horseForm.controls.ownerFullName;
  }

  public get owner(): AbstractControl {
    return this.horseForm.controls.owner;
  }

  public get motherName(): AbstractControl {
    return this.horseForm.controls.motherName;
  }

  public get mother(): AbstractControl {
    return this.horseForm.controls.mother;
  }

  public get fatherName(): AbstractControl {
    return this.horseForm.controls.fatherName;
  }

  public get father(): AbstractControl {
    return this.horseForm.controls.father;
  }

  public get heading(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'Create New Horse';
      default:
        return '?';
    }
  }

  public get submitButtonText(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'Create';
      default:
        return '?';
    }
  }

  get modeIsCreate(): boolean {
    return this.mode === HorseCreateEditMode.create;
  }


  private get modeActionFinished(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'created';
      default:
        return '?';
    }
  }

  public noWhitespaceInsideValidator(control: AbstractControl) { // todo: private?
    const containsWhitespace = (control.value || '').trim().match(/\s/g);
    const isValid = !containsWhitespace;
    return isValid ? null : { whitespace: true };
  }

  public noDateInFutureValidator(dateControl: AbstractControl) {
    const now: Date = new Date();
    return dateControl.value < now ? null : { dateInFuture: true };
  }

  public ownerSuggestions = (input: string) => (input === '')
    ? of([])
    : this.ownerService.searchByName(input, 5);

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.mode = data.mode;
    });
  }

  public dynamicCssClassesForInput(input: AbstractControl): any {
    return {
      // This names in this object are determined by the style library,
      // requiring it to follow TypeScript naming conventions does not make sense.
      // eslint-disable-next-line @typescript-eslint/naming-convention
      'is-invalid': input.invalid && input.dirty,
    };
  }

  public formatOwnerName(owner: Owner | null | undefined): string {
    return (owner == null)
      ? ''
      : `${owner.firstName} ${owner.lastName}`;
  }


  public onSubmit(): void {
    const sendHorse: Horse = {
      name: this.name.value.trim(),
      description: this.description.value?.trim(),
      dateOfBirth: this.dateOfBirth.value,
      sex: this.sex.value,
      owner: this.owner?.value,
    };

    let horse$: Observable<Horse>;

    if (this.mode === HorseCreateEditMode.create) {
      horse$ = this.service.create(sendHorse);
    } else {
      console.error('Edit mode not implemented yet.');
      return;
    }

    horse$.subscribe({
      next: horse => {
        this.notification.success(`Horse ${horse.name} successfully ${this.modeActionFinished}.`);
        this.router.navigate(['/horses']);
      },
      error: error => {
        console.error('Error creating horse', error);
        // TODO show an error message to the user. Include and sensibly present the info from the backend!
      }
    });
  }
}
