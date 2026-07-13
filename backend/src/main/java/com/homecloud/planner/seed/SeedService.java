package com.homecloud.planner.seed;

import com.homecloud.planner.backup.BackupFrequency;
import com.homecloud.planner.backup.BackupPolicy;
import com.homecloud.planner.backup.BackupPolicyRepository;
import com.homecloud.planner.decision.ArchitectureDecision;
import com.homecloud.planner.decision.ArchitectureDecisionRepository;
import com.homecloud.planner.decision.DecisionStatus;
import com.homecloud.planner.device.Device;
import com.homecloud.planner.device.DeviceRepository;
import com.homecloud.planner.device.LifecycleStatus;
import com.homecloud.planner.phase.Phase;
import com.homecloud.planner.phase.PhaseRepository;
import com.homecloud.planner.project.Project;
import com.homecloud.planner.project.ProjectRepository;
import com.homecloud.planner.project.ProjectStatus;
import com.homecloud.planner.servicecatalog.ManagedService;
import com.homecloud.planner.servicecatalog.ManagedServiceRepository;
import com.homecloud.planner.servicecatalog.ManagedServiceStatus;
import com.homecloud.planner.servicecatalog.RuntimeType;
import com.homecloud.planner.servicecatalog.Sensitivity;
import com.homecloud.planner.servicecatalog.ServiceDependency;
import com.homecloud.planner.servicecatalog.ServiceDependencyRepository;
import com.homecloud.planner.shopping.PurchaseItem;
import com.homecloud.planner.shopping.PurchaseItemRepository;
import com.homecloud.planner.shopping.PurchaseStatus;
import com.homecloud.planner.task.Task;
import com.homecloud.planner.task.TaskDependency;
import com.homecloud.planner.task.TaskDependencyRepository;
import com.homecloud.planner.task.TaskPriority;
import com.homecloud.planner.task.TaskRepository;
import com.homecloud.planner.task.TaskStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds a single representative "Personal Hybrid Cloud" project, based on
 * the planning documents in the sibling personal-cloud-docs repository
 * (roadmap, service catalog, network design, backup strategy, shopping
 * list, and ADR-0001). Development-only: this whole package only exists
 * as a Spring bean when the "dev" profile is active — see ADR-0006.
 */
@Service
@Profile("dev")
public class SeedService {

    private static final String PROJECT_NAME = "Personal Hybrid Cloud";

    private final ProjectRepository projectRepository;
    private final PhaseRepository phaseRepository;
    private final TaskRepository taskRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final DeviceRepository deviceRepository;
    private final ManagedServiceRepository managedServiceRepository;
    private final ServiceDependencyRepository serviceDependencyRepository;
    private final BackupPolicyRepository backupPolicyRepository;
    private final ArchitectureDecisionRepository architectureDecisionRepository;

    public SeedService(
            ProjectRepository projectRepository,
            PhaseRepository phaseRepository,
            TaskRepository taskRepository,
            TaskDependencyRepository taskDependencyRepository,
            PurchaseItemRepository purchaseItemRepository,
            DeviceRepository deviceRepository,
            ManagedServiceRepository managedServiceRepository,
            ServiceDependencyRepository serviceDependencyRepository,
            BackupPolicyRepository backupPolicyRepository,
            ArchitectureDecisionRepository architectureDecisionRepository) {
        this.projectRepository = projectRepository;
        this.phaseRepository = phaseRepository;
        this.taskRepository = taskRepository;
        this.taskDependencyRepository = taskDependencyRepository;
        this.purchaseItemRepository = purchaseItemRepository;
        this.deviceRepository = deviceRepository;
        this.managedServiceRepository = managedServiceRepository;
        this.serviceDependencyRepository = serviceDependencyRepository;
        this.backupPolicyRepository = backupPolicyRepository;
        this.architectureDecisionRepository = architectureDecisionRepository;
    }

