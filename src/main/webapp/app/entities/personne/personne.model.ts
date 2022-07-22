export interface IPersonne {
  id?: string;
  prenom?: string;
  nom?: string;
  telephone?: number;
}

export class Personne implements IPersonne {
  constructor(public id?: string, public prenom?: string, public nom?: string, public telephone?: number) {}
}

export function getPersonneIdentifier(personne: IPersonne): string | undefined {
  return personne.id;
}
