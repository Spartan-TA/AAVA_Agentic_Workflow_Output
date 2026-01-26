# Low-Level Technical Design Document
## Spring Boot Application - Comprehensive Design Specification

---

## Document Information
- **Version**: 1.0
- **Date**: 2024
- **Framework**: Spring Boot 3.x
- **Java Version**: 17+
- **Build Tool**: Maven/Gradle

---

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Package Structure](#package-structure)
3. [Domain Model Design](#domain-model-design)
4. [Repository Layer](#repository-layer)
5. [Service Layer](#service-layer)
6. [Controller Layer](#controller-layer)
7. [Security Configuration](#security-configuration)
8. [Exception Handling](#exception-handling)
9. [Configuration Management](#configuration-management)
10. [Integration Points](#integration-points)
11. [Testing Strategy](#testing-strategy)

---

## 1. Architecture Overview

### Section: Application Architecture
**Description**: The application follows a layered architecture pattern with clear separation of concerns. The architecture is based on Spring Boot best practices, implementing the MVC pattern with additional service and repository layers.

**Design Specification**:
- **Presentation Layer**: REST Controllers handling HTTP requests/responses
- **Service Layer**: Business logic implementation
- **Repository Layer**: Data access abstraction using Spring Data JPA
- **Domain Layer**: Entity models and DTOs
- **Infrastructure Layer**: Configuration, security, and cross-cutting concerns

**Sample Implementation**:
```java
// Application Entry Point
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.application.repository")
@EntityScan(basePackages = "com.application.domain.entity")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

---

## 2. Package Structure

### Section: Package Organization
**Description**: The package structure follows Spring Boot conventions and domain-driven design principles, ensuring modularity and maintainability.

**Design Specification**:
```
com.application
âââ config
â   âââ SecurityConfig.java
â   âââ DatabaseConfig.java
â   âââ SwaggerConfig.java
â   âââ ApplicationConfig.java
âââ controller
â   âââ UserController.java
â   âââ ProductController.java
â   âââ OrderController.java
âââ service
â   âââ UserService.java
â   âââ ProductService.java
â   âââ OrderService.java
â   âââ impl
â       âââ UserServiceImpl.java
â       âââ ProductServiceImpl.java
â       âââ OrderServiceImpl.java
âââ repository
â   âââ UserRepository.java
â   âââ ProductRepository.java
â   âââ OrderRepository.java
âââ domain
â   âââ entity
â   â   âââ User.java
â   â   âââ Product.java
â   â   âââ Order.java
â   âââ dto
â   â   âââ UserDTO.java
â   â   âââ ProductDTO.java
â   â   âââ OrderDTO.java
â   âââ mapper
â       âââ UserMapper.java
â       âââ ProductMapper.java
â       âââ OrderMapper.java
âââ exception
â   âââ GlobalExceptionHandler.java
â   âââ ResourceNotFoundException.java
â   âââ BusinessException.java
â   âââ ValidationException.java
âââ security
â   âââ JwtTokenProvider.java
â   âââ JwtAuthenticationFilter.java
â   âââ UserDetailsServiceImpl.java
âââ util
    âââ DateUtil.java
    âââ ValidationUtil.java
    âââ Constants.java
```

---

## 3. Domain Model Design

### Section: Entity Design - User Story 1: User Management
**Description**: Design entities with JPA annotations, implementing proper relationships, validation, and auditing capabilities.

**Design Specification**:
- Use `@Entity` annotation for JPA entities
- Implement `@Table` with proper naming strategy
- Use `@Id` and `@GeneratedValue` for primary keys
- Implement auditing with `@CreatedDate` and `@LastModifiedDate`
- Use `@Column` for field-level constraints
- Implement proper relationships with `@OneToMany`, `@ManyToOne`, `@ManyToMany`

**Sample Implementation**:
```java
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Username is required")
    private String username;
    
    @Column(name = "email", unique = true, nullable = false, length = 100)
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;
    
    @Column(name = "password", nullable = false)
    @NotBlank(message = "Password is required")
    private String password;
    
    @Column(name = "first_name", length = 50)
    private String firstName;
    
    @Column(name = "last_name", length = 50)
    private String lastName;
    
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
}

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "name", unique = true, nullable = false)
    private RoleName name;
    
    @Column(name = "description")
    private String description;
}

public enum UserStatus {
    ACTIVE, INACTIVE, SUSPENDED, DELETED
}

public enum RoleName {
    ROLE_USER, ROLE_ADMIN, ROLE_MODERATOR
}
```

### Section: Entity Design - User Story 2: Product Management
**Description**: Product entity with category relationships and inventory tracking.

**Sample Implementation**:
```java
@Entity
@Table(name = "products")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 200)
    @NotBlank(message = "Product name is required")
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "sku", unique = true, nullable = false, length = 50)
    @NotBlank(message = "SKU is required")
    private String sku;
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    @Column(name = "quantity", nullable = false)
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductStatus status = ProductStatus.AVAILABLE;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
}

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", unique = true, nullable = false, length = 100)
    @NotBlank(message = "Category name is required")
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Category> subCategories = new ArrayList<>();
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Product> products = new ArrayList<>();
}

public enum ProductStatus {
    AVAILABLE, OUT_OF_STOCK, DISCONTINUED
}
```

### Section: Entity Design - User Story 3: Order Management
**Description**: Order entity with order items and payment tracking.

**Sample Implementation**:
```java
@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_number", unique = true, nullable = false, length = 50)
    private String orderNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @Column(name = "shipping_address", nullable = false, columnDefinition = "TEXT")
    private String shippingAddress;
    
    @Column(name = "billing_address", nullable = false, columnDefinition = "TEXT")
    private String billingAddress;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
}

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "quantity", nullable = false)
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
}

