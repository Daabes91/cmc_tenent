package com.clinic.modules.admin.controller;

import com.clinic.modules.admin.dto.PatientTagInput;
import com.clinic.modules.admin.dto.PatientUpsertRequest;
import com.clinic.modules.core.patient.PatientEntity;
import com.clinic.modules.core.patient.PatientRepository;
import com.clinic.modules.core.tag.TagEntity;
import com.clinic.modules.core.tag.TagRepository;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PatientTagIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private TagRepository tagRepository;

        @Autowired
        private PatientRepository patientRepository;

        @Test
        @WithMockUser(roles = { "ADMIN" })
        void shouldCreateAndAssignTagsToPatient() throws Exception {
                // 1. Create a tag
                mockMvc.perform(post("/admin/patients/tags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of("name", "VIP", "color", "red"))))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.data.name").value("VIP"));

                TagEntity tag = tagRepository.findAll().get(0);

                // 2. Create a patient with the tag
                PatientUpsertRequest request = new PatientUpsertRequest(
                                "John",
                                "Doe",
                                "john.doe@example.com",
                                "1234567890",
                                null,
                                null,
                                LocalDate.of(1990, 1, 1),
                                "Notes",
                                List.of(new PatientTagInput(tag.getId(), null)));

                mockMvc.perform(post("/admin/patients")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.data.tags[0].name").value("VIP"));

                // 3. Verify database
                PatientEntity patient = patientRepository.findAll().get(0);
                assertThat(patient.getTags()).hasSize(1);
                assertThat(patient.getTags().iterator().next().getName()).isEqualTo("VIP");
        }

        @Test
        @WithMockUser(roles = { "ADMIN" })
        void shouldFilterPatientsByTags() throws Exception {
                // 1. Create tags
                mockMvc.perform(post("/admin/patients/tags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of("name", "Chronic", "color", "blue"))))
                                .andExpect(status().isCreated());

                mockMvc.perform(post("/admin/patients/tags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of("name", "Acute", "color", "red"))))
                                .andExpect(status().isCreated());

                // Get tags
                List<TagEntity> allTags = tagRepository.findAll();
                TagEntity tag1 = allTags.stream().filter(t -> t.getName().equals("Chronic")).findFirst().orElseThrow();
                TagEntity tag2 = allTags.stream().filter(t -> t.getName().equals("Acute")).findFirst().orElseThrow();

                // 2. Create patients
                createPatientWithTag("Patient1", List.of(tag1.getId()));
                createPatientWithTag("Patient2", List.of(tag2.getId()));
                createPatientWithTag("Patient3", List.of(tag1.getId()));

                // 3. Filter by Tag 1
                mockMvc.perform(get("/admin/patients")
                                .param("tags", String.valueOf(tag1.getId())))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.content.length()").value(2));

                // 4. Filter by Tag 2
                mockMvc.perform(get("/admin/patients")
                                .param("tags", String.valueOf(tag2.getId())))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.content.length()").value(1));
        }

        private void createPatientWithTag(String firstName, List<Long> tagIds) throws Exception {
                List<PatientTagInput> tagInputs = tagIds.stream()
                                .map(id -> new PatientTagInput(id, null))
                                .toList();
                PatientUpsertRequest request = new PatientUpsertRequest(
                                firstName,
                                "Test",
                                firstName.toLowerCase() + "@example.com",
                                "1234567890",
                                null,
                                null,
                                LocalDate.of(1990, 1, 1),
                                "Notes",
                                tagInputs);

                mockMvc.perform(post("/admin/patients")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated());
        }
}
