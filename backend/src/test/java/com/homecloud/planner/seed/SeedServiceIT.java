package com.homecloud.planner.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.homecloud.planner.SecurityTestConfiguration;
import com.homecloud.planner.TestcontainersConfiguration;
import com.homecloud.planner.backup.BackupPolicyRepository;
import com.homecloud.planner.decision.ArchitectureDecisionRepository;
import com.homecloud.planner.device.DeviceRepository;
import com.homecloud.planner.phase.PhaseRepository;
import com.homecloud.planner.project.Project;
import com.homecloud.planner.project.ProjectRepository;
import com.homecloud.planner.servicecatalog.ManagedServiceRepository;
import com.homecloud.planner.shopping.PurchaseItemRepository;
import com.homecloud.planner.task.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@Import({TestcontainersConfiguration.class, SecurityTestConfiguration.class})
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class SeedServiceIT {

    @Autowired
    private SeedService seedService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PhaseRepository phaseRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PurchaseItemRepository purchaseItemRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ManagedServiceRepository managedServiceRepository;

    @Autowired
    private BackupPolicyRepository backupPolicyRepository;

    @Autowired
    private ArchitectureDecisionRepository architectureDecisionRepository;

    @Test
    void seedingCreatesTheExpectedProjectStructure() {
        Project project = seedService.reseed();

        assertThat(project.getName()).isEqualTo("Personal Hybrid Cloud");
        assertThat(phaseRepository.findByProjectIdOrderBySequenceAsc(project.getId())).hasSize(6);
        assertThat(taskRepository.findByPhaseProjectId(project.getId())).isNotEmpty();
        assertThat(purchaseItemRepository.findByProjectId(project.getId())).isNotEmpty();
        assertThat(deviceRepository.findByProjectId(project.getId())).isNotEmpty();
        assertThat(managedServiceRepository.findByProjectId(project.getId())).isNotEmpty();
        assertThat(backupPolicyRepository.findByProjectId(project.getId())).isNotEmpty();
        assertThat(architectureDecisionRepository.findByProjectId(project.getId())).isNotEmpty();
    }

    @Test
    void seedingIsRepeatableWithoutDuplicatingTheProject() {
        seedService.reseed();
        Project second = seedService.reseed();

        long matchingProjects = projectRepository.findAll().stream()
                .filter(project -> project.getName().equals("Personal Hybrid Cloud"))
                .count();
        assertThat(matchingProjects).isEqualTo(1);
        assertThat(phaseRepository.findByProjectIdOrderBySequenceAsc(second.getId())).hasSize(6);
    }

    @Test
    void seedEndpointIsAvailableUnderTheDevProfile() throws Exception {
        mockMvc.perform(post("/api/v1/dev/seed")).andExpect(status().isCreated());
    }
}
