# Assertion and Reason MCQ Assessment System â Low-Level Technical Design Document

---

## USER STORY 1: Create Assertion and Reason MCQ Assessment Module

### Section: Spring Boot Architecture Overview
**Description:**  
The system follows a layered architecture using Spring Boot, separating concerns into Controller, Service, Repository, and Domain layers. RESTful APIs are exposed for assessment creation, with validation and security handled via Spring Security.

**Design Specification:**  
- RESTful API for assessment creation  
- Validation at DTO and entity levels  
- Service layer for business logic  
- Repository for persistence  
- Security: Only teachers can create assessments

### Section: Package Structure, Module Definitions, and Component Breakdown
**Description:**  
Organized by feature and responsibility.

**Design Specification:**  
- `com.example.mcqassessment`
  - `controller`
  - `service`
  - `repository`
  - `domain`
  - `dto`
  - `config`
  - `security`

### Section: Entity Design with Domain Models and Relationships (JPA Entities)
**Description:**  
Models for Assessment, Question, and AnswerChoice.

**Design Specification:**  
- `Assessment` (id, title, week, topic, createdBy, createdAt)
- `Question` (id, assertionText, reasonText, explanation, assessment, answerChoices, correctChoice)
- `AnswerChoice` (id, label, text, question)

**Sample Implementation:**  
```java
@Entity
public class Assessment {
    @Id @GeneratedValue
    private Long id;
    private String title;
    private String week;
    private String topic;
    private String createdBy;
    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL)
    private List<Question> questions;
}

@Entity
public class Question {
    @Id @GeneratedValue
    private Long id;
    private String assertionText;
    private String reasonText;
    private String explanation;
    @ManyToOne
    private Assessment assessment;
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private List<AnswerChoice> answerChoices;
    private String correctChoice; // a, b, c, d
}

@Entity
public class AnswerChoice {
    @Id @GeneratedValue
    private Long id;
    private String label; // a, b, c, d
    private String text;
    @ManyToOne
    private Question question;
}
```

### Section: Service Layer Specifications with Business Logic
**Description:**  
Handles validation, mapping DTOs to entities, and saving assessments.

**Design Specification:**  
- `AssessmentService.createAssessment(AssessmentDTO dto)`
- Validates input, maps to entities, persists

**Sample Implementation:**  
```java
@Service
public class AssessmentService {
    @Autowired
    private AssessmentRepository assessmentRepository;
    public Assessment createAssessment(AssessmentDTO dto) {
        // Validate, map, and save
    }
}
```

### Section: Repository Layer with Spring Data JPA Repositories
**Description:**  
Persistence for entities.

**Design Specification:**  
- `AssessmentRepository extends JpaRepository<Assessment, Long>`
- `QuestionRepository extends JpaRepository<Question, Long>`
- `AnswerChoiceRepository extends JpaRepository<AnswerChoice, Long>`

### Section: Controller Specifications with REST API Endpoints
**Description:**  
Exposes endpoints for assessment creation.

**Design Specification:**  
- `POST /api/assessments` (Teacher only)

**Sample Implementation:**  
```java
@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {
    @Autowired
    private AssessmentService assessmentService;
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Assessment> create(@RequestBody @Valid AssessmentDTO dto) {
        return ResponseEntity.ok(assessmentService.createAssessment(dto));
    }
}
```

### Section: Configuration and Security Settings (Spring Security)
**Description:**  
Restricts creation to teachers.

**Design Specification:**  
- Method-level security with `@PreAuthorize`
- JWT or session-based authentication

### Section: Integration Points for External Services
**Description:**  
None for this user story.

### Section: Code Snippets and Pseudo-code Illustrating Design Patterns
**Description:**  
DTO to Entity mapping, validation.

**Sample Implementation:**  
```java
public class AssessmentDTO {
    private String title;
    private String week;
    private String topic;
    private List<QuestionDTO> questions;
}
```

### Section: Database Schema Design
**Description:**  
Normalized tables for assessments, questions, and choices.

