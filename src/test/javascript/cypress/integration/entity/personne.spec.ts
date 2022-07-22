import { entityItemSelector } from '../../support/commands';
import {
  entityTableSelector,
  entityDetailsButtonSelector,
  entityDetailsBackButtonSelector,
  entityCreateButtonSelector,
  entityCreateSaveButtonSelector,
  entityCreateCancelButtonSelector,
  entityEditButtonSelector,
  entityDeleteButtonSelector,
  entityConfirmDeleteButtonSelector,
} from '../../support/entity';

describe('Personne e2e test', () => {
  const personnePageUrl = '/personne';
  const personnePageUrlPattern = new RegExp('/personne(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const personneSample = { nom: 'Movies a auxiliary', telephone: 11 };

  let personne: any;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/personnes+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/personnes').as('postEntityRequest');
    cy.intercept('DELETE', '/api/personnes/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (personne) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/personnes/${personne.id}`,
      }).then(() => {
        personne = undefined;
      });
    }
  });

  it('Personnes menu should load Personnes page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('personne');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response!.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Personne').should('exist');
    cy.url().should('match', personnePageUrlPattern);
  });

  describe('Personne page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(personnePageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Personne page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/personne/new$'));
        cy.getEntityCreateUpdateHeading('Personne');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response!.statusCode).to.equal(200);
        });
        cy.url().should('match', personnePageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/personnes',
          body: personneSample,
        }).then(({ body }) => {
          personne = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/personnes+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/personnes?page=0&size=20>; rel="last",<http://localhost/api/personnes?page=0&size=20>; rel="first"',
              },
              body: [personne],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(personnePageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Personne page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('personne');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response!.statusCode).to.equal(200);
        });
        cy.url().should('match', personnePageUrlPattern);
      });

      it('edit button click should load edit Personne page', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Personne');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response!.statusCode).to.equal(200);
        });
        cy.url().should('match', personnePageUrlPattern);
      });

      it('last delete button click should delete instance of Personne', () => {
        cy.get(entityDeleteButtonSelector).last().click();
        cy.getEntityDeleteDialogHeading('personne').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response!.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response!.statusCode).to.equal(200);
        });
        cy.url().should('match', personnePageUrlPattern);

        personne = undefined;
      });
    });
  });

  describe('new Personne page', () => {
    beforeEach(() => {
      cy.visit(`${personnePageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Personne');
    });

    it('should create an instance of Personne', () => {
      cy.get(`[data-cy="nom"]`).type('Loan').should('have.value', 'Loan');

      cy.get(`[data-cy="telephone"]`).type('10').should('have.value', '10');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response!.statusCode).to.equal(201);
        personne = response!.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response!.statusCode).to.equal(200);
      });
      cy.url().should('match', personnePageUrlPattern);
    });
  });
});
