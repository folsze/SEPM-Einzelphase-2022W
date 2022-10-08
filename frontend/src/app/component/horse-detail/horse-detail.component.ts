import { Component, OnInit } from '@angular/core';
import {Horse} from '../../dto/horse';
import {HorseService} from '../../service/horse.service';
import {OwnerService} from '../../service/owner.service';
import {ActivatedRoute} from '@angular/router';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: 'app-horse-detail',
  templateUrl: './horse-detail.component.html',
  styleUrls: ['./horse-detail.component.scss']
})
export class HorseDetailComponent implements OnInit {

  public horse: Horse;

  public motherName: string;

  selectedId: number;

  constructor(
    private horseService: HorseService,
    private ownerService: OwnerService,
    private route: ActivatedRoute,
    private notification: ToastrService,
  ) { } // todo: messageService

  public get ownerFullName(): string {
    if (this.horse.owner) {
      return this.horse.owner.firstName + ' ' + this.horse.owner.lastName;
    } else {
      return 'No owner set for this horse';
    }
  }

  ngOnInit() {
    this.route.paramMap.subscribe(paramMap => {
      this.getHorse(Number(paramMap.get('id')));
    });
  }

  // public deleteHorse(id: number) {
  //   this.horseService.delete(id).subscribe(
  //     response => console.log('deleted horse', response),
  //     error => this.handleError(error)
  //   );
  // }

  private getHorse(id: number): void {
    this.horseService.getHorseById(id).subscribe({
      next: data => {
        this.horse = data;
        // if (this.horse.mother?.id != null) {
        //   this.getMotherName();
        // }
        // if (this.horse.father?.id != null) {
        //   this.getFatherName();
        // }
      },
      // error: error => this.handleError(error)
    });
  }

  // private getMotherName() {
  //   this.horseService.getHorse(this.horse.motherId).subscribe({
  //     next: motherHorse => {
  //       this.motherName = motherHorse.name;
  //     },
  //     error: error => this.handleError(error)
  //   });
  // }
  //
  // private getFatherName() {
  //   this.horseService.getHorse(this.horse.fatherId).subscribe({
  //     next: fatherHorse => {
  //       this.fatherName = fatherHorse.name;
  //     },
  //     error: error => this.handleError(error)
  //   });
  // }
}
