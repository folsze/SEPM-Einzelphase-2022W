import { Component, OnInit } from '@angular/core';
import {Horse} from '../../../dto/horse';
import {HorseService} from '../../../service/horse.service';
import {ActivatedRoute, Router} from '@angular/router';
import {
  ConfirmDeleteModalContentComponent
} from '../../confirm-delete-modal-content/confirm-delete-modal-content.component';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {constructErrorMessageWithList} from '../../../shared/validator';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: 'app-family-tree',
  templateUrl: './family-tree.component.html',
  styleUrls: ['./family-tree.component.scss']
})
export class FamilyTreeComponent implements OnInit {

  public limit: number;
  public limitFromInput: number;

  public id: number;
  public editingLimit = false;

  public familyTreeRoot: Horse;

  constructor(private horseService: HorseService,
              private route: ActivatedRoute,
              private router: Router,
              private modalService: NgbModal,
              private notification: ToastrService
  ) {}

  ngOnInit(): void {
    this.limit = Number(this.route.snapshot.queryParamMap.get('limit'));
    if (!this.limit) {
      this.limit = 100;
    }
    // fetch on prev line necessary since no change detected on initial route move
    this.subscribeToLimitQueryParamChange();
    this.getFamilyTreeData();
  }

  public dateOfBirthAsLocaleDate(horse: Horse): string {
    return new Date(horse.dateOfBirth).toLocaleDateString();
  }

  public openDeleteConfirm(horse: Horse): void {
    const modalRef = this.modalService.open(ConfirmDeleteModalContentComponent);
    modalRef.componentInstance.horse = horse;

    modalRef.result.then((horseWasDeleted: boolean) => {
      if (horseWasDeleted) {
        this.getFamilyTreeData();
      } else {
        console.warn('Horses weren\'t reloaded as the deletion failed.');
      }
    });
  }

  public toEditMode() {
    this.editingLimit = true;
  }

  public onSubmit(newLimit: number): void {
    this.editingLimit = false;
    this.setQueryParamLimit(newLimit);
  }

  public getFamilyTreeData(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    const limit = Number(this.route.snapshot.queryParamMap.get('limit'));
    this.horseService.getFamilyTree(id, limit).subscribe({
      next: (familyTreeHorse: Horse) => this.familyTreeRoot = familyTreeHorse,
      error: (error) => {
        console.error('Error while getting family tree', error);
        const errorMessage = error.status === 0
          ? 'Connection to the server failed.'
          : constructErrorMessageWithList(error);
        this.notification.error(errorMessage, `Error while getting family tree`, {enableHtml: true, timeOut: 0});
      }
    });
  }

  private subscribeToLimitQueryParamChange(){
    this.route.queryParamMap.subscribe(
      (params) => {
        if (params.has('limit')) {
          this.limit = Number(this.route.snapshot.queryParamMap.get('limit'));
          this.getFamilyTreeData();
        }
      });
  }


  private setQueryParamLimit(newLimit: number): void {
    this.router.navigate(
      [],
      {
        relativeTo: this.route,
        queryParams: {limit: newLimit}
      }
    );
  }
}
