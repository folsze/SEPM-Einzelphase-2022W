import {Component} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {Horse} from '../../dto/horse';
import {constructErrorMessageWithList} from '../../shared/validator';
import {HorseService} from '../../service/horse.service';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: 'app-confirm-delete-modal-content',
  templateUrl: './confirm-delete-modal-content.component.html',
  styleUrls: ['./confirm-delete-modal-content.component.scss']
})
export class ConfirmDeleteModalContentComponent {
  public horse: Horse;

  constructor(
    public modal: NgbActiveModal,
    private service: HorseService,
    private notification: ToastrService
  ) {}

  public deleteHorseAndCloseModalIfDeleteWorked(): void {
    if (this.horse.id) {
      this.service.deleteHorse(this.horse.id).subscribe({
        next: () => {
          this.notification.success(`Horse ${this.horse.name} successfully deleted.`);
          const horseWasDeleted = true;
          this.modal.close(horseWasDeleted);
        },
        error: (error) => {
          console.error('Error deleting horse', error);
          const errorMessage = error.status === 0
            ? 'Connection to the server failed.'
            : constructErrorMessageWithList(error);
          this.notification.error(errorMessage, `Could not delete horse ${this.horse.name}.`, {enableHtml: true, timeOut: 0});
        }
      });
    } else {
      console.error('Horse to be deleted doesn\'t exist.');
    }
  }
}