**Design Specification:**  
- `assessment(id, title, week, topic, created_by, created_at)`
- `question(id, assertion_text, reason_text, explanation, assessment_id, correct_choice)`
- `answer_choice(id, label, text, question_id)`

---

## USER STORY 2: Student Attempts Assertion and Reason MCQ Assessment

### Section: Spring Boot Architecture Overview
**Description:**  
Students access assessments, submit answers, and receive feedback via REST APIs.

**Design Specification:**  
- REST endpoints for fetching and submitting attempts
- Service layer for evaluation and feedback

### Section: Package Structure, Module Definitions, and Component Breakdown
**Design Specification:**  
- `controller`: `AssessmentAttemptController`
- `service`: `AssessmentAttemptService`
- `repository`: `AssessmentAttemptRepository`
- `domain`: `AssessmentAttempt`, `AttemptedQuestion`

### Section: Entity Design with Domain Models and Relationships (JPA Entities)
**Design Specification:**  
- `AssessmentAttempt` (id, assessment, student, startedAt, completedAt, score, attemptedQuestions)
- `AttemptedQuestion` (id, question, selectedChoice, isCorrect, feedback, assessmentAttempt)

**Sample Implementation:**  
```java
@Entity
public class AssessmentAttempt {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Assessment assessment;
    private String studentUsername;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer score;
    @OneToMany(mappedBy = "assessmentAttempt", cascade = CascadeType.ALL)
    private List<AttemptedQuestion> attemptedQuestions;
}

@Entity
public class AttemptedQuestion {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Question question;
    private String selectedChoice;
    private Boolean isCorrect;
    private String feedback;
    @ManyToOne
    private AssessmentAttempt assessmentAttempt;
}
```

### Section: Service Layer Specifications with Business Logic
**Design Specification:**  
- `AssessmentAttemptService.startAttempt(assessmentId, student)`
- `AssessmentAttemptService.submitAttempt(attemptDTO)`
- Evaluates answers, calculates score, generates feedback

**Sample Implementation:**  
```java
@Service
public class AssessmentAttemptService {
    public AssessmentAttempt submitAttempt(AttemptDTO dto) {
        // Evaluate answers, calculate score, save attempt, generate feedback
    }
}
```

### Section: Repository Layer with Spring Data JPA Repositories
**Design Specification:**  
- `AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, Long>`
- `AttemptedQuestionRepository extends JpaRepository<AttemptedQuestion, Long>`

### Section: Controller Specifications with REST API Endpoints
**Design Specification:**  
- `GET /api/assessments/{id}` (fetch assessment for attempt)
- `POST /api/attempts` (submit answers)

**Sample Implementation:**  
```java
@RestController
@RequestMapping("/api/attempts")
public class AssessmentAttemptController {
    @Autowired
    private AssessmentAttemptService attemptService;
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AssessmentAttempt> submit(@RequestBody AttemptDTO dto) {
        return ResponseEntity.ok(attemptService.submitAttempt(dto));
    }
}
```

### Section: Configuration and Security Settings (Spring Security)
**Design Specification:**  
- Students can only attempt assessments

### Section: Integration Points for External Services
**Description:**  
None for this user story.

### Section: Code Snippets and Pseudo-code Illustrating Design Patterns
**Sample Implementation:**  
```java
public class AttemptDTO {
    private Long assessmentId;
    private List<AttemptedQuestionDTO> answers;
}
```

### Section: Database Schema Design
**Design Specification:**  
- `assessment_attempt(id, assessment_id, student_username, started_at, completed_at, score)`
- `attempted_question(id, question_id, selected_choice, is_correct, feedback, assessment_attempt_id)`

---

## USER STORY 3: Teacher Reviews Student Performance

### Section: Spring Boot Architecture Overview
**Description:**  
Teachers can view individual and aggregate performance via REST APIs.

**Design Specification:**  
- Endpoints for fetching performance data
- Service layer for aggregation