public enum OrderStatus {
    PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED
}

public enum PaymentStatus {
    PENDING, PAID, FAILED, REFUNDED
}
```

### Section: DTO Design
**Description**: Data Transfer Objects for API communication, separating internal entity structure from external API contracts.

**Sample Implementation**:
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;
    
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UserStatus status;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;
    
    @NotBlank(message = "Product name is required")
    private String name;
    
    private String description;
    
    @NotBlank(message = "SKU is required")
    private String sku;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;
    
    private Long categoryId;
    private String categoryName;
    private String imageUrl;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    private Long id;
    private String orderNumber;
    private Long userId;
    private String username;
    private List<OrderItemDTO> orderItems;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String shippingAddress;
    private String billingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
```

---

## 4. Repository Layer

### Section: Repository Design - User Story 1: User Repository
**Description**: Spring Data JPA repositories providing data access abstraction with custom query methods.

**Design Specification**:
- Extend `JpaRepository` for CRUD operations
- Use method naming conventions for query derivation
- Implement custom queries with `@Query` annotation
- Use `@Modifying` for update/delete operations
- Implement pagination and sorting support

**Sample Implementation**:
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> findByStatus(UserStatus status);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") RoleName roleName);
    
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findByCreatedAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    Page<User> findByFirstNameContainingOrLastNameContaining(
        String firstName, 
        String lastName, 
        Pageable pageable
    );
    
    @Modifying
    @Query("UPDATE User u SET u.status = :status WHERE u.id = :userId")
    int updateUserStatus(@Param("userId") Long userId, @Param("status") UserStatus status);
}
```

### Section: Repository Design - User Story 2: Product Repository
**Description**: Product repository with advanced search and filtering capabilities.

**Sample Implementation**:
```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findBySku(String sku);
    
    List<Product> findByStatus(ProductStatus status);
    
    List<Product> findByCategoryId(Long categoryId);
    
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.status = :status")
    List<Product> findByCategoryIdAndStatus(
        @Param("categoryId") Long categoryId,
        @Param("status") ProductStatus status
    );
    
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceRange(
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice
    );
    
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.quantity < :threshold")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);
    
    @Modifying
    @Query("UPDATE Product p SET p.quantity = p.quantity - :quantity WHERE p.id = :productId AND p.quantity >= :quantity")
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    @Modifying
    @Query("UPDATE Product p SET p.quantity = p.quantity + :quantity WHERE p.id = :productId")
    int increaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByName(String name);
    
    List<Category> findByParentIsNull();
    
    List<Category> findByParentId(Long parentId);
    
    boolean existsByName(String name);
}
```

### Section: Repository Design - User Story 3: Order Repository
**Description**: Order repository with complex queries for order management and reporting.

**Sample Implementation**:
```java
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    List<Order> findByUserId(Long userId);
    
    List<Order> findByStatus(OrderStatus status);
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = :status")
    List<Order> findByUserIdAndStatus(
        @Param("userId") Long userId,
        @Param("status") OrderStatus status
    );
    
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT o FROM Order o JOIN FETCH o.orderItems WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = :status " +
           "AND o.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalRevenue(
        @Param("status") OrderStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    Long countOrdersByUserId(@Param("userId") Long userId);
    
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    List<OrderItem> findByOrderId(Long orderId);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.product.id = :productId")
    List<OrderItem> findByProductId(@Param("productId") Long productId);
    
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product.id = :productId")
    Long getTotalQuantitySoldByProduct(@Param("productId") Long productId);
}
```

---

## 5. Service Layer

### Section: Service Design - User Story 1: User Service
**Description**: Service layer implementing business logic for user management with transaction management and validation.

**Design Specification**:
- Define service interfaces for abstraction
- Implement service classes with `@Service` annotation
- Use `@Transactional` for transaction management
- Implement proper exception handling
- Use DTOs for data transfer
- Implement MapStruct or ModelMapper for entity-DTO conversion

**Sample Implementation**:
```java
public interface UserService {
    UserDTO createUser(UserDTO userDTO);
    UserDTO updateUser(Long id, UserDTO userDTO);
    UserDTO getUserById(Long id);
    UserDTO getUserByUsername(String username);
    List<UserDTO> getAllUsers();
    Page<UserDTO> getUsersWithPagination(int page, int size, String sortBy);
    void deleteUser(Long id);
    void changeUserStatus(Long id, UserStatus status);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}

