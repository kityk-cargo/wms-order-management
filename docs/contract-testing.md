# Contract Testing in WMS Order Management

This document describes how to run and maintain the contract verification tests for the WMS Order Management service.

## Overview

Contract tests verify that our service implementation fulfills the promises made to consumers in the Pact contracts. The WMS Order Management service acts as a provider for these contracts, and we verify that our implementation matches what consumers expect.

## Running Contract Tests

### Standard Verification

To run the contract tests using the default settings:

```bash
./gradlew pactVerify
```

This will:
1. Start a test instance of the service
2. Load contracts from the default location (`../wms-contracts/pact/rest/wms_order_management`)
3. Verify each interaction defined in the contracts
4. Generate detailed test reports

### Custom Contract Location

To verify against contracts in a custom location:

```bash
./gradlew pactVerify -Ppact.folder.path=/path/to/contracts
```

## Contract Structure

Contracts are stored in the `wms-contracts` repository, with the structure:

```
wms-contracts/
└── pact/
    └── rest/
        └── wms_order_management/
            └── wms_ui.json
```

Each JSON file contains interactions that define:
- The provider state (preconditions)
- The expected request
- The expected response

## Provider States

Provider states set up the test environment to match the preconditions assumed in the contract. They're defined in `OrderManagementContractVerificationTest.java` using the `@State` annotation.

Common provider states include:
- `orders exist` - Test data for orders is available
- `order with ID 1 exists` - A specific order exists
- `server is experiencing issues` - Error handling tests

## Best Practices

1. **State Management**: Ensure your `@State` handlers correctly set up the test environment
2. **Diagnostics**: Use the detailed logs to troubleshoot verification failures
3. **Regular Verification**: Run tests with every meaningful change to the API
4. **Contract updates**: When the API changes, update contracts and sync with consumers

## Troubleshooting

If verification fails:

1. Check the test logs for specific interaction failures
2. Verify that the contract files are being found (logs will show detected files)
3. Ensure database is properly set up through Liquibase
4. Confirm provider states correctly match the interactions in the contracts

## CI Integration

Contract tests run automatically in CI with every pull request. The workflow:
1. Builds the service
2. Runs contract verification tests
3. Transforms and optionally publishes the Pact results 