### Section: Package Structure, Module Definitions, and Component Breakdown
**Design Specification:**  
- `controller`: `PerformanceController`
- `service`: `PerformanceService`

### Section: Entity Design with Domain Models and Relationships (JPA Entities)
**Design Specification:**  
- Reuse `AssessmentAttempt` and `AttemptedQuestion`

### Section: Service Layer Specifications with Business Logic
**Design Specification:**  
- `PerformanceService.getStudentPerformance(assessmentId, student)`
- `PerformanceService.getAggregatePerformance(assessmentId)`

**Sample Implementation:**  
```java
@Service
public class PerformanceService {
    public StudentPerformanceDTO getStudentPerformance(Long assessmentId, String student) { ... }
    public AggregatePerformanceDTO getAggregatePerformance(Long assessmentId) { ... }
}
```

### Section: Repository Layer with Spring Data JPA Repositories
**Design Specification:**  
- Use `AssessmentAttemptRepository` with custom queries

### Section: Controller Specifications with REST API Endpoints
**Design Specification:**  
- `GET /api/performance/assessment/{id}/student/{username}`
- `GET /api/performance/assessment/{id}/aggregate`

**Sample Implementation:**  
```java
@RestController
@RequestMapping("/api/performance")
public class PerformanceController {
    @Autowired
    private PerformanceService performanceService;
    @GetMapping("/assessment/{id}/student/{username}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<StudentPerformanceDTO> getStudentPerformance(...) { ... }
    @GetMapping("/assessment/{id}/aggregate")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AggregatePerformanceDTO> getAggregatePerformance(...) { ... }
}
```

### Section: Configuration and Security Settings (Spring Security)
**Design Specification:**  
- Only teachers can access performance data

### Section: Integration Points for External Services
**Description:**  
None for this user story.

### Section: Code Snippets and Pseudo-code Illustrating Design Patterns
**Sample Implementation:**  
```java
public class StudentPerformanceDTO {
    private String student;
    private int score;
    private List<QuestionPerformanceDTO> questions;
}
```

### Section: Database Schema Design
**Design Specification:**  
- No new tables; queries on existing attempt tables

---

## USER STORY 4: Support for Multiple Weeks and Topics

### Section: Spring Boot Architecture Overview
**Description:**  
Assessments are tagged with week and topic metadata for organization.

**Design Specification:**  
- Metadata fields in `Assessment`
- Endpoints for filtering

### Section: Package Structure, Module Definitions, and Component Breakdown
**Design Specification:**  
- `controller`: `AssessmentController` (filter endpoints)
- `service`: `AssessmentService`

### Section: Entity Design with Domain Models and Relationships (JPA Entities)
**Design Specification:**  
- `Assessment` includes `week` and `topic` fields

### Section: Service Layer Specifications with Business Logic
**Design Specification:**  
- `AssessmentService.findByWeekAndTopic(week, topic)`

**Sample Implementation:**  
```java
public List<Assessment> findByWeekAndTopic(String week, String topic) {
    return assessmentRepository.findByWeekAndTopic(week, topic);
}
```

### Section: Repository Layer with Spring Data JPA Repositories
**Design Specification:**  
- `List<Assessment> findByWeekAndTopic(String week, String topic);`

### Section: Controller Specifications with REST API Endpoints
**Design Specification:**  
- `GET /api/assessments?week=1&topic=Biology`

### Section: Configuration and Security Settings (Spring Security)
**Design Specification:**  
- Accessible to authenticated users

### Section: Integration Points for External Services
**Description:**  
None for this user story.

### Section: Code Snippets and Pseudo-code Illustrating Design Patterns
**Sample Implementation:**  
```java
@GetMapping
public List<Assessment> filter(@RequestParam String week, @RequestParam String topic) {
    return assessmentService.findByWeekAndTopic(week, topic);
}
```

### Section: Database Schema Design
**Design Specification:**  
- `assessment` table includes `week` and `topic` columns

---

## USER STORY 5: Provide Explanations for Answers

### Section: Spring Boot Architecture Overview
**Description:**  
Explanations are stored with questions and shown to students after attempts.