    /** Repeatable: removes any previous seed run for this project name, then recreates it from scratch. */
    @Transactional
    public Project reseed() {
        removeExistingSeed();

        Project project = projectRepository.save(new Project(
                PROJECT_NAME,
                "Track the planning, purchasing, installation, and operation of a personal hybrid cloud: "
                        + "protect current data, add local compute, automate and observe the home, then extend into "
                        + "documents and local AI.",
                ProjectStatus.IN_PROGRESS,
                new BigDecimal("6000.00"),
                LocalDate.now().minusMonths(2),
                LocalDate.now().plusMonths(10)));

        Phase foundation = phaseRepository.save(new Phase(project, "Foundation", "Networking groundwork and planning.", 1));
        Phase storage = phaseRepository.save(new Phase(project, "Storage", "Protect current data before anything else.", 2));
        Phase compute = phaseRepository.save(new Phase(project, "Compute", "Add a mini PC and deploy core services.", 3));
        Phase automation = phaseRepository.save(
                new Phase(project, "Automation and Observability", "Home Assistant, metrics, dashboards, and logs.", 4));
        Phase documentAi = phaseRepository.save(
                new Phase(project, "Document and AI Platform", "Document management and private local AI.", 5));
        Phase futureExpansion = phaseRepository.save(
                new Phase(project, "Future Expansion", "Scale networking and compute only when justified.", 6));

        seedFoundationTasks(foundation);
        List<Task> storageTasks = seedStorageTasks(storage);
        List<Task> computeTasks = seedComputeTasks(compute);
        seedAutomationTasks(automation);
        seedDocumentAiTasks(documentAi);
        seedFutureExpansionTasks(futureExpansion);

        // Cross-phase dependency: configuring Docker Compose (Compute) depends on
        // the NAS backup being restore-tested (Storage) — application data lives on
        // compute but its durability depends on storage being trustworthy first.
        Task testRestores = storageTasks.get(storageTasks.size() - 1);
        Task deployDockerCompose = computeTasks.get(2);
        taskDependencyRepository.save(new TaskDependency(deployDockerCompose, testRestores));

        seedShoppingList(project, foundation, storage, compute, futureExpansion);
        List<Device> devices = seedDevices(project);
        List<ManagedService> services = seedServices(project, devices);
        seedBackupPolicies(project);
        seedDecisions(project, devices, services);

        return project;
    }

    private void removeExistingSeed() {
        projectRepository.findByName(PROJECT_NAME).ifPresent(project -> {
            architectureDecisionRepository.deleteAll(architectureDecisionRepository.findByProjectId(project.getId()));
            backupPolicyRepository.deleteAll(backupPolicyRepository.findByProjectId(project.getId()));
            managedServiceRepository.deleteAll(managedServiceRepository.findByProjectId(project.getId()));
            deviceRepository.deleteAll(deviceRepository.findByProjectId(project.getId()));
            purchaseItemRepository.deleteAll(purchaseItemRepository.findByProjectId(project.getId()));
            taskRepository.deleteAll(taskRepository.findByPhaseProjectId(project.getId()));
            phaseRepository.deleteAll(phaseRepository.findByProjectIdOrderBySequenceAsc(project.getId()));
            projectRepository.delete(project);
        });
    }

    private void seedFoundationTasks(Phase foundation) {
        task(foundation, "Inventory existing network equipment", TaskStatus.COMPLETED, TaskPriority.MEDIUM,
                "USG-3P, Cloud Key, 8-port PoE switch, and two NanoHD access points are in place and documented.");
        task(foundation, "Document VLAN plan", TaskStatus.NOT_STARTED, TaskPriority.MEDIUM,
                "Trusted, IoT, guest, management, and an optional lab VLAN, each with an explicit firewall rule.");
        task(foundation, "Set up UPS for core equipment", TaskStatus.NOT_STARTED, TaskPriority.HIGH,
                "USB/network shutdown support, enough runtime for the NAS, gateway, switch, and compute host.");
    }

