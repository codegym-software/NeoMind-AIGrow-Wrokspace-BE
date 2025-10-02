// SIMPLE STANDALONE TEST FOR PROJECT MANAGEMENT
// Copy this to a new Spring Boot project to test quickly

@RestController
@RequestMapping("/api/test-projects")
public class SimpleProjectController {
    
    private List<SimpleProject> projects = new ArrayList<>();
    
    @PostMapping
    public SimpleProject create(@RequestBody SimpleProject project) {
        project.setId(UUID.randomUUID().toString());
        project.setCreatedAt(LocalDateTime.now());
        projects.add(project);
        return project;
    }
    
    @GetMapping
    public List<SimpleProject> getAll() {
        return projects;
    }
    
    @GetMapping("/{id}")
    public SimpleProject getById(@PathVariable String id) {
        return projects.stream()
            .filter(p -> p.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Project not found"));
    }
    
    @GetMapping("/workspace/{workspaceId}")
    public List<SimpleProject> getByWorkspace(@PathVariable String workspaceId) {
        return projects.stream()
            .filter(p -> p.getWorkspaceId().equals(workspaceId))
            .collect(Collectors.toList());
    }
}

class SimpleProject {
    private String id;
    private String workspaceId;
    private String name;
    private String description;
    private String status = "PLANNING";
    private String priority = "MEDIUM";
    private LocalDateTime createdAt;
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

// Test with:
// POST /api/test-projects
// {
//   "workspaceId": "test-workspace-001",
//   "name": "Test Project",
//   "description": "Simple test"
// }
//
// GET /api/test-projects/workspace/test-workspace-001
