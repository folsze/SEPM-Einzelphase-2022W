import { Component, OnInit } from '@angular/core';
import {Horse} from '../../../dto/horse';
import {HorseService} from '../../../service/horse.service';
import {ActivatedRoute, Router} from '@angular/router';

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

  public toEditMode() {
    this.editingLimit = true;
  }

  public onSubmit(newLimit: number): void {
    this.editingLimit = false;
    this.setQueryParamLimit(newLimit);
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

  private getFamilyTreeData(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    const limit = Number(this.route.snapshot.queryParamMap.get('limit'));
    this.horseService.getFamilyTree(id, limit).subscribe({
      next: (familyTreeHorse: Horse) => this.familyTreeRoot = familyTreeHorse,
      error: (error: any) => console.error('NOT IMPLEMENTED YET')
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
