import {Component, OnInit} from '@angular/core';
import {AbstractControl, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {debounceTime, distinctUntilChanged, Observable, of, OperatorFunction, switchMap} from 'rxjs';
import {Horse, HorseDetail} from 'src/app/dto/horse';
import {Owner} from 'src/app/dto/owner';
import {Sex} from 'src/app/dto/sex';
import {HorseService} from 'src/app/service/horse.service';
import {OwnerService} from 'src/app/service/owner.service';
import {FormMode} from '../../../enum/formMode';
import {constructErrorMessageWithList} from '../../../shared/validator';

@Component({
  selector: 'app-horse-form',
  templateUrl: './horse-form.component.html',
  styleUrls: ['./horse-form.component.scss']
})
export class HorseFormComponent implements OnInit {

  private static readonly typeAheadMaxOptionCount = 5;

  public mode: FormMode;

  public horseForm: FormGroup = this.formBuilder.group({
    id: [null],
    name: [null, [Validators.required]],
    description: [null],
    dateOfBirth: [null, [Validators.required, HorseFormComponent.noDateInFutureValidator]],
    sex: [null, [Validators.required]],
    ownerFullNameSubstring: [''],
    owner: [null], // owner gets selected by using the ownerFullName control
    motherNameSubstring: [''],
    mother: [null],
    fatherNameSubstring: [''],
    father: [null]
  });

  constructor(
    private horseService: HorseService,
    private ownerService: OwnerService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
    private formBuilder: FormBuilder,
  ) { }

  // -------------------------------------------------------- START OF GETTER SECTION ---------------------------------
  public get idOfHorseBeingEditedElseUndefined(): number {
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

  public get ownerFullNameSubstringFormControl(): AbstractControl {
    return this.horseForm.controls.owner;
  }

  public get ownerFormControl(): AbstractControl {
    return this.horseForm.controls.owner;
  }

  public get currentOwnerFullName(): string {
    return this.ownerFormControl.value ?
      this.ownerFormControl.value.firstName + ' ' + this.ownerFormControl.value.lastName :
      '';
  }

  public get motherNameSubstringFormControl(): AbstractControl {
    return this.horseForm.controls.motherNameSubstring;
  }

  public get motherFormControl(): AbstractControl {
    return this.horseForm.controls.mother;
  }

  public get currentMotherName(): string {
    return this.motherFormControl.value?.name;
  }

  public get fatherNameSubstringFormControl(): AbstractControl {
    return this.horseForm.controls.fatherNameSubstring;
  }

  public get fatherFormControl(): AbstractControl {
    return this.horseForm.controls.father;
  }

  public get currentFatherName(): string {
    return this.fatherFormControl.value?.name;
  }

  public get heading(): string {
    if (this.isCreateMode) {
      return 'Add Horse';
    } else if (this.isEditMode) {
      return 'Edit Horse';
    } else {
      return 'Details of Horse';
    }
  }

  public get submitButtonText(): string {
    if (this.isCreateMode) {
      return 'Add Horse';
    } else {
      return 'Save changes';
    }
  }

  public get isEditMode(): boolean {
    return this.mode === FormMode.edit;
  }

  public get isCreateMode(): boolean {
    return this.mode === FormMode.create;
  }

  public get isReadonlyMode(): boolean {
    return this.mode === FormMode.readonly;
  }

  private get modeActionFinished(): string {
    if (this.isCreateMode) {
      return 'created';
    } else {
      return 'edited';
    }
  }
  // -------------------------------------------------------- END OF GETTER & SETTER SECTION -----------------------------------

  private static getFullNameIfExistsOf(owner: Owner): string {
    return owner ? owner.firstName + ' ' + owner.lastName : '';
  }

  private static getNameIfExistsOf(h: HorseDetail): string {
    return h ? h.name : '';
  }

  private static noDateInFutureValidator(dateControl: AbstractControl) {
    const now: Date = new Date();
    const dateInput: Date = new Date(dateControl.value);
    const dateInFuture = dateInput > now;
    return dateInFuture ? { dateInFuture: true } : null;
  }

  /**
   * With the current implementation it is not possible to directly transition from [edit/readonly <-> create].
   * That's why when subscribing to mode changes, the transition where special things happen is only: [edit <-> readonly].
   * [read -> edit] requires no actions (no fetch/clear).
   * [edit -> read] requires fetch
   */
  ngOnInit(): void {
    this.route.data.subscribe(
    data => {
      if (this.isEditMode || this.isReadonlyMode) {
        const id = Number(this.route.snapshot.params.id);
        this.requestHorseValues(id);
      }
      this.mode = data.mode;
    });

    this.route.paramMap.subscribe(paramMap => {
      const id: string | null = paramMap.get('id');
      if (id !== null) {
        this.requestHorseValues(Number(id));
      }
    });
  }

  public navigateToMother() {
    if (this.isReadonlyMode) {
      this.router.navigate(['../' + this.motherFormControl.value.id],
      { relativeTo: this.route });
    } else if (this.isEditMode) {
      this.router.navigate(['../../' + this.motherFormControl.value.id + '/edit'],
      { relativeTo: this.route });
    }
  }

  public navigateToFather() {
    if (this.isReadonlyMode) {
      this.router.navigate(['../' + this.fatherFormControl.value.id],
        { relativeTo: this.route });
    } else if (this.isEditMode) {
      this.router.navigate(['../../' + this.fatherFormControl.value.id + '/edit'],
        { relativeTo: this.route });
    }
  }

  public setCreateMode() {
    this.horseForm.reset();
    this.mode = FormMode.create;
  }

  public setEditMode(){
    this.mode = FormMode.edit;
  }

  public setReadonlyMode() {
    this.mode = FormMode.readonly;
  }

  public confirmIfDeleteHorse(): void {
    if (confirm(`Are you sure you want to delete the horse \"${this.nameFormControl.value}\"`)) {
      this.horseService.deleteHorse(this.idOfHorseBeingEditedElseUndefined).subscribe({
        next: () => {
          this.notification.success(`Horse ${this.nameFormControl.value} successfully deleted.`);
          this.router.navigate(['/horses']);
        },
        error: (error) => {
          const errorMessage = error.status === 0
            ? 'Connection to the server failed.'
            : constructErrorMessageWithList(error);
          this.notification.error(errorMessage, `Could not delete horse.`, {enableHtml: true, timeOut: 0});
        }
      });
    }
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
    const sendHorse: HorseDetail = {
      id: this.idOfHorseBeingEditedElseUndefined,
      name: this.nameFormControl.value.trim(),
      description: this.descriptionFormControl.value?.trim(),
      dateOfBirth: this.dateOfBirthFormControl.value,
      sex: this.sexFormControl.value,
      owner: this.ownerFormControl.value,
      mother: this.motherFormControl.value,
      father: this.fatherFormControl.value
    };

    let horse$: Observable<Horse>;

    if (this.isCreateMode) {
      horse$ = this.horseService.create(sendHorse);
    } else {
      horse$ = this.horseService.update(sendHorse);
    }

    horse$.subscribe({
      next: horse => {
        this.notification.success(`Horse ${horse.name} successfully ${this.modeActionFinished}.`);
        this.router.navigate(['/horses']);
      },
      error: error => {
        console.error('Error creating horse', error);
        const errorMessage = error.status === 0
          ? 'Connection to the server failed.'
          : constructErrorMessageWithList(error);
        this.notification.error(errorMessage, `Horse could not be ${this.modeActionFinished}`, { enableHtml: true, timeOut: 0 });
      }
    });
  }

  // -------------------------------------------------------- START OF OWNER TYPEAHEAD SECTION -----------------------------------
  public setOwner(owner: Owner) {
    this.horseForm.controls.owner.setValue(owner);
  }

  public clearOwner(): void {
    this.ownerFullNameSubstringFormControl.reset();
    this.ownerFormControl.reset();
  }

  public ownerFullNameResultFormatter = (owner: Owner) => HorseFormComponent.getFullNameIfExistsOf(owner);

  public searchOwner: OperatorFunction<string, readonly Owner[]> = (fullName$: Observable<string>) =>
    fullName$.pipe(
      debounceTime(200),
      distinctUntilChanged(),
      switchMap(fullName => this.searchOwnersByFullName(fullName))
    );

  public searchOwnersByFullName(fullName: string): Observable<Owner[]> {
    return (fullName === '') ? of([]) :
      this.ownerService.searchByFullNameSubstring(fullName.trim(),
        HorseFormComponent.typeAheadMaxOptionCount);
  }
  // -------------------------------------------------------- END OF OWNER TYPEAHEAD SECTION -----------------------------------

  // -------------------------------------------------------- START OF MOTHER TYPEAHEAD SECTION -----------------------------------
  public setMother(mother: HorseDetail) {
    this.horseForm.controls.mother.setValue(mother);
  }

  public clearMother(): void {
    this.motherNameSubstringFormControl.reset();
    this.motherFormControl.reset();
  }

  public motherNameResultFormatter = (mother: HorseDetail) => HorseFormComponent.getNameIfExistsOf(mother);

  public searchMother: OperatorFunction<string, readonly HorseDetail[]> = (name$: Observable<string>) =>
    name$.pipe(
      debounceTime(200),
      distinctUntilChanged(),
      switchMap(name => this.searchMothersByNameSubstring(name))
    );

  public searchMothersByNameSubstring(name: string): Observable<HorseDetail[]> {
    return (name === '') ? of([]) :
      this.horseService.search({
          name,
          sex: Sex.female,
          limit: HorseFormComponent.typeAheadMaxOptionCount,
          idOfHorseToBeExcluded: this.idOfHorseBeingEditedElseUndefined,
      }
      );
  }
  // -------------------------------------------------------- END OF MOTHER TYPEAHEAD SECTION -----------------------------------

  // -------------------------------------------------------- START OF FATHER TYPEAHEAD SECTION -----------------------------------
  public setFather(father: HorseDetail) {
    this.horseForm.controls.father.setValue(father);
  }

  public clearFather(): void {
    this.fatherNameSubstringFormControl.reset();
    this.fatherFormControl.reset();
  }

  public fatherNameResultFormatter = (father: HorseDetail) => HorseFormComponent.getNameIfExistsOf(father);

  public searchFather: OperatorFunction<string, readonly HorseDetail[]> = (name$: Observable<string>) =>
    name$.pipe(
      debounceTime(200),
      distinctUntilChanged(),
      switchMap(name => this.searchFathersByNameSubstring(name))
    );

  public searchFathersByNameSubstring(name: string): Observable<HorseDetail[]> {
    return (name === '') ? of([]) :
      this.horseService.search({
        name,
        sex: Sex.male,
        limit: HorseFormComponent.typeAheadMaxOptionCount,
        idOfHorseToBeExcluded: this.idOfHorseBeingEditedElseUndefined,
      });
  }
  // -------------------------------------------------------- END OF FATHER TYPEAHEAD SECTION -----------------------------------

  private requestHorseValues(id: number): void {
    this.horseService.getHorseById(id).subscribe({
      next: horse => {
        this.setFormValues(horse);
      },
      error: error => {
        console.error(error.message);
        const errorMessage = error.status === 0
          ? 'Connection to the server failed.'
          : constructErrorMessageWithList(error);
        this.notification.error(errorMessage, `Could not get horse.`, {enableHtml: true, timeOut: 0});
      }
    });
  }

  private setFormValues(horse: HorseDetail): void {
    this.horseForm.setValue({
      id: horse.id,
      name: horse.name,
      description: horse.description,
      dateOfBirth: horse.dateOfBirth,
      sex: horse.sex,
      ownerFullNameSubstring: HorseFormComponent.getFullNameIfExistsOf(this.ownerFormControl.value),
      owner: horse.owner,
      motherNameSubstring: HorseFormComponent.getNameIfExistsOf(this.motherFormControl.value),
      mother: horse.mother,
      fatherNameSubstring: HorseFormComponent.getNameIfExistsOf(this.fatherFormControl.value),
      father: horse.father,
    });
  }

}
