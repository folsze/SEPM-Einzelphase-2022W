import {Component} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-confirm-delete-modal-content',
  templateUrl: './confirm-delete-modal-content.component.html',
  styleUrls: ['./confirm-delete-modal-content.component.scss']
})
export class ConfirmDeleteModalContentComponent {
  public horseName: string;

  constructor(public modal: NgbActiveModal) { }
}