    private List<Task> seedStorageTasks(Phase storage) {
        Task selectNas = task(storage, "Select NAS and drives", TaskStatus.COMPLETED, TaskPriority.CRITICAL,
                "Four-bay NAS with Btrfs snapshots, Time Machine, and cloud backup support; CMR NAS-rated drives.");
        Task configureShares = task(storage, "Define storage shares and permissions", TaskStatus.IN_PROGRESS,
                TaskPriority.HIGH, "Separate shares for Time Machine, the photo archive, documents, and app data.");
        Task timeMachine = task(storage, "Configure Time Machine", TaskStatus.NOT_STARTED, TaskPriority.HIGH, null);
        Task photoArchive = task(storage, "Create independent Apple Photos archive workflow", TaskStatus.NOT_STARTED,
                TaskPriority.MEDIUM, "Keep an original-quality copy on the NAS independent of iCloud Photos.");
        Task offsiteBackup = task(storage, "Configure encrypted off-site backup", TaskStatus.NOT_STARTED,
                TaskPriority.CRITICAL, "Encrypt before upload for anything containing sensitive data.");
        Task testRestores = task(storage, "Test restores", TaskStatus.NOT_STARTED, TaskPriority.CRITICAL,
                "Restore one Time Machine file and one photo original to prove the backups actually work.");

        dependsOn(configureShares, selectNas);
        dependsOn(timeMachine, configureShares);
        dependsOn(offsiteBackup, timeMachine);
        dependsOn(testRestores, offsiteBackup);

        return List.of(selectNas, configureShares, timeMachine, photoArchive, offsiteBackup, testRestores);
    }

    private List<Task> seedComputeTasks(Phase compute) {
        Task selectMiniPc = task(compute, "Select a mini PC", TaskStatus.IN_PROGRESS, TaskPriority.HIGH,
                "32-64 GB RAM, NVMe, 2.5GbE, efficient CPU; Quick Sync if transcoding matters.");
        Task installOs = task(compute, "Install the base operating system", TaskStatus.NOT_STARTED, TaskPriority.HIGH, null);
        Task deployDockerCompose = task(compute, "Deploy Docker Compose", TaskStatus.NOT_STARTED, TaskPriority.HIGH, null);
        Task deployPlanner = task(compute, "Deploy HomeCloud Planner", TaskStatus.NOT_STARTED, TaskPriority.MEDIUM,
                "This application — track everything else from here on.");

        dependsOn(installOs, selectMiniPc);
        dependsOn(deployDockerCompose, installOs);
        dependsOn(deployPlanner, deployDockerCompose);

        return List.of(selectMiniPc, installOs, deployDockerCompose, deployPlanner);
    }

    private void seedAutomationTasks(Phase automation) {
        Task homeAssistant = task(automation, "Deploy Home Assistant", TaskStatus.NOT_STARTED, TaskPriority.HIGH,
                "As Home Assistant OS in a VM, or as a container for more explicit service management.");
        Task prometheus = task(automation, "Deploy Prometheus", TaskStatus.NOT_STARTED, TaskPriority.MEDIUM, null);
        Task grafana = task(automation, "Deploy Grafana", TaskStatus.NOT_STARTED, TaskPriority.MEDIUM,
                "Dashboards for internet health, host resources, storage capacity, and backup status.");
        Task loki = task(automation, "Deploy Loki", TaskStatus.NOT_STARTED, TaskPriority.LOW,
                "Add once metrics are stable; useful but not required for the first deployment.");

        dependsOn(grafana, prometheus);
        dependsOn(loki, grafana);
        // Independent of the storage/compute chain, but still benefits from a host being ready.
        dependsOn(homeAssistant, prometheus);
    }

