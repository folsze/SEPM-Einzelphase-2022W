import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from 'src/environments/environment';
import {Horse, HorseDetail, HorseSearchFilter} from '../dto/horse';

const baseUri = environment.backendUrl + '/horses';

@Injectable({
  providedIn: 'root'
})
export class HorseService {

  constructor(
    private http: HttpClient
  ) {}

  public search(filter: HorseSearchFilter): Observable<Horse[]> {
    let params = new HttpParams();

    if (filter.name) {
      params = params.append('name', filter.name);
    }

    if (filter.description) {
      params = params.append('description', filter.description);
    }

    if (filter.dateOfBirth) {
      params = params.append('dateOfBirth', filter.dateOfBirth.toString());
    }

    if (filter.sex) {
      params = params.append('sex', filter.sex);
    }

    if (filter.ownerFullNameSubstring) {
      params = params.append('ownerFullNameSubstring', filter.ownerFullNameSubstring);
    }

    if (filter.limit) {
      params = params.append('limit', filter.limit);
    }

    if (filter.idOfHorseToBeExcluded) {
      params = params.append('idOfHorseToBeExcluded', filter.idOfHorseToBeExcluded);
    }

    return this.http.get<Horse[]>(baseUri, { params });
  }

  public getHorseById(id: number): Observable<HorseDetail> {
    return this.http.get<HorseDetail>(baseUri + '/' + id);
  }

  /**
   * Create a new horse in the system.
   *
   * @param horse the data for the horse that should be created
   * @return an Observable for the created horse
   */
  public create(horse: HorseDetail): Observable<HorseDetail> {
    return this.http.post<Horse>(
      baseUri,
      horse
    );
  }

  public update(horse: HorseDetail): Observable<HorseDetail> {
    return this.http.put<Horse>(
      baseUri + '/' + horse.id,
      horse
    );
  }

  /**
   * delete horse with id from the system
   *
   * @param id the id of the horse to be deleted
   * @return Observable of the horse that was deleted
   */
  public deleteHorse(id: number): Observable<Horse> {
    return this.http.delete<Horse>(baseUri + '/' + id);
  }
}
