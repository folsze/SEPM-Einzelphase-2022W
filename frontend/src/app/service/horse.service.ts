import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from 'src/environments/environment';
import {Horse} from '../dto/horse';

const baseUri = environment.backendUrl + '/horses';

@Injectable({
  providedIn: 'root'
})
export class HorseService {

  constructor(
    private http: HttpClient,
  ) { }

  /**
   * Get all horses stored in the system
   *
   * @return observable list of found horses.
   */
  public getAll(): Observable<Horse[]> {
    return this.http.get<Horse[]>(baseUri);
  }

  public getHorseById(id: number): Observable<Horse> {
    return this.http.get<Horse>(baseUri + '/' + id);
  }

  /**
   * Create a new horse in the system.
   *
   * @param horse the data for the horse that should be created
   * @return an Observable for the created horse
   */
  public create(horse: Horse): Observable<Horse> {
    return this.http.post<Horse>(
      baseUri,
      horse
    );
  }

  public update(horse: Horse): Observable<Horse> {
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
