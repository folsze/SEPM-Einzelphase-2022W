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

export interface HorseDetail {
  id?: number;
  name: string;
  description?: string;
  dateOfBirth: Date;
  sex: Sex;
  owner?: Owner;
  mother?: HorseMinimal;
  father?: HorseMinimal;
}

interface HorseMinimal {
  id: number;
  name: string;
  dateOfBirth: Date;
  sex: Sex;
}


export interface HorseSearchFilter {
  name?: string;
  description?: string;
  dateOfBirth?: Date;
  sex?: Sex;
  ownerFullNameSubstring?: string;
  fatherNameSubstring?: string;
  limit?: number;
  idOfHorseToBeExcluded?: number;
}
