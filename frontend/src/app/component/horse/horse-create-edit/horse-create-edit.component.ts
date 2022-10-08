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

export enum FormMode {
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
  public initialName: string;

  public mode: FormMode = FormMode.create;

  public horseForm: FormGroup = this.formBuilder.group({
    id: [null],
    name: [null, [Validators.required, this.noWhitespaceInsideValidator]],
    description: [null],
    dateOfBirth: [null, [Validators.required, this.noDateInFutureValidator]],
    sex: [null, [Validators.required]],
    ownerFullName: [''],
    owner: [null], // owner gets selected by using the ownerFullName control
    // motherName: [''],
    // mother: [null],
    // fatherName: [''],
    // father: [null]
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

  // -------------------------------------------------------- START OF GETTER SECTION ---------------------------------
  public get editHorseId(): number {
    return this.horseForm.controls.id.value;
  }

  public get nameFormControl(): AbstractControl {
    return this.horseForm.controls.name;
  }

  public get descriptionFormControl(): AbstractControl {
    return this.horseForm.controls.description;
  }

  public get dateOfBirthFormControl(): AbstractControl {
    return this.horseForm.controls.dateOfBirth;
  }

  public get sexFormControl(): AbstractControl {
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
    if (this.isCreateMode) {
      return 'Add Horse';
    } else {
      return 'Edit Horse';
    }
  }

  public get submitButtonText(): string {
    if (this.isCreateMode) {
      return 'Add Horse';
    } else {
      return 'Submit Edits';
    }
  }

  public get isEditMode(): boolean {
    return this.mode === FormMode.edit;
  }

  public get isCreateMode(): boolean {
    return this.mode === FormMode.create;
  }

  private get modeActionFinished(): string {
    if (this.isCreateMode) {
      return 'created';
    } else {
      return 'edited';
    }
  }
  // -------------------------------------------------------- END OF GETTER SECTION -----------------------------------

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      // the following: really rare edge case: if user manually edits url from horses/1/edit to horses/create without reloading somehow
      if (this.isEditMode && data.mode === FormMode.create) {
        this.horseForm.reset();
      }
      this.mode = data.mode;
    });

    this.route.paramMap.subscribe(paramMap => {
      if (this.isEditMode) {
        this.getHorseValues(Number(paramMap.get('id')));
      }
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
    return owner ? owner.firstName + ' ' + owner.lastName : '';
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
      id: this.editHorseId,
      name: this.nameFormControl.value.trim(),
      description: this.descriptionFormControl.value?.trim(),
      dateOfBirth: this.dateOfBirthFormControl.value,
      sex: this.sexFormControl.value,
      owner: this.ownerFormControl?.value,
    };

    let horse$: Observable<Horse>;

    if (this.isCreateMode) {
      horse$ = this.service.create(sendHorse);
    } else {
      horse$ = this.service.update(sendHorse);
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

  private getHorseValues(id: number): void {
    this.service.getHorseById(id).subscribe({
      next: horse => {
        this.initialName = horse.name;
        this.currentOwnerInputContent = this.getFullNameIfExists(this.ownerFormControl.value);
        this.setFormValues(horse);
      },
      error: error => {
        console.error(error.message);
      }
    });
  }

  private setFormValues(horse: Horse): void {
    this.horseForm.setValue({
      id: horse.id,
      name: horse.name,
      description: horse.description,
      dateOfBirth: horse.dateOfBirth,
      sex: horse.sex,
      ownerFullName: this.getFullNameIfExists(this.ownerFormControl.value),
      owner: horse.owner,
      // motherName: horse.mother ? horse.mother.name : null,
      // mother: horse.mother,
      // fatherName: horse.father ? horse.father.name : null,
      // father: horse.father,
    });
  }
}