@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public UserServiceImpl(
        UserRepository userRepository,
        UserMapper userMapper,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public UserDTO createUser(UserDTO userDTO) {
        log.info("Creating new user with username: {}", userDTO.getUsername());
        
        // Validate uniqueness
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new BusinessException("Username already exists: " + userDTO.getUsername());
        }
        
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BusinessException("Email already exists: " + userDTO.getEmail());
        }
        
        // Map DTO to Entity
        User user = userMapper.toEntity(userDTO);
        
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Set default status
        user.setStatus(UserStatus.ACTIVE);
        
        // Save user
        User savedUser = userRepository.save(user);
        
        log.info("User created successfully with ID: {}", savedUser.getId());
        return userMapper.toDTO(savedUser);
    }
    
    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.info("Updating user with ID: {}", id);
        
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        // Update fields
        existingUser.setFirstName(userDTO.getFirstName());
        existingUser.setLastName(userDTO.getLastName());
        existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        
        // Check email uniqueness if changed
        if (!existingUser.getEmail().equals(userDTO.getEmail())) {
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new BusinessException("Email already exists: " + userDTO.getEmail());
            }
            existingUser.setEmail(userDTO.getEmail());
        }
        
        User updatedUser = userRepository.save(existingUser);
        
        log.info("User updated successfully with ID: {}", updatedUser.getId());
        return userMapper.toDTO(updatedUser);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        log.info("Fetching user with ID: {}", id);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        return userMapper.toDTO(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        log.info("Fetching user with username: {}", username);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        return userMapper.toDTO(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        log.info("Fetching all users");
        
        List<User> users = userRepository.findAll();
        return users.stream()
            .map(userMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getUsersWithPagination(int page, int size, String sortBy) {
        log.info("Fetching users with pagination - page: {}, size: {}, sortBy: {}", page, size, sortBy);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<User> userPage = userRepository.findAll(pageable);
        
        return userPage.map(userMapper::toDTO);
    }
    
    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        userRepository.delete(user);
        
        log.info("User deleted successfully with ID: {}", id);
    }
    
    @Override
    public void changeUserStatus(Long id, UserStatus status) {
        log.info("Changing status for user ID: {} to {}", id, status);
        
        int updated = userRepository.updateUserStatus(id, status);
        
        if (updated == 0) {
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }
        
        log.info("User status changed successfully");
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
```

### Section: Service Design - User Story 2: Product Service
**Description**: Product service with inventory management and search capabilities.

**Sample Implementation**:
```java
public interface ProductService {
    ProductDTO createProduct(ProductDTO productDTO);
    ProductDTO updateProduct(Long id, ProductDTO productDTO);
    ProductDTO getProductById(Long id);
    ProductDTO getProductBySku(String sku);
    List<ProductDTO> getAllProducts();
    Page<ProductDTO> getProductsWithPagination(int page, int size, String sortBy);
    List<ProductDTO> getProductsByCategory(Long categoryId);
    Page<ProductDTO> searchProducts(String keyword, int page, int size);
    void deleteProduct(Long id);
    void updateStock(Long id, Integer quantity);
    List<ProductDTO> getLowStockProducts(Integer threshold);
}

@Service
@Transactional
@Slf4j
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    
    @Autowired
    public ProductServiceImpl(
        ProductRepository productRepository,
        CategoryRepository categoryRepository,
        ProductMapper productMapper
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }
    
    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating new product with SKU: {}", productDTO.getSku());
        
        // Validate SKU uniqueness
        if (productRepository.findBySku(productDTO.getSku()).isPresent()) {
            throw new BusinessException("Product with SKU already exists: " + productDTO.getSku());
        }
        
        // Validate category
        Category category = categoryRepository.findById(productDTO.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + productDTO.getCategoryId()));
        
        // Map DTO to Entity
        Product product = productMapper.toEntity(productDTO);
        product.setCategory(category);
        product.setStatus(ProductStatus.AVAILABLE);
        
        // Save product
        Product savedProduct = productRepository.save(product);
        
        log.info("Product created successfully with ID: {}", savedProduct.getId());
        return productMapper.toDTO(savedProduct);
    }
    
    @Override
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("Updating product with ID: {}", id);
        
        Product existingProduct = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        // Update fields
        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setQuantity(productDTO.getQuantity());
        existingProduct.setImageUrl(productDTO.getImageUrl());
        
        // Update category if changed
        if (!existingProduct.getCategory().getId().equals(productDTO.getCategoryId())) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + productDTO.getCategoryId()));
            existingProduct.setCategory(category);
        }
        
        // Update status based on quantity
        if (existingProduct.getQuantity() == 0) {
            existingProduct.setStatus(ProductStatus.OUT_OF_STOCK);
        } else {
            existingProduct.setStatus(ProductStatus.AVAILABLE);
        }
        
        Product updatedProduct = productRepository.save(existingProduct);
        
        log.info("Product updated successfully with ID: {}", updatedProduct.getId());
        return productMapper.toDTO(updatedProduct);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.info("Fetching product with ID: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        return productMapper.toDTO(product);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductBySku(String sku) {
        log.info("Fetching product with SKU: {}", sku);
        
        Product product = productRepository.findBySku(sku)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
        
        return productMapper.toDTO(product);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        log.info("Fetching all products");
        
        List<Product> products = productRepository.findAll();
        return products.stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsWithPagination(int page, int size, String sortBy) {
        log.info("Fetching products with pagination - page: {}, size: {}, sortBy: {}", page, size, sortBy);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Product> productPage = productRepository.findAll(pageable);
        
        return productPage.map(productMapper::toDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        log.info("Fetching products for category ID: {}", categoryId);
        
        List<Product> products = productRepository.findByCategoryId(categoryId);
        return products.stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProducts(String keyword, int page, int size) {
        log.info("Searching products with keyword: {}", keyword);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.searchProducts(keyword, pageable);
        
        return productPage.map(productMapper::toDTO);
    }
    
    @Override
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        productRepository.delete(product);
        
        log.info("Product deleted successfully with ID: {}", id);
    }
    
    @Override
    public void updateStock(Long id, Integer quantity) {
        log.info("Updating stock for product ID: {} with quantity: {}", id, quantity);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        product.setQuantity(quantity);
        
        if (quantity == 0) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        } else {
            product.setStatus(ProductStatus.AVAILABLE);
        }
        
        productRepository.save(product);
        
        log.info("Stock updated successfully for product ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getLowStockProducts(Integer threshold) {
        log.info("Fetching low stock products with threshold: {}", threshold);
        
        List<Product> products = productRepository.findLowStockProducts(threshold);
        return products.stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    }
}
```

### Section: Service Design - User Story 3: Order Service
**Description**: Order service with complex business logic for order processing and payment handling.

**Sample Implementation**:
```java
public interface OrderService {
    OrderDTO createOrder(OrderDTO orderDTO);
    OrderDTO updateOrderStatus(Long id, OrderStatus status);
    OrderDTO getOrderById(Long id);
    OrderDTO getOrderByOrderNumber(String orderNumber);
    List<OrderDTO> getOrdersByUserId(Long userId);
    Page<OrderDTO> getOrdersWithPagination(int page, int size, String sortBy);
    void cancelOrder(Long id);
    BigDecimal calculateTotalRevenue(LocalDateTime startDate, LocalDateTime endDate);
}

@Service
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    
    @Autowired
    public OrderServiceImpl(
        OrderRepository orderRepository,
        UserRepository userRepository,
        ProductRepository productRepository,
        OrderMapper orderMapper
    ) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderMapper = orderMapper;
    }
    
    @Override
    public OrderDTO createOrder(OrderDTO orderDTO) {
        log.info("Creating new order for user ID: {}", orderDTO.getUserId());
        
        // Validate user
        User user = userRepository.findById(orderDTO.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + orderDTO.getUserId()));
        
        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setShippingAddress(orderDTO.getShippingAddress());
        order.setBillingAddress(orderDTO.getBillingAddress());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        
        // Process order items
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (OrderItemDTO itemDTO : orderDTO.getOrderItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + itemDTO.getProductId()));
            
            // Check stock availability
            if (product.getQuantity() < itemDTO.getQuantity()) {
                throw new BusinessException("Insufficient stock for product: " + product.getName());
            }
            
            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
            
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubtotal());
            
            // Decrease stock
            int updated = productRepository.decreaseStock(product.getId(), itemDTO.getQuantity());
            if (updated == 0) {
                throw new BusinessException("Failed to update stock for product: " + product.getName());
            }
        }
        
        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        
        log.info("Order created successfully with ID: {} and order number: {}", savedOrder.getId(), savedOrder.getOrderNumber());
        return orderMapper.toDTO(savedOrder);
    }
    
    @Override
    public OrderDTO updateOrderStatus(Long id, OrderStatus status) {
        log.info("Updating order status for ID: {} to {}", id, status);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        
        // Validate status transition
        validateStatusTransition(order.getStatus(), status);
        
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        
        log.info("Order status updated successfully for ID: {}", id);
        return orderMapper.toDTO(updatedOrder);
    }
    
    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        log.info("Fetching order with ID: {}", id);
        
        Order order = orderRepository.findByIdWithItems(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        
        return orderMapper.toDTO(order);
    }
    
    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderByOrderNumber(String orderNumber) {
        log.info("Fetching order with order number: {}", orderNumber);
        
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));
        
        return orderMapper.toDTO(order);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByUserId(Long userId) {
        log.info("Fetching orders for user ID: {}", userId);
        
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
            .map(orderMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersWithPagination(int page, int size, String sortBy) {
        log.info("Fetching orders with pagination - page: {}, size: {}, sortBy: {}", page, size, sortBy);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Order> orderPage = orderRepository.findAll(pageable);
        
        return orderPage.map(orderMapper::toDTO);
    }
    
    @Override
    public void cancelOrder(Long id) {
        log.info("Cancelling order with ID: {}", id);
        
        Order order = orderRepository.findByIdWithItems(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        
        // Validate cancellation
        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException("Cannot cancel order in status: " + order.getStatus());
        }
        
        // Restore stock
        for (OrderItem item : order.getOrderItems()) {
            productRepository.increaseStock(item.getProduct().getId(), item.getQuantity());
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        
        log.info("Order cancelled successfully with ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating total revenue from {} to {}", startDate, endDate);
        
        BigDecimal revenue = orderRepository.calculateTotalRevenue(OrderStatus.DELIVERED, startDate, endDate);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
    
    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }
    
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Implement status transition validation logic
        if (currentStatus == OrderStatus.CANCELLED || currentStatus == OrderStatus.DELIVERED) {
            throw new BusinessException("Cannot change status from " + currentStatus);
        }
    }
}
```

---

## 6. Controller Layer

### Section: Controller Design - User Story 1: User Controller
**Description**: REST controllers exposing API endpoints with proper request/response handling, validation, and documentation.

**Design Specification**:
- Use `@RestController` annotation
- Implement `@RequestMapping` for base path
- Use appropriate HTTP method annotations (`@GetMapping`, `@PostMapping`, etc.)
- Implement `@Valid` for request validation
- Use `ResponseEntity` for proper HTTP responses
- Implement Swagger/OpenAPI documentation

**Sample Implementation**:
```java
@RestController
@RequestMapping("/api/v1/users")
@Validated
@Slf4j
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {
    
    private final UserService userService;
    
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping
    @Operation(summary = "Create a new user", description = "Creates a new user in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "User already exists")
    })
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody UserDTO userDTO) {
        log.info("REST request to create user: {}", userDTO.getUsername());
        
        UserDTO createdUser = userService.createUser(userDTO);
        
        ApiResponse<UserDTO> response = ApiResponse.<UserDTO>builder()
            .success(true)
            .message("User created successfully")
            .data(createdUser)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates an existing user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
        @PathVariable Long id,
        @Valid @RequestBody UserDTO userDTO
    ) {
        log.info("REST request to update user with ID: {}", id);
        
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        
        ApiResponse<UserDTO> response = ApiResponse.<UserDTO>builder()
            .success(true)
            .message("User updated successfully")
            .data(updatedUser)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves a user by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        log.info("REST request to get user with ID: {}", id);
        
        UserDTO user = userService.getUserById(id);
        
        ApiResponse<UserDTO> response = ApiResponse.<UserDTO>builder()
            .success(true)
            .message("User retrieved successfully")
            .data(user)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves all users with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    })
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getAllUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy
    ) {
        log.info("REST request to get all users - page: {}, size: {}, sortBy: {}", page, size, sortBy);
        
        Page<UserDTO> users = userService.getUsersWithPagination(page, size, sortBy);
        
        ApiResponse<Page<UserDTO>> response = ApiResponse.<Page<UserDTO>>builder()
            .success(true)
            .message("Users retrieved successfully")
            .data(users)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Deletes a user by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("REST request to delete user with ID: {}", id);
        
        userService.deleteUser(id);
        
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/status")
    @Operation(summary = "Change user status", description = "Changes the status of a user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User status changed successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<Void>> changeUserStatus(
        @PathVariable Long id,
        @RequestParam UserStatus status
    ) {
        log.info("REST request to change status for user ID: {} to {}", id, status);
        
        userService.changeUserStatus(id, status);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
            .success(true)
            .message("User status changed successfully")
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
}
```

### Section: Controller Design - User Story 2: Product Controller
**Description**: Product management REST endpoints with search and filtering capabilities.

**Sample Implementation**:
```java
@RestController
@RequestMapping("/api/v1/products")
@Validated
@Slf4j
@Tag(name = "Product Management", description = "APIs for managing products")
public class ProductController {
    
    private final ProductService productService;
    
    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    @PostMapping
    @Operation(summary = "Create a new product", description = "Creates a new product in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Product already exists")
    })
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        log.info("REST request to create product: {}", productDTO.getName());
        
        ProductDTO createdProduct = productService.createProduct(productDTO);
        
        ApiResponse<ProductDTO> response = ApiResponse.<ProductDTO>builder()
            .success(true)
            .message("Product created successfully")
            .data(createdProduct)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Updates an existing product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product updated successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
        @PathVariable Long id,
        @Valid @RequestBody ProductDTO productDTO
    ) {
        log.info("REST request to update product with ID: {}", id);
        
        ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
        
        ApiResponse<ProductDTO> response = ApiResponse.<ProductDTO>builder()
            .success(true)
            .message("Product updated successfully")
            .data(updatedProduct)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves a product by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product found"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Long id) {
        log.info("REST request to get product with ID: {}", id);
        
        ProductDTO product = productService.getProductById(id);
        
        ApiResponse<ProductDTO> response = ApiResponse.<ProductDTO>builder()
            .success(true)
            .message("Product retrieved successfully")
            .data(product)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieves all products with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    })
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getAllProducts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy
    ) {
        log.info("REST request to get all products - page: {}, size: {}, sortBy: {}", page, size, sortBy);
        
        Page<ProductDTO> products = productService.getProductsWithPagination(page, size, sortBy);
        
        ApiResponse<Page<ProductDTO>> response = ApiResponse.<Page<ProductDTO>>builder()
            .success(true)
            .message("Products retrieved successfully")
            .data(products)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Searches products by keyword")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products found")
    })
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> searchProducts(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        log.info("REST request to search products with keyword: {}", keyword);
        
        Page<ProductDTO> products = productService.searchProducts(keyword, page, size);
        
        ApiResponse<Page<ProductDTO>> response = ApiResponse.<Page<ProductDTO>>builder()
            .success(true)
            .message("Products found")
            .data(products)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category", description = "Retrieves products by category ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProductsByCategory(@PathVariable Long categoryId) {
        log.info("REST request to get products for category ID: {}", categoryId);
        
        List<ProductDTO> products = productService.getProductsByCategory(categoryId);
        
        ApiResponse<List<ProductDTO>> response = ApiResponse.<List<ProductDTO>>builder()
            .success(true)
            .message("Products retrieved successfully")
            .data(products)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Deletes a product by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("REST request to delete product with ID: {}", id);
        
        productService.deleteProduct(id);
        
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/stock")
    @Operation(summary = "Update product stock", description = "Updates the stock quantity of a product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock updated successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ApiResponse<Void>> updateStock(
        @PathVariable Long id,
        @RequestParam Integer quantity
    ) {
        log.info("REST request to update stock for product ID: {} with quantity: {}", id, quantity);
        
        productService.updateStock(id, quantity);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
            .success(true)
            .message("Stock updated successfully")
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
}
```

### Section: Controller Design - User Story 3: Order Controller
**Description**: Order management REST endpoints with order processing and tracking.

**Sample Implementation**:
```java
@RestController
@RequestMapping("/api/v1/orders")
@Validated
@Slf4j
@Tag(name = "Order Management", description = "APIs for managing orders")
public class OrderController {
    
    private final OrderService orderService;
    
    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @PostMapping
    @Operation(summary = "Create a new order", description = "Creates a new order in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "User or product not found")
    })
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(@Valid @RequestBody OrderDTO orderDTO) {
        log.info("REST request to create order for user ID: {}", orderDTO.getUserId());
        
        OrderDTO createdOrder = orderService.createOrder(orderDTO);
        
        ApiResponse<OrderDTO> response = ApiResponse.<OrderDTO>builder()
            .success(true)
            .message("Order created successfully")
            .data(createdOrder)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieves an order by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order found"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@PathVariable Long id) {
        log.info("REST request to get order with ID: {}", id);
        
        OrderDTO order = orderService.getOrderById(id);
        
        ApiResponse<OrderDTO> response = ApiResponse.<OrderDTO>builder()
            .success(true)
            .message("Order retrieved successfully")
            .data(order)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/order-number/{orderNumber}")
    @Operation(summary = "Get order by order number", description = "Retrieves an order by its order number")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order found"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderByOrderNumber(@PathVariable String orderNumber) {
        log.info("REST request to get order with order number: {}", orderNumber);
        
        OrderDTO order = orderService.getOrderByOrderNumber(orderNumber);
        
        ApiResponse<OrderDTO> response = ApiResponse.<OrderDTO>builder()
            .success(true)
            .message("Order retrieved successfully")
            .data(order)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get orders by user ID", description = "Retrieves all orders for a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersByUserId(@PathVariable Long userId) {
        log.info("REST request to get orders for user ID: {}", userId);
        
        List<OrderDTO> orders = orderService.getOrdersByUserId(userId);
        
        ApiResponse<List<OrderDTO>> response = ApiResponse.<List<OrderDTO>>builder()
            .success(true)
            .message("Orders retrieved successfully")
            .data(orders)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieves all orders with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    })
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getAllOrders(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        log.info("REST request to get all orders - page: {}, size: {}, sortBy: {}", page, size, sortBy);
        
        Page<OrderDTO> orders = orderService.getOrdersWithPagination(page, size, sortBy);
        
        ApiResponse<Page<OrderDTO>> response = ApiResponse.<Page<OrderDTO>>builder()
            .success(true)
            .message("Orders retrieved successfully")
            .data(orders)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status", description = "Updates the status of an order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "400", description = "Invalid status transition")
    })
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
        @PathVariable Long id,
        @RequestParam OrderStatus status
    ) {
        log.info("REST request to update order status for ID: {} to {}", id, status);
        
        OrderDTO updatedOrder = orderService.updateOrderStatus(id, status);
        
        ApiResponse<OrderDTO> response = ApiResponse.<OrderDTO>builder()
            .success(true)
            .message("Order status updated successfully")
            .data(updatedOrder)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel order", description = "Cancels an order by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Order cancelled successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "400", description = "Order cannot be cancelled")
    })
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        log.info("REST request to cancel order with ID: {}", id);
        
        orderService.cancelOrder(id);
        
        return ResponseEntity.noContent().build();
    }
}
```

### Section: API Response Wrapper
**Description**: Standardized API response structure for consistent client communication.

**Sample Implementation**:
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private List<String> errors;
}
```

