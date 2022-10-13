import {Component, OnInit} from '@angular/core';
import {ToastrService} from 'ngx-toastr';
import {HorseService} from 'src/app/service/horse.service';
import {Horse} from '../../dto/horse';
import {Owner} from '../../dto/owner';
import {AbstractControl, FormBuilder, FormGroup} from '@angular/forms';
import {debounceTime, distinctUntilChanged, Observable} from 'rxjs';

@Component({
  selector: 'app-horse',
  templateUrl: './horse.component.html',
  styleUrls: ['./horse.component.scss']
})
export class HorseComponent implements OnInit {

  public horseSearchForm: FormGroup = this.formBuilder.group({
    name: [null, [this.noWhitespaceInsideValidator]],
    description: [null],
    dateOfBirth: [null, [this.noDateInFutureValidator]],
    sex: [null],
    ownerFullNameSubstring: [null],
  });

  horses: Horse[] = [];
  bannerError: string | null = null;

  constructor(
    private service: HorseService,
    private notification: ToastrService,
    private formBuilder: FormBuilder
  ) { }

  // GETTERS:
  public get nameFormControl(): AbstractControl {
    return this.horseSearchForm.controls.name;
  }

  public get descriptionFormControl(): AbstractControl {
    return this.horseSearchForm.controls.description;
  }

  public get dateOfBirthFormControl(): AbstractControl {
    return this.horseSearchForm.controls.dateOfBirth;
  }

  public get sexFormControl(): AbstractControl {
    return this.horseSearchForm.controls.sex;
  }

  public get ownerFullNameSubstringFormControl(): AbstractControl {
    return this.horseSearchForm.controls.ownerFullNameSubstring;
  }

  ngOnInit(): void {
    this.searchWithCurrentValues(this.horseSearchForm.value);

    this.horseSearchForm.valueChanges.pipe(debounceTime(500), distinctUntilChanged()).subscribe({
        next: horseSearchForm =>   {
          this.searchWithCurrentValues(horseSearchForm);
        }
    });
  }

  public searchWithCurrentValues(formValue: any) { // todo Fragestunde: welchen Typ hat das? "object=any ist ok?", "an object with a key-value pair for each member of the group"
    const horses: Observable<Horse[]> = this.service.search({
      name: formValue.name?.trim(),
      description: formValue.description?.trim(),
      dateOfBirth: formValue.dateOfBirth,
      sex: formValue.sex,
      ownerFullNameSubstring: formValue.ownerFullNameSubstring?.trim(),
    });

    horses.subscribe({
      next: data => this.horses = data,
      error: error => {
        console.error('Error fetching horses', error);
        this.bannerError = 'Could not fetch horses: ' + error.message;

        const errorMessage = error.status === 0
          ? 'Is the backend up?'
          : HorseComponent.constructErrorMessage(error);

        this.notification.error(errorMessage, 'Could Not Fetch Horses', { enableHtml: true });
      }
    });
  }

  private static constructErrorMessage(error: any): string {
    let errorMessage = error.error.message + '.<br><br>';

    if (error.error.errors?.length > 0) {
      errorMessage += '<ul>';
      for (const message of error.error.errors) {
        errorMessage += '<li>' + message + '</li>';
      }
      errorMessage += '</ul>';
    }

    return errorMessage;
  }

  public ownerName(owner: Owner | null): string {
    return owner
      ? `${owner.firstName} ${owner.lastName}`
      : '';
  }

  public dateOfBirthAsLocaleDate(horse: Horse): string {
    return new Date(horse.dateOfBirth).toLocaleDateString();
  }

  public deleteIfUserConfirms(horse: Horse): void {
    if (horse.id) {
      if (confirm(`Are you sure you want to delete the horse \"${horse.name}\"`)) {
        this.service.deleteHorse(horse.id).subscribe(
          () => {
            this.notification.success(`Horse ${horse.name} successfully deleted.`);
            this.searchWithCurrentValues(this.horseSearchForm.value);
          }
        );
      }
    }
  }

  private noWhitespaceInsideValidator(control: AbstractControl) { // todo: private?
    const containsWhitespace = (control.value || '').trim().match(/\s/g);
    const isValid = !containsWhitespace;
    return isValid ? null : { whitespace: true };
  }

  private noDateInFutureValidator(dateControl: AbstractControl) {
    const now: Date = new Date();
    const isValid = !(dateControl.value < now);
    return isValid ? null : { dateInFuture: true };
  }
}
