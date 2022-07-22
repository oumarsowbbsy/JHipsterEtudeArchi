import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { IPersonne, Personne } from '../personne.model';

import { PersonneService } from './personne.service';

describe('Personne Service', () => {
  let service: PersonneService;
  let httpMock: HttpTestingController;
  let elemDefault: IPersonne;
  let expectedResult: IPersonne | IPersonne[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(PersonneService);
    httpMock = TestBed.inject(HttpTestingController);

    elemDefault = {
      id: 'AAAAAAA',
      prenom: 'AAAAAAA',
      nom: 'AAAAAAA',
      telephone: 0,
    };
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = Object.assign({}, elemDefault);

      service.find('ABC').subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(elemDefault);
    });

    it('should create a Personne', () => {
      const returnedFromService = Object.assign(
        {
          id: 'ID',
        },
        elemDefault
      );

      const expected = Object.assign({}, returnedFromService);

      service.create(new Personne()).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Personne', () => {
      const returnedFromService = Object.assign(
        {
          id: 'BBBBBB',
          prenom: 'BBBBBB',
          nom: 'BBBBBB',
          telephone: 1,
        },
        elemDefault
      );

      const expected = Object.assign({}, returnedFromService);

      service.update(expected).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Personne', () => {
      const patchObject = Object.assign(
        {
          prenom: 'BBBBBB',
        },
        new Personne()
      );

      const returnedFromService = Object.assign(patchObject, elemDefault);

      const expected = Object.assign({}, returnedFromService);

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Personne', () => {
      const returnedFromService = Object.assign(
        {
          id: 'BBBBBB',
          prenom: 'BBBBBB',
          nom: 'BBBBBB',
          telephone: 1,
        },
        elemDefault
      );

      const expected = Object.assign({}, returnedFromService);

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toContainEqual(expected);
    });

    it('should delete a Personne', () => {
      service.delete('ABC').subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult);
    });

    describe('addPersonneToCollectionIfMissing', () => {
      it('should add a Personne to an empty array', () => {
        const personne: IPersonne = { id: 'ABC' };
        expectedResult = service.addPersonneToCollectionIfMissing([], personne);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(personne);
      });

      it('should not add a Personne to an array that contains it', () => {
        const personne: IPersonne = { id: 'ABC' };
        const personneCollection: IPersonne[] = [
          {
            ...personne,
          },
          { id: 'CBA' },
        ];
        expectedResult = service.addPersonneToCollectionIfMissing(personneCollection, personne);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Personne to an array that doesn't contain it", () => {
        const personne: IPersonne = { id: 'ABC' };
        const personneCollection: IPersonne[] = [{ id: 'CBA' }];
        expectedResult = service.addPersonneToCollectionIfMissing(personneCollection, personne);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(personne);
      });

      it('should add only unique Personne to an array', () => {
        const personneArray: IPersonne[] = [{ id: 'ABC' }, { id: 'CBA' }, { id: 'e3b182c2-da7d-419d-96dc-ba0bbdaad529' }];
        const personneCollection: IPersonne[] = [{ id: 'ABC' }];
        expectedResult = service.addPersonneToCollectionIfMissing(personneCollection, ...personneArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const personne: IPersonne = { id: 'ABC' };
        const personne2: IPersonne = { id: 'CBA' };
        expectedResult = service.addPersonneToCollectionIfMissing([], personne, personne2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(personne);
        expect(expectedResult).toContain(personne2);
      });

      it('should accept null and undefined values', () => {
        const personne: IPersonne = { id: 'ABC' };
        expectedResult = service.addPersonneToCollectionIfMissing([], null, personne, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(personne);
      });

      it('should return initial array if no Personne is added', () => {
        const personneCollection: IPersonne[] = [{ id: 'ABC' }];
        expectedResult = service.addPersonneToCollectionIfMissing(personneCollection, undefined, null);
        expect(expectedResult).toEqual(personneCollection);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