---

## 7. Security Configuration

### Section: Security Configuration - JWT Authentication
**Description**: Comprehensive security configuration implementing JWT-based authentication and authorization.

**Design Specification**:
- Implement Spring Security with JWT tokens
- Configure authentication and authorization
- Implement password encoding
- Configure CORS and CSRF
- Implement role-based access control

**Sample Implementation**:
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    
    @Autowired
    public SecurityConfig(
        JwtAuthenticationFilter jwtAuthenticationFilter,
        UserDetailsService userDetailsService
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/v1/users/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/v1/products/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/v1/orders/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpirationMs;
    
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .getBody();
        
        return claims.getSubject();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }
}

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    
    @Autowired
    public JwtAuthenticationFilter(
        JwtTokenProvider tokenProvider,
        UserDetailsService userDetailsService
    ) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String username = tokenProvider.getUsernameFromToken(jwt);
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .authorities(user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList()))
            .accountExpired(false)
            .accountLocked(user.getStatus() == UserStatus.SUSPENDED)
            .credentialsExpired(false)
            .disabled(user.getStatus() == UserStatus.INACTIVE)
            .build();
    }
}
```

---

## 8. Exception Handling

### Section: Global Exception Handler
**Description**: Centralized exception handling for consistent error responses across the application.

**Design Specification**:
- Use `@ControllerAdvice` for global exception handling
- Implement custom exception classes
- Return standardized error responses
- Log exceptions appropriately

**Sample Implementation**:
```java
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .success(false)
            .message(ex.getMessage())
            .errorCode("RESOURCE_NOT_FOUND")
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.error("Business exception: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .success(false)
            .message(ex.getMessage())
            .errorCode("BUSINESS_ERROR")
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());
        
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .success(false)
            .message("Validation failed")
            .errorCode("VALIDATION_ERROR")
            .errors(errors)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .success(false)
            .message("Access denied")
            .errorCode("ACCESS_DENIED")
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .success(false)
            .message("An unexpected error occurred")
            .errorCode("INTERNAL_SERVER_ERROR")
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private boolean success;
    private String message;
    private String errorCode;
    private List<String> errors;
    private LocalDateTime timestamp;
}

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
```

---

## 9. Configuration Management

### Section: Application Configuration
**Description**: Externalized configuration using Spring Boot properties and profiles.

**Design Specification**:
- Use `application.yml` or `application.properties`
- Implement profile-specific configurations
- Use `@ConfigurationProperties` for type-safe configuration
- Externalize sensitive data

**Sample Implementation**:
```yaml
# application.yml
spring:
  application:
    name: spring-boot-application
  
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:password}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        use_sql_comments: true
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
    open-in-view: false
  
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
  
  jackson:
    serialization:
      write-dates-as-timestamps: false
    time-zone: UTC
    default-property-inclusion: non_null

