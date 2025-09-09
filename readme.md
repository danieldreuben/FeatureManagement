# Feature Manager Project

Spring boot Feature Management system enables dynamic control of application features. Features can be toggled on or off, including user targeting groups, roles, or custom conditions. The FM supports multiple backends through decoupled repositories, such as Azure App Configuration (AzureAppConfigFeatureRepository) and a YAML-based model. It also allows custom filters (Java Lambdas) for advanced targeting scenarios, providing a scalable and extensible approach to feature flag management.

---

## Table of Contents

- [Features](#features)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Usage](#usage)
- [Filters](#filters)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

---

## Features

- Dynamic feature toggling using Azure App Configuration.
- Targeting filters based on:
  - Users
  - Groups
  - Roles
  - Custom conditions (time-based, environment-based, etc.)
- Centralized feature management with programmatic access.
- Support for multiple micro-frontends via a common `FeatureManager`.
- Easy integration with Spring Boot applications.

---

## Getting Started

mvn spring-boot:run

# Check one feature
curl -H "X-User-Id: bob" \
     -H "X-Roles: admin" \
     -H "X-Permissions: invoice.read" \
     http://localhost:8080/features/RoleAndPermissionFeature

# Check all features
curl -H "X-User-Id: bob" \
     -H "X-Roles: admin" \
     -H "X-Permissions: invoice.read" \
     http://localhost:8080/features


### Prerequisites

- Java 17+
- Maven 3+
- Spring Boot 3+
- Azure Subscription with App Configuration enabled

### Installation

Clone the repository:

```bash
git clone https://github.com/danieldreuben/FeatureManagement/feature-manager.git