    private void seedDocumentAiTasks(Phase documentAi) {
        Task paperless = task(documentAi, "Deploy Paperless-ngx", TaskStatus.NOT_STARTED, TaskPriority.MEDIUM, null);
        Task scanningWorkflow = task(documentAi, "Establish scanning and retention workflow", TaskStatus.NOT_STARTED,
                TaskPriority.MEDIUM, null);
        task(documentAi, "Add local embeddings and private retrieval", TaskStatus.NOT_STARTED, TaskPriority.LOW, null);
        task(documentAi, "Evaluate local LLM performance", TaskStatus.NOT_STARTED, TaskPriority.LOW,
                "Before buying dedicated GPU hardware, confirm real model and latency requirements.");

        dependsOn(scanningWorkflow, paperless);
    }

    private void seedFutureExpansionTasks(Phase futureExpansion) {
        task(futureExpansion, "Upgrade to 2.5/10GbE between compute and storage", TaskStatus.NOT_STARTED,
                TaskPriority.LOW, null);
        task(futureExpansion, "Add an additional compute node", TaskStatus.NOT_STARTED, TaskPriority.LOW, null);
        task(futureExpansion, "Evaluate dedicated AI hardware", TaskStatus.NOT_STARTED, TaskPriority.LOW,
                "Only after workloads are understood — no speculative GPU purchases.");
        task(futureExpansion, "Upgrade UniFi gateway", TaskStatus.NOT_STARTED, TaskPriority.LOW,
                "Only when IDS/IPS is an active bottleneck or before moving to faster fiber.");
    }

    private Task task(Phase phase, String title, TaskStatus status, TaskPriority priority, String acceptanceCriteria) {
        LocalDate completedDate = status == TaskStatus.COMPLETED ? LocalDate.now().minusWeeks(2) : null;
        LocalDate targetDate = status == TaskStatus.COMPLETED ? null : LocalDate.now().plusWeeks(3);
        return taskRepository.save(new Task(
                phase, title, null, status, priority, null, null, targetDate, completedDate, acceptanceCriteria, null));
    }

    private void dependsOn(Task task, Task prerequisite) {
        taskDependencyRepository.save(new TaskDependency(task, prerequisite));
    }

    private void seedShoppingList(Project project, Phase foundation, Phase storage, Phase compute, Phase futureExpansion) {
        purchase(project, storage, "Storage", "Four-bay NAS", "Synology or QNAP", 1,
                new BigDecimal("650.00"), null, PurchaseStatus.PLANNED,
                "Btrfs snapshots, Time Machine, and cloud backup support; quiet desktop form factor.");
        purchase(project, storage, "Storage", "NAS drives", "NAS-rated CMR", 4,
                new BigDecimal("110.00"), null, PurchaseStatus.PLANNED, "Sized after capacity planning.");
        purchase(project, foundation, "Power", "UPS", null, 1,
                new BigDecimal("180.00"), null, PurchaseStatus.PLANNED,
                "USB/network shutdown support; enough runtime for the NAS, gateway, switch, and compute host.");
        purchase(project, foundation, "Infrastructure", "Vented rack shelf", null, 1,
                new BigDecimal("40.00"), null, PurchaseStatus.IDEA, "For the NAS, mini PC, and Raspberry Pi.");
        purchase(project, compute, "Compute", "Mini PC", null, 1,
                new BigDecimal("550.00"), null, PurchaseStatus.RESEARCHING,
                "32-64 GB RAM, NVMe, 2.5GbE, efficient CPU; Quick Sync if transcoding matters.");
        purchase(project, compute, "Compute", "NVMe SSD", "High endurance", 1,
                new BigDecimal("120.00"), null, PurchaseStatus.IDEA, "Sized for applications and databases.");
        purchase(project, futureExpansion, "Networking", "UniFi gateway", null, 1,
                new BigDecimal("400.00"), null, PurchaseStatus.IDEA,
                "Multi-gig capable with full security features enabled; buy only when justified.");
        purchase(project, futureExpansion, "Networking", "Multi-gig switch", null, 1,
                new BigDecimal("250.00"), null, PurchaseStatus.IDEA, "2.5GbE access and 10GbE uplinks.");
        purchase(project, futureExpansion, "Compute", "AI system", "NVIDIA GPU or unified-memory", 1,
                new BigDecimal("1500.00"), null, PurchaseStatus.IDEA,
                "Chosen for proven model needs only — no speculative purchase.");
    }