jwt:
  secret: ${JWT_SECRET:mySecretKey123456789012345678901234567890}
  expiration: 86400000 # 24 hours

logging:
  level:
    root: INFO
    com.application: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/application.log
    max-size: 10MB
    max-history: 30

server:
  port: 8080
  servlet:
    context-path: /
  error:
    include-message: always
    include-binding-errors: always
    include-stacktrace: on_param
    include-exception: false

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operations-sorter: method
    tags-sorter: alpha

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true

---
# application-dev.yml
spring:
  config:
    activate:
      on-profile: dev
  
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: update

logging:
  level:
    root: DEBUG

---
# application-prod.yml
spring:
  config:
    activate:
      on-profile: prod
  
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate

logging:
  level:
    root: WARN
    com.application: INFO
```

### Section: Swagger/OpenAPI Configuration
**Description**: API documentation configuration using SpringDoc OpenAPI.

**Sample Implementation**:
```java
@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Spring Boot Application API")
                .version("1.0")
                .description("Comprehensive API documentation for Spring Boot application")
                .contact(new Contact()
                    .name("Development Team")
                    .email("dev@example.com")
                    .url("https://example.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Enter JWT token")));
    }
}
```

---

## 10. Integration Points

### Section: External Service Integration
**Description**: Integration with external services using RestTemplate or WebClient.

**Design Specification**:
- Use `RestTemplate` or `WebClient` for HTTP communication
- Implement retry logic and circuit breaker patterns
- Handle timeouts and connection pooling
- Implement proper error handling

**Sample Implementation**:
```java
@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(5))
            .errorHandler(new CustomResponseErrorHandler())
            .build();
    }
    
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
            .baseUrl("https://api.external-service.com")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}

