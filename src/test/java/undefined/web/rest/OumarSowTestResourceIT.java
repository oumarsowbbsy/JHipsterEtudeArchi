package com.mycompany.myapp.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mycompany.myapp.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Test class for the OumarSowTestResource REST controller.
 *
 * @see OumarSowTestResource
 */
@IntegrationTest
class OumarSowTestResourceIT {

    private MockMvc restMockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        OumarSowTestResource oumarSowTestResource = new OumarSowTestResource();
        restMockMvc = MockMvcBuilders.standaloneSetup(oumarSowTestResource).build();
    }

    /**
     * Test creation
     */
    @Test
    void testCreation() throws Exception {
        restMockMvc.perform(post("/api/oumar-sow-test/creation")).andExpect(status().isOk());
    }

    /**
     * Test createTest
     */
    @Test
    void testCreateTest() throws Exception {
        restMockMvc.perform(put("/api/oumar-sow-test/create-test")).andExpect(status().isOk());
    }
}
