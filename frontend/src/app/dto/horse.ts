import {Owner} from './owner';
import {Sex} from './sex';

export interface Horse {
  id?: number;
  name: string;
  description?: string;
  dateOfBirth: Date;
  sex: Sex;
  owner?: Owner;
}


export interface HorseSearchFilter {
  name?: string;
  description?: string;
  dateOfBirth?: Date;
  sex?: Sex;
  ownerFullNameSubstringFormControl?: string;
  limit?: number;
}
