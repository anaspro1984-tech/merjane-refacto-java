# Code Review Feedback
## Controllers
- Controllers should remain lightweight and free of business logic.
- Order processing logic must be extracted into a dedicated OrderService class. 
- The System.out.println(order) call exposes more data than necessary and is not compliant with OWASP logging best practices, as it risks leaking sensitive information. Logging should be limited to the minimum data required for tracking. Additionally, lines 40 and 41 are dead code: orderId is appended to a list but never consumed and should be removed. An OrderService will be injected via @Autowired.
## Product Type Handling
Rather than checking p.getType() through conditional branches, implement a Strategy pattern with three concrete classes: NormalProduct, SeasonalProduct, and ExpirableProduct. Each will expose an isAvailable() method with behavior specific to its product type.
## Repositories
OrderRepository and ProductRepository should be abstracted behind two interfaces, IOrderRepository and IProductRepository, to decouple the data access layer from its implementation.
## Notification Service
Introduce an INotificationService interface so that NotificationService can be swapped out for alternative implementations in the future, such as a message broker.
## ProductService
Product expiration logic and other checks are currently duplicated between ProductService and the controller. Adopting the Strategy pattern per product type will consolidate this logic and eliminate the duplication.