import {Component, OnInit} from '@angular/core';
import {ToastrService} from 'ngx-toastr';
import {HorseService} from 'src/app/service/horse.service';
import {Horse} from '../../dto/horse';
import {Owner} from '../../dto/owner';
import {AbstractControl, FormBuilder, FormGroup} from '@angular/forms';
import {debounceTime, distinctUntilChanged, Observable} from 'rxjs';
import {constructErrorMessageWithList} from '../../shared/validator';

@Component({
  selector: 'app-horse',
  templateUrl: './horse.component.html',
  styleUrls: ['./horse.component.scss']
})
export class HorseComponent implements OnInit {

  public horseSearchForm: FormGroup = this.formBuilder.group({
    name: [null],
    description: [null],
    dateOfBirth: [null, HorseComponent.noDateInFutureValidator],
    sex: [null],
    ownerFullNameSubstring: [null],
  });

  horses: Horse[] = [];

  constructor(
    private service: HorseService,
    private notification: ToastrService,
    private formBuilder: FormBuilder
  ) { }

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

  private static noDateInFutureValidator(dateControl: AbstractControl) {
    const now: Date = new Date();
    const dateInput: Date = new Date(dateControl.value);
    const dateInFuture = dateInput > now;
    return dateInFuture ? { dateInFuture: true } : null;
  }

  ngOnInit(): void {
    this.searchWithCurrentValues(this.horseSearchForm.value);
    this.horseSearchForm.valueChanges.pipe(debounceTime(500), distinctUntilChanged()).subscribe({
        next: horseSearchFormValues =>   {
          if (this.horseSearchForm.valid) {
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
      ownerFullNameSubstring: formValue.ownerFullNameSubstring?.trim(),
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
        this.service.deleteHorse(horse.id).subscribe({
          next: () => {
            this.notification.success(`Horse ${horse.name} successfully deleted.`);
            this.searchWithCurrentValues(this.horseSearchForm.value);
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
