# Order Management Service - Test Structure

This document explains the test structure and organization for the Order Management Service.

## Refactoring Goals

The test suite has been refactored to achieve the following goals:

1. **Reduce Duplication**: Common setup, utility methods, and constants have been centralized to avoid duplication
2. **Improve Readability**: Tests are now more focused on their actual purpose rather than setup details
3. **Consistent Structure**: Tests follow a consistent pattern and organization across different layers
4. **Maintainability**: Changes to common patterns only need to be made in one place

## Test Structure

### Core Components

- **TestContainersConfig**: Base class for integration tests using TestContainers
- **BaseRepositoryTest**: Base class for repository integration tests
- **BaseControllerTest**: Base class for controller integration tests
- **TestConstants**: Centralized constants used across tests
- **TestEntityFactory**: Factory methods for creating test entities

### Setup Separation

Tests now cleanly separate the following concerns:

1. **Test Container Setup**: Defined once in TestContainersConfig
2. **Database Integration**: Configured once in BaseRepositoryTest
3. **Mock MVC Setup**: Configured once in BaseControllerTest
4. **Test Data Creation**: Centralized in TestEntityFactory
5. **Common Constants**: Centralized in TestConstants

## Naming Conventions

Tests follow the naming convention: `[methodName]_[condition]_[expectedResult]`

Examples:
- `findByEmail_WhenCustomerExists_ShouldReturnCustomer()`
- `createOrder_WithInvalidData_ShouldThrowException()`

## Benefits

This refactoring provides several benefits:

1. **Reduced code duplication**: Common test setup is now defined in one place
2. **Faster test development**: New tests can reuse existing utilities and patterns
3. **Consistent test style**: All tests follow the same pattern and organization
4. **Easier maintenance**: Changes to common patterns only need to be made in one place
5. **Better readability**: Tests focus on actual test logic rather than setup details

## Arrange-Act-Assert Pattern

All tests should follow the Arrange-Act-Assert pattern:

```java
// Arrange
Customer customer = createPersistedCustomer(customerRepository);

// Act
Optional<Customer> result = customerRepository.findByEmail(TEST_EMAIL);

// Assert
assertTrue(result.isPresent());
assertEquals(CUSTOMER_NAME, result.get().getName());
```

## Testing Different Layers

### Repository Tests

Repository tests extend `BaseRepositoryTest` which provides:
- TestContainers setup for PostgreSQL
- Common test data cleanup
- Repository autowiring
- Transactional test context

### Controller Tests

Controller tests extend `BaseControllerTest` which provides:
- Mock MVC configuration
- Utility methods for performing requests and assertions
- TestContainers setup for integration tests

## Integration With Spring Test

The refactoring maintains full compatibility with Spring's testing framework, including:
- Profiles for different test types
- Database integration with TestContainers
- Transaction management for tests
- Mock MVC for controller testing 