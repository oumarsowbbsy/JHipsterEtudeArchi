package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Personne;
import com.mycompany.myapp.repository.PersonneRepository;
import com.mycompany.myapp.repository.search.PersonneSearchRepository;
import com.mycompany.myapp.service.dto.PersonneDTO;
import com.mycompany.myapp.service.mapper.PersonneMapper;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Integration tests for the {@link PersonneResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class PersonneResourceIT {

    private static final String DEFAULT_PRENOM = "AAAAAAAAAA";
    private static final String UPDATED_PRENOM = "BBBBBBBBBB";

    private static final String DEFAULT_NOM = "AAAAAAAAAA";
    private static final String UPDATED_NOM = "BBBBBBBBBB";

    private static final Integer DEFAULT_TELEPHONE = 12;
    private static final Integer UPDATED_TELEPHONE = 11;

    private static final String ENTITY_API_URL = "/api/personnes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/personnes";

    @Autowired
    private PersonneRepository personneRepository;

    @Autowired
    private PersonneMapper personneMapper;

    /**
     * This repository is mocked in the com.mycompany.myapp.repository.search test package.
     *
     * @see com.mycompany.myapp.repository.search.PersonneSearchRepositoryMockConfiguration
     */
    @Autowired
    private PersonneSearchRepository mockPersonneSearchRepository;

    @Autowired
    private WebTestClient webTestClient;

    private Personne personne;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Personne createEntity() {
        Personne personne = new Personne().prenom(DEFAULT_PRENOM).nom(DEFAULT_NOM).telephone(DEFAULT_TELEPHONE);
        return personne;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Personne createUpdatedEntity() {
        Personne personne = new Personne().prenom(UPDATED_PRENOM).nom(UPDATED_NOM).telephone(UPDATED_TELEPHONE);
        return personne;
    }

    @BeforeEach
    public void initTest() {
        personneRepository.deleteAll().block();
        personne = createEntity();
    }

    @Test
    void createPersonne() throws Exception {
        int databaseSizeBeforeCreate = personneRepository.findAll().collectList().block().size();
        // Configure the mock search repository
        when(mockPersonneSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Create the Personne
        PersonneDTO personneDTO = personneMapper.toDto(personne);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(personneDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Personne in the database
        List<Personne> personneList = personneRepository.findAll().collectList().block();
        assertThat(personneList).hasSize(databaseSizeBeforeCreate + 1);
        Personne testPersonne = personneList.get(personneList.size() - 1);
        assertThat(testPersonne.getPrenom()).isEqualTo(DEFAULT_PRENOM);
        assertThat(testPersonne.getNom()).isEqualTo(DEFAULT_NOM);
        assertThat(testPersonne.getTelephone()).isEqualTo(DEFAULT_TELEPHONE);

        // Validate the Personne in Elasticsearch
        verify(mockPersonneSearchRepository, times(1)).save(testPersonne);
    }

    @Test
    void createPersonneWithExistingId() throws Exception {
        // Create the Personne with an existing ID
        personne.setId("existing_id");
        PersonneDTO personneDTO = personneMapper.toDto(personne);

        int databaseSizeBeforeCreate = personneRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(personneDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Personne in the database
        List<Personne> personneList = personneRepository.findAll().collectList().block();
        assertThat(personneList).hasSize(databaseSizeBeforeCreate);

        // Validate the Personne in Elasticsearch
        verify(mockPersonneSearchRepository, times(0)).save(personne);
    }

    @Test
    void checkPrenomIsRequired() throws Exception {
        int databaseSizeBeforeTest = personneRepository.findAll().collectList().block().size();
        // set the field null
        personne.setPrenom(null);

        // Create the Personne, which fails.
        PersonneDTO personneDTO = personneMapper.toDto(personne);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(personneDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Personne> personneList = personneRepository.findAll().collectList().block();
        assertThat(personneList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkNomIsRequired() throws Exception {
        int databaseSizeBeforeTest = personneRepository.findAll().collectList().block().size();
        // set the field null
        personne.setNom(null);

        // Create the Personne, which fails.
        PersonneDTO personneDTO = personneMapper.toDto(personne);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(personneDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Personne> personneList = personneRepository.findAll().collectList().block();
        assertThat(personneList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkTelephoneIsRequired() throws Exception {
        int databaseSizeBeforeTest = personneRepository.findAll().collectList().block().size();
        // set the field null
        personne.setTelephone(null);

        // Create the Personne, which fails.
        PersonneDTO personneDTO = personneMapper.toDto(personne);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(personneDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Personne> personneList = personneRepository.findAll().collectList().block();
        assertThat(personneList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllPersonnes() {
        // Initialize the database
        personneRepository.save(personne).block();

        // Get all the personneList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(personne.getId()))
            .jsonPath("$.[*].prenom")
            .value(hasItem(DEFAULT_PRENOM))
            .jsonPath("$.[*].nom")
            .value(hasItem(DEFAULT_NOM))
            .jsonPath("$.[*].telephone")
            .value(hasItem(DEFAULT_TELEPHONE));
    }

    @Test
    void getPersonne() {
        // Initialize the database
        personneRepository.save(personne).block();

        // Get the personne
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, personne.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(personne.getId()))
            .jsonPath("$.prenom")
            .value(is(DEFAULT_PRENOM))
            .jsonPath("$.nom")
            .value(is(DEFAULT_NOM))
            .jsonPath("$.telephone")
            .value(is(DEFAULT_TELEPHONE));
    }

    @Test
    void getNonExistingPersonne() {
        // Get the personne
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewPersonne() throws Exception {
        // Configure the mock search repository
        when(mockPersonneSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        personneRepository.save(personne).block();

        int databaseSizeBeforeUpdate = personneRepository.findAll().collectList().block().size();

        // Update the personne
        Personne updatedPersonne = personneRepository.findById(personne.getId()).block();
        updatedPersonne.prenom(UPDATED_PRENOM).nom(UPDATED_NOM).telephone(UPDATED_TELEPHONE);
        PersonneDTO personneDTO = personneMapper.toDto(updatedPersonne);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, personneDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(personneDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Personne in the database
        List<Personne> personneList = personneRepository.findAll().collectList().block();
        assertThat(personneList).hasSize(databaseSizeBeforeUpdate);
        Personne testPersonne = personneList.get(personneList.size() - 1);
        assertThat(testPersonne.getPrenom()).isEqualTo(UPDATED_PRENOM);
        assertThat(testPersonne.getNom()).isEqualTo(UPDATED_NOM);
        assertThat(testPersonne.getTelephone()).isEqualTo(UPDATED_TELEPHONE);

        // Validate the Personne in Elasticsearch
        verify(mockPersonneSearchRepository).save(testPersonne);
    }

    @Test
    void putNonExistingPersonne() throws Exception {
        int databaseSizeBeforeUpdate = personneRepository.findAll().collectList().block().size();
        personne.setId(UUID.randomUUID().toString());

        // Create the Personne
        PersonneDTO personneDTO = personneMapper.toDto(personne);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, personneDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(personneDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Personne in the database
        List<Personne> personneList = personneRepository.findAll().collectList().block();
        assertThat(personneList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Personne in Elasticsearch
        verify(mockPersonneSearchRepository, times(0)).save(personne);
    }

    @Test
    void putWithIdMismatchPersonne() throws Exception {
        int databaseSizeBeforeUpdate = personneRepository.findAll().collectList().block().size();
        personne.setId(UUID.randomUUID().toString());

        // Create the Personne
        PersonneDTO personneDTO = personneMapper.toDto(personne);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(personneDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Personne in the database
        List<Personne> personneList = personneRepository.findAll().collectList().block();
        assertThat(personneList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Personne in Elasticsearch
        verify(mockPersonneSearchRepository, times(0)).save(personne);
    }

    @Test
    void putWithMissingIdPathParamPersonne() throws Exception {
        int databaseSizeBeforeUpdate = personneRepository.findAll().collectList().block().size();
        personne.setId(UUID.randomUUID().toString());

        // Create the Personne
        PersonneDTO personneDTO = personneMapper.toDto(personne);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(personneDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Personne in the database
        List<Personne> personneList = personneRepository.findAll().collectList().block();
        assertThat(personneList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Personne in Elasticsearch
        verify(mockPersonneSearchRepository, times(0)).save(personne);
    }

    @Test
    void partialUpdatePersonneWithPatch() throws Exception {
        // Initialize the database
        personneRepository.save(personne).block();

        int databaseSizeBeforeUpdate = personneRepository.findAll().collectList().block().size();

        // Update the personne using partial update
        Personne partialUpdatedPersonne = new Personne();
        partialUpdatedPersonne.setId(personne.getId());

        partialUpdatedPersonne.prenom(UPDATED_PRENOM);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedPersonne.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedPersonne))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Personne in the database
        List<Personne> personneList = personneRepository.findAll().collectList().block();
        assertThat(personneList).hasSize(databaseSizeBeforeUpdate);
        Personne testPersonne = personneList.get(personneList.size() - 1);
        assertThat(testPersonne.getPrenom()).isEqualTo(UPDATED_PRENOM);
        assertThat(testPersonne.getNom()).isEqualTo(DEFAULT_NOM);
        assertThat(testPersonne.getTelephone()).isEqualTo(DEFAULT_TELEPHONE);
    }

    @Test
    void fullUpdatePersonneWithPatch() throws Exception {
        // Initialize the database
        personneRepository.save(personne).block();

        int databaseSizeBeforeUpdate = personneRepository.findAll().collectList().block().size();

        // Update the personne using partial update
        Personne partialUpdatedPersonne = new Personne();
        partialUpdatedPersonne.setId(personne.getId());

        partialUpdatedPersonne.prenom(UPDATED_PRENOM).nom(UPDATED_NOM).telephone(UPDATED_TELEPHONE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedPersonne.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedPersonne))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Personne in the database
        List<Personne> personneList = personneRepository.findAll().collectList().block();
        assertThat(personneList).hasSize(databaseSizeBeforeUpdate);
        Personne testPersonne = personneList.get(personneList.size() - 1);
        assertThat(testPersonne.getPrenom()).isEqualTo(UPDATED_PRENOM);
        assertThat(testPersonne.getNom()).isEqualTo(UPDATED_NOM);
        assertThat(testPersonne.getTelephone()).isEqualTo(UPDATED_TELEPHONE);
    }

    @Test
    void patchNonExistingPersonne() throws Exception {
        int databaseSizeBeforeUpdate = personneRepository.findAll().collectList().block().size();
        personne.setId(UUID.randomUUID().toString());

        // Create the Personne
        PersonneDTO personneDTO = personneMapper.toDto(personne);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, personneDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(personneDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Personne in the database
        List<Personne> personneList = personneRepository.findAll().collectList().block();
        assertThat(personneList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Personne in Elasticsearch
        verify(mockPersonneSearchRepository, times(0)).save(personne);
    }

    @Test
    void patchWithIdMismatchPersonne() throws Exception {
        int databaseSizeBeforeUpdate = personneRepository.findAll().collectList().block().size();
        personne.setId(UUID.randomUUID().toString());

        // Create the Personne
        PersonneDTO personneDTO = personneMapper.toDto(personne);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(personneDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Personne in the database
        List<Personne> personneList = personneRepository.findAll().collectList().block();
        assertThat(personneList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Personne in Elasticsearch
        verify(mockPersonneSearchRepository, times(0)).save(personne);
    }

    @Test
    void patchWithMissingIdPathParamPersonne() throws Exception {
        int databaseSizeBeforeUpdate = personneRepository.findAll().collectList().block().size();
        personne.setId(UUID.randomUUID().toString());

        // Create the Personne
        PersonneDTO personneDTO = personneMapper.toDto(personne);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(personneDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Personne in the database
        List<Personne> personneList = personneRepository.findAll().collectList().block();
        assertThat(personneList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Personne in Elasticsearch
        verify(mockPersonneSearchRepository, times(0)).save(personne);
    }

    @Test
    void deletePersonne() {
        // Configure the mock search repository
        when(mockPersonneSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(mockPersonneSearchRepository.deleteById(anyString())).thenReturn(Mono.empty());
        // Initialize the database
        personneRepository.save(personne).block();

        int databaseSizeBeforeDelete = personneRepository.findAll().collectList().block().size();

        // Delete the personne
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, personne.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Personne> personneList = personneRepository.findAll().collectList().block();
        assertThat(personneList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Personne in Elasticsearch
        verify(mockPersonneSearchRepository, times(1)).deleteById(personne.getId());
    }

    @Test
    void searchPersonne() {
        // Configure the mock search repository
        when(mockPersonneSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(mockPersonneSearchRepository.count()).thenReturn(Mono.just(1L));
        // Initialize the database
        personneRepository.save(personne).block();
        when(mockPersonneSearchRepository.search("id:" + personne.getId(), PageRequest.of(0, 20))).thenReturn(Flux.just(personne));

        // Search the personne
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + personne.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(personne.getId()))
            .jsonPath("$.[*].prenom")
            .value(hasItem(DEFAULT_PRENOM))
            .jsonPath("$.[*].nom")
            .value(hasItem(DEFAULT_NOM))
            .jsonPath("$.[*].telephone")
            .value(hasItem(DEFAULT_TELEPHONE));
    }
}
