import {Component, OnInit} from '@angular/core';
import {ToastrService} from 'ngx-toastr';
import {HorseService} from 'src/app/service/horse.service';
import {Horse} from '../../dto/horse';
import {Owner} from '../../dto/owner';
import {AbstractControl, FormBuilder, FormGroup} from '@angular/forms';
import {debounceTime, distinctUntilChanged, Observable, of, OperatorFunction, switchMap} from 'rxjs';
import {constructErrorMessageWithList} from '../../shared/validator';
import {OwnerService} from '../../service/owner.service';

@Component({
  selector: 'app-horse',
  templateUrl: './horse.component.html',
  styleUrls: ['./horse.component.scss']
})
export class HorseComponent implements OnInit {

  private static readonly typeAheadMaxOptionCount = 5;

  public form: FormGroup = this.formBuilder.group({
    name: [null],
    description: [null],
    dateOfBirth: [null, HorseComponent.noDateInFutureValidator],
    sex: [null],
    owner: [null],
    ownerFullNameSubstring: [''],
  });

  horses: Horse[] = [];

  constructor(
    private service: HorseService,
    private notification: ToastrService,
    private formBuilder: FormBuilder,
    private ownerService: OwnerService
  ) { }

  public get nameFormControl(): AbstractControl {
    return this.form.controls.name;
  }

  public get descriptionFormControl(): AbstractControl {
    return this.form.controls.description;
  }

  public get dateOfBirthFormControl(): AbstractControl {
    return this.form.controls.dateOfBirth;
  }

  public get sexFormControl(): AbstractControl {
    return this.form.controls.sex;
  }

  public get ownerFormControl(): AbstractControl {
    return this.form.controls.owner;
  }

  public get ownerFullNameSubstringFormControl(): AbstractControl {
    return this.form.controls.ownerFullNameSubstring;
  }

  private static noDateInFutureValidator(dateControl: AbstractControl) {
    const now: Date = new Date();
    const dateInput: Date = new Date(dateControl.value);
    const dateInFuture = dateInput > now;
    return dateInFuture ? { dateInFuture: true } : null;
  }

  ngOnInit(): void {
    this.searchWithCurrentValues(this.form.value);
    this.form.valueChanges.pipe(debounceTime(500), distinctUntilChanged()).subscribe({
        next: horseSearchFormValues =>   {
          if (this.form.valid) {
            this.searchWithCurrentValues(horseSearchFormValues);
          }
        }
    });
  }

  public searchWithCurrentValues(formValue: any) {
    const horses: Observable<Horse[]> = this.service.search({
      name: formValue.name?.trim(),
      description: formValue.description?.trim(),
      dateOfBirth: formValue.dateOfBirth,
      sex: formValue.sex,
      ownerId: formValue.owner?.id,
    });

    horses.subscribe({
      next: data => this.horses = data,
      error: error => {
        console.error('Error fetching horses', error);
        const errorMessage = error.status === 0
          ? 'Connection to the server failed.'
          : constructErrorMessageWithList(error);
        this.notification.error(errorMessage, 'Could Not Fetch Horses', { enableHtml: true, timeOut: 0 });
      }
    });
  }

  // -------------------------------------------------------- START OF OWNER TYPEAHEAD SECTION -----------------------------------
  private static getFullNameIfExistsOf(owner: Owner): string {
    return owner ? owner.firstName + ' ' + owner.lastName : '';
  }

  public get currentOwnerFullName(): string {
    return this.ownerFormControl.value ?
      this.ownerFormControl.value.firstName + ' ' + this.ownerFormControl.value.lastName :
      '';
  }

  public ownerFullName(owner: Owner): string {
    return owner ?
      owner.firstName + ' ' + owner.lastName :
      '';
  }

  public setOwner(owner: Owner) {
    this.form.controls.owner.setValue(owner);
  }

  public clearOwner(): void {
    this.ownerFullNameSubstringFormControl.reset();
    this.ownerFormControl.reset();
  }

  public ownerFullNameResultFormatter = (owner: Owner) => HorseComponent.getFullNameIfExistsOf(owner);

  public searchOwner: OperatorFunction<string, readonly Owner[]> = (fullName$: Observable<string>) =>
    fullName$.pipe(
      debounceTime(200),
      distinctUntilChanged(),
      switchMap(fullName => this.searchOwnersByFullName(fullName))
    );

  public searchOwnersByFullName(fullName: string): Observable<Owner[]> {
    return (fullName === '') ? of([]) :
      this.ownerService.searchByFullNameSubstring(fullName.trim(),
        HorseComponent.typeAheadMaxOptionCount);
  }
  // -------------------------------------------------------- END OF OWNER TYPEAHEAD SECTION -----------------------------------

  public dateOfBirthAsLocaleDate(horse: Horse): string {
    return new Date(horse.dateOfBirth).toLocaleDateString();
  }

  public deleteIfUserConfirms(horse: Horse): void {
    if (horse.id) {
      if (confirm(`Are you sure you want to delete the horse \"${horse.name}\"`)) {
        this.service.deleteHorse(horse.id).subscribe({
          next: () => {
            this.notification.success(`Horse ${horse.name} successfully deleted.`);
            this.searchWithCurrentValues(this.form.value);
          },
          error: (error) => {
            console.error('Error fetching owners', error);
            const errorMessage = error.status === 0
              ? 'Connection to the server failed.'
              : constructErrorMessageWithList(error);
            this.notification.error(errorMessage, `Could not delete horse ${horse.name}.`, {enableHtml: true, timeOut: 0});
          }
        });
      }
    }
  }
}
