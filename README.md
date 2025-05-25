# 🔄 Spring Transactional Either

A Spring Boot library that extends transaction management to work seamlessly with functional programming patterns using Vavr's Either type, enabling intelligent rollback decisions based on Either left values.

## 🎯 Overview

This library bridges the gap between Spring's declarative transaction management and functional programming with Either types. It provides custom transaction interceptors and attribute parsers that understand Either return types and can handle rollback scenarios based on Either's left values, promoting clean error handling without exceptions.

## ✨ Features

- **🔄 Either-Aware Transactions**: Automatically handles transaction rollback based on Either left values
- **🎯 Custom Rollback Rules**: Define rollback behavior for specific error types in Either left values
- **🚀 Spring Boot Integration**: Seamless integration with Spring Boot's auto-configuration
- **🛡️ Functional Error Handling**: Full support for Vavr's Either type in transactional methods
- **⚡ Zero Configuration**: Works out-of-the-box with existing Spring applications
- **🏗️ Hexagonal Architecture**: Perfect for clean architecture and domain-driven design
- **🔧 Extensible**: Custom transaction attributes and rollback rules support

## 📦 Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.emedina.transactional</groupId>
    <artifactId>transactional-either-spring-boot</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 🚀 Quick Start

### 1️⃣ Basic Usage

```java
import io.vavr.control.Either;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    
    @Transactional
    public Either<String, User> createUser(UserRequest request) {
        // Transaction will automatically rollback if Either.left() is returned
        if (request.getEmail() == null) {
            return Either.left("Email is required");
        }
        
        User user = new User(request.getUsername(), request.getEmail());
        userRepository.save(user);
        return Either.right(user);
    }
}
```

### 2️⃣ Custom Rollback Rules

```java
@Service
public class OrderService {
    
    @Transactional(rollbackForWithEither = {ValidationException.class})
    public Either<OrderError, Order> processOrder(OrderRequest request) {
        return validateOrder(request)
            .flatMap(this::calculateTotal)
            .flatMap(this::saveOrder)
            .mapLeft(error -> new OrderError("Order processing failed", error));
    }
}
```

### 3️⃣ Configuration (Optional)

```java
import com.emedina.transactional.config.TransactionManagerConfigWithEither;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TransactionManagerConfigWithEither.class)
public class TransactionConfiguration {
    // Additional transaction configuration if needed
}
```

## 🏗️ Architecture

The library consists of several key components:

### 🔄 TransactionInterceptorWithEither

The core interceptor that extends Spring's transaction support to handle Either return types and make intelligent rollback decisions.

### 🎯 TransactionAspectSupportWithEither

Provides the foundational aspect support with Either-aware transaction management logic, including the VavrDelegate for Either evaluation.

### 📋 RuleBasedTransactionAttributeWithEither

Extended transaction attribute class that supports custom rollback rules for Either error values, allowing fine-grained control over transaction behavior.

### 🔧 SpringTransactionAnnotationParserWithEither

Custom annotation parser that translates transactional attributes to Spring types with Either support, enabling seamless integration.

## ⚙️ How It Works

1. **🔍 Method Interception**: The `TransactionInterceptorWithEither` intercepts methods annotated with `@Transactional`
2. **🎯 Either Detection**: Checks if the return type is an Either and evaluates the result
3. **🛡️ Rollback Decision**: If Either.left() contains an error matching rollback rules, triggers transaction rollback
4. **🔄 Transaction Completion**: Commits successful transactions or rolls back based on Either evaluation
5. **📊 Error Propagation**: Preserves Either semantics while managing transaction lifecycle

## 🔄 Either Type Benefits

The Either type provides several advantages for transaction management:

- **🚫 No Exceptions**: Avoid exception-based error handling in business logic
- **🔗 Composable**: Chain transactional operations functionally
- **🎯 Explicit**: Make error cases explicit in the type system
- **🛡️ Safe**: Compile-time safety for error handling
- **📊 Informative**: Rich error information without stack traces

### Example with Functional Composition

```java
@Service
public class PaymentService {
    
    @Transactional
    public Either<PaymentError, PaymentResult> processPayment(PaymentRequest request) {
        return validatePayment(request)
            .flatMap(this::checkBalance)
            .flatMap(this::chargeCard)
            .flatMap(this::recordTransaction)
            .map(this::generateReceipt);
    }
    
    private Either<PaymentError, PaymentRequest> validatePayment(PaymentRequest request) {
        // Validation logic returning Either
    }
}
```

## 🧪 Testing

The library includes comprehensive unit tests with high coverage. Run tests with:

```bash
mvn test
```

### 📊 Test Coverage

- ✅ **Unit Tests**: All components tested with JUnit 5 and Mockito
- ✅ **Integration Scenarios**: Transaction behavior validation
- ✅ **Edge Cases**: Error conditions and rollback scenarios covered
- ✅ **90%+ Coverage**: Comprehensive test suite with JaCoCo

### 🔧 JaCoCo Coverage

Generate coverage reports:

```bash
mvn clean test jacoco:report
```

View the coverage report at `target/site/jacoco/index.html`

## 📋 Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| **Spring Boot** | 3.5.0 | Core Spring Boot integration |
| **Java** | 24+ | Runtime platform |
| **Vavr** | 0.10.6 | Functional programming with Either |
| **Shared Kernel Transactional** | 1.0.0 | Transaction interfaces |

### Test Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| **JUnit Jupiter** | 5.11.3 | Testing framework |
| **Mockito** | 5.14.2 | Mocking framework |
| **AssertJ** | 3.26.3 | Fluent assertions |
| **SLF4J** | 2.0.16 | Logging framework |

## 🔧 Build Requirements

- **Java 24+**
- **Maven 3.9+**
- **Spring Boot 3.5.0+**

### Building

```bash
# Compile the project
mvn clean compile

# Run tests
mvn test

# Package the library
mvn clean package

# Generate documentation
mvn javadoc:javadoc
```

## 🤝 Contributing

1. 🍴 Fork the repository
2. 🌿 Create a feature branch (`git checkout -b feature/amazing-feature`)
3. ✅ Add tests for your changes
4. 🧪 Ensure all tests pass (`mvn test`)
5. 📊 Maintain 50%+ test coverage
6. 📝 Update documentation as needed
7. 📤 Submit a pull request

### Development Guidelines

- Follow hexagonal architecture principles
- Maintain functional programming patterns
- Add comprehensive tests for new features
- Update documentation for API changes

## 📄 License

This project is part of the hexagonal architecture examples and follows the same licensing terms.

## 👨‍💻 Author

**Enrique Medina Montenegro**

---

## 🏷️ Tags

`spring-boot` `transactions` `either` `functional-programming` `vavr` `hexagonal-architecture` `ddd` `spring-framework` `aop` `error-handling` `monads` `clean-architecture`

---

*🎯 This library enables clean, functional transaction management in Spring Boot applications using Either types for explicit error handling without exceptions.*