@Service
@Slf4j
public class ExternalServiceClient {
    
    private final RestTemplate restTemplate;
    private final WebClient webClient;
    
    @Autowired
    public ExternalServiceClient(RestTemplate restTemplate, WebClient webClient) {
        this.restTemplate = restTemplate;
        this.webClient = webClient;
    }
    
    public ExternalDataDTO fetchDataFromExternalService(String id) {
        log.info("Fetching data from external service for ID: {}", id);
        
        try {
            ResponseEntity<ExternalDataDTO> response = restTemplate.getForEntity(
                "https://api.external-service.com/data/{id}",
                ExternalDataDTO.class,
                id
            );
            
            return response.getBody();
        } catch (RestClientException ex) {
            log.error("Error fetching data from external service: {}", ex.getMessage());
            throw new BusinessException("Failed to fetch data from external service");
        }
    }
    
    public Mono<ExternalDataDTO> fetchDataReactive(String id) {
        log.info("Fetching data reactively from external service for ID: {}", id);
        
        return webClient.get()
            .uri("/data/{id}", id)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, response -> 
                Mono.error(new BusinessException("Client error from external service")))
            .onStatus(HttpStatusCode::is5xxServerError, response -> 
                Mono.error(new BusinessException("Server error from external service")))
            .bodyToMono(ExternalDataDTO.class)
            .doOnError(error -> log.error("Error in reactive call: {}", error.getMessage()));
    }
}