    private void purchase(
            Project project,
            Phase phase,
            String category,
            String productName,
            String manufacturer,
            int quantity,
            BigDecimal estimatedUnitPrice,
            BigDecimal actualUnitPrice,
            PurchaseStatus status,
            String notes) {
        purchaseItemRepository.save(new PurchaseItem(
                project, phase, category, productName, manufacturer, null, null, quantity, estimatedUnitPrice,
                actualUnitPrice, null, null, status, null, null, null, null, notes));
    }

    private List<Device> seedDevices(Project project) {
        Device gateway = device(project, "USG-3P", "Ubiquiti", "Gateway", "Rack", LifecycleStatus.ACTIVE, null);
        device(project, "Cloud Key", "Ubiquiti", "Controller", "Rack", LifecycleStatus.ACTIVE, null);
        Device switch8Port = device(project, "8-port PoE switch", "Ubiquiti", "Switch", "Rack", LifecycleStatus.ACTIVE, null);
        device(project, "NanoHD AP - Living room", "Ubiquiti", "Access Point", "Living room", LifecycleStatus.ACTIVE, null);
        device(project, "NanoHD AP - Office", "Ubiquiti", "Access Point", "Office", LifecycleStatus.ACTIVE, null);
        Device raspberryPi = device(project, "Raspberry Pi", "Raspberry Pi Foundation", "DNS host", "Office",
                LifecycleStatus.ACTIVE, null);
        device(project, "WD NAS (existing)", "Western Digital", "Storage", "Office", LifecycleStatus.ACTIVE,
                LocalDate.now().plusMonths(2));
        device(project, "Mini PC (planned)", null, "Compute host", "Office", LifecycleStatus.PLANNED, null);

        return List.of(gateway, switch8Port, raspberryPi);
    }

    private Device device(
            Project project, String name, String manufacturer, String role, String location, LifecycleStatus status,
            LocalDate replacementTarget) {
        return deviceRepository.save(new Device(
                project, name, manufacturer, null, null, role, location, null, null, null, null, null, null, null,
                null, status, replacementTarget, null));
    }

