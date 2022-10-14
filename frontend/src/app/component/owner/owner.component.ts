import { Component, OnInit } from '@angular/core';
import {Horse} from '../../dto/horse';
import {ToastrService} from 'ngx-toastr';
import {Owner} from '../../dto/owner';
import {OwnerService} from '../../service/owner.service';
import {constructErrorMessageWithList} from '../../shared/validator';

@Component({
  selector: 'app-owner',
  templateUrl: './owner.component.html',
  styleUrls: ['./owner.component.scss']
})
export class OwnerComponent implements OnInit {

  search = false;
  owners: Owner[] = [];

  constructor(
    private service: OwnerService,
    private notification: ToastrService,
  ) { }

  ngOnInit(): void {
    this.reloadOwners();
  }

  reloadOwners() {
    this.service.getAll()
      .subscribe({
        next: data => {
          this.owners = data;
        },
        error: error => {
          console.error('Error fetching owners', error);
          const errorMessage = error.status === 0
            ? 'Connection to the server failed.'
            : constructErrorMessageWithList(error);
          this.notification.error(errorMessage, 'Could Not Fetch Owners', {enableHtml: true, timeOut: 0});
        }
      });
  }

  ownerName(owner: Owner | null): string {
    return owner
      ? `${owner.firstName} ${owner.lastName}`
      : '';
  }

  dateOfBirthAsLocaleDate(horse: Horse): string {
    return new Date(horse.dateOfBirth).toLocaleDateString();
  }

}