public class CustomResponseErrorHandler implements ResponseErrorHandler {
    
    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().is4xxClientError() || 
               response.getStatusCode().is5xxServerError();
    }
    
    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if (response.getStatusCode().is4xxClientError()) {
            throw new BusinessException("Client error: " + response.getStatusCode());
        } else if (response.getStatusCode().is5xxServerError()) {
            throw new BusinessException("Server error: " + response.getStatusCode());
        }
    }
}
```

### Section: Message Queue Integration
**Description**: Integration with message brokers like RabbitMQ or Kafka.

**Sample Implementation**:
```java
@Configuration
public class RabbitMQConfig {
    
    public static final String QUEUE_NAME = "order.queue";
    public static final String EXCHANGE_NAME = "order.exchange";
    public static final String ROUTING_KEY = "order.routing.key";
    
    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, true);
    }
    
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }
    
    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }
    
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}

@Service
@Slf4j
public class MessagePublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    @Autowired
    public MessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    public void publishOrderCreatedEvent(OrderDTO order) {
        log.info("Publishing order created event for order: {}", order.getOrderNumber());
        
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME,
            RabbitMQConfig.ROUTING_KEY,
            order
        );
    }
}

@Component
@Slf4j
public class MessageConsumer {
    
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleOrderCreatedEvent(OrderDTO order) {
        log.info("Received order created event for order: {}", order.getOrderNumber());
        
        // Process the order event
        // Send notifications, update inventory, etc.
    }
}
```

---

## 11. Testing Strategy

### Section: Unit Testing
**Description**: Comprehensive unit testing strategy using JUnit 5 and Mockito.

**Sample Implementation**:
```java
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    private User user;
    private UserDTO userDTO;
    
    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setStatus(UserStatus.ACTIVE);
        
        userDTO = UserDTO.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .status(UserStatus.ACTIVE)
            .build();
    }
    
    @Test
    void createUser_Success() {
        // Given
        when(userRepository.existsByUsername(userDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(userDTO.getEmail())).thenReturn(false);
        when(userMapper.toEntity(userDTO)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userDTO);
        
        // When
        UserDTO result = userService.createUser(userDTO);
        
        // Then
        assertNotNull(result);
        assertEquals(userDTO.getUsername(), result.getUsername());
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void createUser_UsernameExists_ThrowsException() {
        // Given
        when(userRepository.existsByUsername(userDTO.getUsername())).thenReturn(true);
        
        // When & Then
        assertThrows(BusinessException.class, () -> userService.createUser(userDTO));
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void getUserById_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);
        
        // When
        UserDTO result = userService.getUserById(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository).findById(1L);
    }
    
    @Test
    void getUserById_NotFound_ThrowsException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(1L));
    }
}
```

### Section: Integration Testing
**Description**: Integration testing with Spring Boot Test and TestContainers.

**Sample Implementation**:
```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UserControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }
    
    @Test
    void createUser_Success() throws Exception {
        // Given
        UserDTO userDTO = UserDTO.builder()
            .username("testuser")
            .email("test@example.com")
            .password("password123")
            .firstName("Test")
            .lastName("User")
            .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.username").value("testuser"))
            .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }
    
    @Test
    void getUserById_Success() throws Exception {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setStatus(UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);
        
        // When & Then
        mockMvc.perform(get("/api/v1/users/{id}", savedUser.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(savedUser.getId()))
            .andExpect(jsonPath("$.data.username").value("testuser"));
    }
}
```

---

## 12. Database Migration

### Section: Flyway Migration Scripts
**Description**: Database version control using Flyway migration scripts.

**Sample Implementation**:
```sql
-- V1__Create_users_table.sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone_number VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);