    private List<ManagedService> seedServices(Project project, List<Device> devices) {
        Device raspberryPi = devices.get(2);

        ManagedService adGuard = managedService(project, raspberryPi, "AdGuard Home",
                "Local DNS resolution, network-wide ad/tracker filtering, and local hostnames.",
                ManagedServiceStatus.OPERATIONAL, RuntimeType.DOCKER, Sensitivity.INTERNAL, false,
                "Keeps DNS available when the main compute host is being maintained.");
        ManagedService homeAssistant = managedService(project, null, "Home Assistant",
                "Smart-home orchestration across Apple, Google, Alexa, Nest, Matter, and Zigbee integrations.",
                ManagedServiceStatus.PLANNED, RuntimeType.VIRTUAL_MACHINE, Sensitivity.INTERNAL, false, null);
        ManagedService prometheus = managedService(project, null, "Prometheus",
                "Collects time-series metrics from applications, hosts, containers, and exporters.",
                ManagedServiceStatus.PLANNED, RuntimeType.DOCKER, Sensitivity.INTERNAL, false, null);
        ManagedService grafana = managedService(project, null, "Grafana",
                "Dashboards and alerts for internet health, host resources, storage, and backup status.",
                ManagedServiceStatus.PLANNED, RuntimeType.DOCKER, Sensitivity.INTERNAL, false, null);
        ManagedService paperless = managedService(project, null, "Paperless-ngx",
                "Ingests, OCRs, tags, and indexes scanned documents.",
                ManagedServiceStatus.PLANNED, RuntimeType.DOCKER, Sensitivity.CONFIDENTIAL, false, null);
        ManagedService ollama = managedService(project, null, "Ollama", "Runs local language models for private workflows.",
                ManagedServiceStatus.PLANNED, RuntimeType.DOCKER, Sensitivity.INTERNAL, false, null);
        ManagedService openWebUi = managedService(project, null, "Open WebUI",
                "Browser-based chat interface for Ollama and selected external providers.",
                ManagedServiceStatus.PLANNED, RuntimeType.DOCKER, Sensitivity.INTERNAL, false, null);
        ManagedService authentik = managedService(project, null, "Authentik",
                "Single sign-on, OpenID Connect, and multi-factor authentication for self-hosted apps.",
                ManagedServiceStatus.PLANNED, RuntimeType.DOCKER, Sensitivity.CONFIDENTIAL, false,
                "Add after the initial services are stable.");
        ManagedService tailscale = managedService(project, null, "Tailscale",
                "Encrypted remote access without directly exposing internal applications.",
                ManagedServiceStatus.PLANNED, RuntimeType.OTHER, Sensitivity.CONFIDENTIAL, true, null);
        ManagedService jellyfin = managedService(project, null, "Jellyfin", "Indexes and streams locally stored media.",
                ManagedServiceStatus.PLANNED, RuntimeType.DOCKER, Sensitivity.INTERNAL, false, null);
        ManagedService planner = managedService(project, null, "HomeCloud Planner",
                "Tracks roadmap progress, purchases, budget, devices, services, backups, and decisions — this application.",
                ManagedServiceStatus.OPERATIONAL, RuntimeType.DOCKER, Sensitivity.CONFIDENTIAL, false, null);

        serviceDependency(grafana, prometheus);
        serviceDependency(openWebUi, ollama);
        serviceDependency(homeAssistant, adGuard);

        return List.of(
                adGuard, homeAssistant, prometheus, grafana, paperless, ollama, openWebUi, authentik, tailscale,
                jellyfin, planner);
    }

    private ManagedService managedService(
            Project project,
            Device hostDevice,
            String name,
            String purpose,
            ManagedServiceStatus status,
            RuntimeType runtimeType,
            Sensitivity sensitivity,
            boolean externallyExposed,
            String notes) {
        return managedServiceRepository.save(new ManagedService(
                project, hostDevice, name, purpose, null, status, runtimeType, null, sensitivity, externallyExposed,
                null, null, null, null, notes));
    }

    private void serviceDependency(ManagedService service, ManagedService dependsOn) {
        serviceDependencyRepository.save(new ServiceDependency(service, dependsOn));
    }

    private void seedBackupPolicies(Project project) {
        backupPolicy(project, "MacBook", "MacBook", "Time Machine on NAS", null, false, false,
                BackupFrequency.CONTINUOUS, "Rolling", null, null, null, null);
        backupPolicy(project, "Apple Photos", "iCloud / Apple Photos", "Independent NAS archive plus Mac backup",
                "Encrypted cloud copy of the archive", true, true, BackupFrequency.DAILY, "Indefinite", "24 hours",
                "4 hours", LocalDate.now().minusDays(20), "Restored a sample original successfully.");
        backupPolicy(project, "Documents", "NAS", "Snapshots and versioned backup", "Encrypted cloud backup", true, true,
                BackupFrequency.DAILY, "1 year", "24 hours", "4 hours", LocalDate.now().minusDays(10),
                "Quarterly restore test passed.");
        backupPolicy(project, "Home Assistant configuration", "Mini PC", "Automated backup to NAS",
                "Encrypted cloud copy", true, false, BackupFrequency.WEEKLY, "90 days", null, null, null, null);
        backupPolicy(project, "Application configuration", "Git", "NAS mirror", "Private remote Git", false, false,
                BackupFrequency.CONTINUOUS, "Full history", null, null, null, null);
        backupPolicy(project, "HomeCloud Planner database", "Mini PC", "Scheduled PostgreSQL backup to NAS",
                "Encrypted cloud copy", true, true, BackupFrequency.DAILY, "30 days", "24 hours", "1 hour",
                LocalDate.now(), "Restored into a scratch database successfully.");
        backupPolicy(project, "AI indexes", "Mini PC", null, null, false, false, BackupFrequency.MANUAL, null, null,
                null, null, "Usually rebuildable from source data; backup is optional.");
    }

