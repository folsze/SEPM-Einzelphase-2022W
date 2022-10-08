import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Owner } from '../dto/owner';

const baseUri = environment.backendUrl + '/owners';

@Injectable({
  providedIn: 'root'
})
export class OwnerService {

  constructor(
    private http: HttpClient,
  ) { }

  public getAll(): Observable<Owner[]>{
    return this.http.get<Owner[]>(baseUri);
  }

  public searchByFullNameSubstring(fullNameSubstring: string, maxResultCount: number): Observable<Owner[]> {
    const params = new HttpParams()
      .set('fullNameSubstring', fullNameSubstring)
      .set('maxResultCount', maxResultCount);
    return this.http.get<Owner[]>(baseUri, { params });
  }

  public create(owner: Owner): Observable<Owner> {
    return this.http.post<Owner>(baseUri, owner);
  }
}