**Design Specification:**  
- `explanation` field in `Question`
- API to fetch explanations post-attempt

### Section: Package Structure, Module Definitions, and Component Breakdown
**Design Specification:**  
- `controller`: `AssessmentAttemptController`
- `service`: `AssessmentAttemptService`

### Section: Entity Design with Domain Models and Relationships (JPA Entities)
**Design Specification:**  
- `Question` includes `explanation` field

### Section: Service Layer Specifications with Business Logic
**Design Specification:**  
- After attempt, explanations are included in feedback

**Sample Implementation:**  
```java
public class AttemptedQuestionDTO {
    private Long questionId;
    private String selectedChoice;
    private Boolean isCorrect;
    private String explanation;
}
```

### Section: Repository Layer with Spring Data JPA Repositories
**Design Specification:**  
- No changes needed

### Section: Controller Specifications with REST API Endpoints
**Design Specification:**  
- `GET /api/attempts/{id}/feedback` (returns explanations)

### Section: Configuration and Security Settings (Spring Security)
**Design Specification:**  
- Only the student who attempted can view explanations

### Section: Integration Points for External Services
**Description:**  
None for this user story.

### Section: Code Snippets and Pseudo-code Illustrating Design Patterns
**Sample Implementation:**  
```java
@GetMapping("/{id}/feedback")
@PreAuthorize("hasRole('STUDENT')")
public AttemptFeedbackDTO getFeedback(@PathVariable Long id) { ... }
```

### Section: Database Schema Design
**Design Specification:**  
- `question` table includes `explanation` column

---

## USER STORY 6: Export Assessment Results

### Section: Spring Boot Architecture Overview
**Description:**  
Teachers can export results as CSV or PDF.

**Design Specification:**  
- Export endpoints in controller
- Service for file generation

### Section: Package Structure, Module Definitions, and Component Breakdown
**Design Specification:**  
- `controller`: `ExportController`
- `service`: `ExportService`
- Util classes for CSV/PDF generation

### Section: Entity Design with Domain Models and Relationships (JPA Entities)
**Design Specification:**  
- Uses existing attempt and performance entities

### Section: Service Layer Specifications with Business Logic
**Design Specification:**  
- `ExportService.exportResultsToCSV(assessmentId)`
- `ExportService.exportResultsToPDF(assessmentId)`

**Sample Implementation:**  
```java
@Service
public class ExportService {
    public byte[] exportResultsToCSV(Long assessmentId) { ... }
    public byte[] exportResultsToPDF(Long assessmentId) { ... }
}
```

### Section: Repository Layer with Spring Data JPA Repositories
**Design Specification:**  
- Use `AssessmentAttemptRepository` for data

### Section: Controller Specifications with REST API Endpoints
**Design Specification:**  
- `GET /api/export/assessment/{id}/csv`
- `GET /api/export/assessment/{id}/pdf`

**Sample Implementation:**  
```java
@RestController
@RequestMapping("/api/export")
public class ExportController {
    @Autowired
    private ExportService exportService;
    @GetMapping("/assessment/{id}/csv")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Resource> exportCSV(@PathVariable Long id) { ... }
    @GetMapping("/assessment/{id}/pdf")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Resource> exportPDF(@PathVariable Long id) { ... }
}
```

### Section: Configuration and Security Settings (Spring Security)
**Design Specification:**  
- Only teachers can export

### Section: Integration Points for External Services
**Design Specification:**  
- Optionally integrate with email/file storage for delivery

### Section: Code Snippets and Pseudo-code Illustrating Design Patterns
**Sample Implementation:**  
```java
public byte[] exportResultsToCSV(Long assessmentId) {
    // Fetch attempts, write to CSV, return bytes
}
```

### Section: Database Schema Design
**Design Specification:**  
- No new tables; uses existing attempt tables

---

# End of Document

This document provides a comprehensive, low-level technical design for all user stories, following Spring Boot best practices and ensuring clarity for developers.