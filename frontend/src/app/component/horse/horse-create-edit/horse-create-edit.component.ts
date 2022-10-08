import {Component, OnInit} from '@angular/core';
import {AbstractControl, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {debounceTime, distinctUntilChanged, Observable, of, OperatorFunction, switchMap} from 'rxjs';
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

  private static readonly typeAheadMaxOptionCount = 5;

  public currentOwnerInputContent = '';

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

  // ---------------------------- START OF GETTER SECTION ----------------------------
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

  public get ownerFullNameSubstring(): AbstractControl {
    return this.horseForm.controls.ownerFullName;
  }

  public get ownerFormControl(): AbstractControl {
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

  public get modeIsCreate(): boolean {
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
  // ---------------------------- END OF GETTER SECTION ----------------------------

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.mode = data.mode;
    });
  }

  public setOwner(owner: Owner) {
    this.currentOwnerInputContent = this.getFullNameIfExists(owner);
    this.horseForm.controls.owner.setValue(owner);
  }

  public clearOwner(): void {
    this.ownerFullNameSubstring.reset();
    this.ownerFormControl.reset();
    this.currentOwnerInputContent = '';
  }

  public getFullNameIfExists(owner: Owner): string { // fixme: will cause unknown bug soon, cause null
    return owner.firstName + ' ' + owner.lastName;
  }

  public ownerFullNameResultFormatter = (owner: Owner) => this.getFullNameIfExists(owner);

  public searchOwner: OperatorFunction<string, readonly Owner[]> = (fullName$: Observable<string>) =>
    fullName$.pipe(
      debounceTime(200),
      distinctUntilChanged(),
      switchMap(fullName => this.searchOwnersByFullName(fullName))
    );

  public noWhitespaceInsideValidator(control: AbstractControl) { // todo: private?
    const containsWhitespace = (control.value || '').trim().match(/\s/g);
    const isValid = !containsWhitespace;
    return isValid ? null : { whitespace: true };
  }

  public noDateInFutureValidator(dateControl: AbstractControl) {
    const now: Date = new Date();
    const isValid = !(dateControl.value < now);
    return isValid ? null : { dateInFuture: true };
  }

  public dynamicCssClassesForInput(input: AbstractControl): any {
    return {
      // This names in this object are determined by the style library,
      // requiring it to follow TypeScript naming conventions does not make sense.
      // eslint-disable-next-line @typescript-eslint/naming-convention
      'is-invalid': input.invalid && input.dirty,
    };
  }

  public onSubmit(): void {
    const sendHorse: Horse = {
      name: this.name.value.trim(),
      description: this.description.value?.trim(),
      dateOfBirth: this.dateOfBirth.value,
      sex: this.sex.value,
      owner: this.ownerFormControl?.value,
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

  private searchOwnersByFullName(fullName: string): Observable<Owner[]> {
    return (fullName === '') ? of([]) :
    this.ownerService.searchByFullNameSubstring(fullName.trim(),
      HorseCreateEditComponent.typeAheadMaxOptionCount);
  }
}