-- V2__Create_roles_table.sql
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

INSERT INTO roles (name, description) VALUES 
    ('ROLE_USER', 'Standard user role'),
    ('ROLE_ADMIN', 'Administrator role'),
    ('ROLE_MODERATOR', 'Moderator role');

-- V3__Create_products_table.sql
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    parent_id BIGINT,
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    sku VARCHAR(50) UNIQUE NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    category_id BIGINT NOT NULL,
    image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
);

CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_status ON products(status);

-- V4__Create_orders_table.sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    shipping_address TEXT NOT NULL,
    billing_address TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
);

CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_order_items_order ON order_items(order_id);
```

---

## Conclusion

This comprehensive low-level technical design document provides detailed specifications for implementing a Spring Boot application following industry best practices. The document covers all essential aspects including:

- Layered architecture with clear separation of concerns
- Domain model design with JPA entities and relationships
- Repository layer with Spring Data JPA
- Service layer with business logic and transaction management
- Controller layer with RESTful API endpoints
- Security configuration with JWT authentication
- Exception handling and error responses
- Configuration management with profiles
- Integration with external services and message queues
- Comprehensive testing strategy
- Database migration with Flyway

All code samples are production-ready and follow Spring Boot conventions, ensuring maintainability, scalability, and adherence to industry standards.