    private void backupPolicy(
            Project project,
            String dataCategory,
            String primaryLocation,
            String localBackupLocation,
            String offsiteBackupLocation,
            boolean encrypted,
            boolean containsSensitiveData,
            BackupFrequency frequency,
            String retention,
            String recoveryPointObjective,
            String recoveryTimeObjective,
            LocalDate lastVerifiedDate,
            String verificationNotes) {
        backupPolicyRepository.save(new BackupPolicy(
                project, dataCategory + " backup", dataCategory, primaryLocation, localBackupLocation,
                offsiteBackupLocation, encrypted, containsSensitiveData, frequency, retention, recoveryPointObjective,
                recoveryTimeObjective, lastVerifiedDate, verificationNotes));
    }

    private void seedDecisions(Project project, List<Device> devices, List<ManagedService> services) {
        Device gateway = devices.get(0);
        ManagedService homeAssistantService = services.get(1);

        ArchitectureDecision separateStorageCompute = new ArchitectureDecision(
                project,
                "Separate storage and compute",
                DecisionStatus.ACCEPTED,
                "The application has several related domains but no current need for independently scalable "
                        + "services or hardware.",
                "Use a dedicated NAS for durable storage and a mini PC for applications.",
                "A single consolidated server running both storage and compute workloads.",
                "Permits independent upgrades, reduces application impact on backup workloads, and avoids "
                        + "replacing storage solely to gain more CPU or memory.",
                LocalDate.now().minusMonths(3),
                "Revisit only if a future consolidated platform has a clear cost, reliability, and recovery "
                        + "advantage.");
        architectureDecisionRepository.save(separateStorageCompute);

        ArchitectureDecision deferNetworkUpgrades = new ArchitectureDecision(
                project,
                "Defer network hardware upgrades until justified by measured need",
                DecisionStatus.ACCEPTED,
                "The existing USG-3P, Cloud Key, switch, and access points still meet current needs.",
                "Keep the current gateway and switch until IDS/IPS materially limits desired throughput or "
                        + "faster fiber arrives; keep switching until NAS-to-compute or client workloads "
                        + "demonstrate a multi-gig need.",
                "Upgrading proactively based on the general market roadmap rather than a measured bottleneck.",
                "Avoids spending on hardware headroom that isn't yet needed.",
                LocalDate.now().minusMonths(3),
                "Revisit when security logging/IDS measurably limits throughput, or before activating faster "
                        + "fiber.");
        deferNetworkUpgrades.setRelatedDevices(new HashSet<>(Set.of(gateway)));
        architectureDecisionRepository.save(deferNetworkUpgrades);

        ArchitectureDecision haDeployment = new ArchitectureDecision(
                project,
                "Run Home Assistant as a container initially",
                DecisionStatus.PROPOSED,
                "Home Assistant OS in a VM simplifies add-ons; a container gives more explicit service "
                        + "management alongside the rest of the Docker Compose stack.",
                "Start with Home Assistant as a container managed by the same Docker Compose stack as other "
                        + "services, and reassess if add-on simplicity becomes a real friction point.",
                "Home Assistant OS in a dedicated VM.",
                "Consistent deployment and backup tooling with the rest of the stack; some add-ons may require "
                        + "more manual setup.",
                null,
                "Revisit if a specific add-on that requires HAOS becomes necessary.");
        haDeployment.setRelatedServices(new HashSet<>(Set.of(homeAssistantService)));
        architectureDecisionRepository.save(haDeployment);
    }
}
