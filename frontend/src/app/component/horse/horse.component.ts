import {Component, OnInit} from '@angular/core';
import {ToastrService} from 'ngx-toastr';
import {HorseService} from 'src/app/service/horse.service';
import {Horse} from '../../dto/horse';
import {Owner} from '../../dto/owner';
import {AbstractControl, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Observable} from 'rxjs';

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
    this.reloadHorses();
  }

  public onSubmit() {

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
            this.reloadHorses();
          }
        );
      }
    }
  }

  public reloadHorses(): void {
    const horses: Observable<Horse[]> = this.service.search({
      name: this.nameFormControl.value?.trim(),
      description: this.descriptionFormControl.value?.trim(),
      dateOfBirth: this.dateOfBirthFormControl.value,
      sex: this.sexFormControl.value,
      ownerFullNameSubstringFormControl: this.ownerFullNameSubstringFormControl.value?.trim(),
    });

    horses.subscribe({
        next: data => {
          this.horses = data;
        },
        error: error => {
          console.error('Error fetching horses', error);
          this.bannerError = 'Could not fetch horses: ' + error.message;
          const errorMessage = error.status === 0
            ? 'Is the backend up?'
            : error.message.message;
          this.notification.error(errorMessage, 'Could Not Fetch Horses');
        }
      });
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
