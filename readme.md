# Feature Manager Project

This project implements a Feature Management system in Spring Boot that enables dynamic control of application features. Features can be toggled on or off, including user targeting groups, roles, or custom conditions. The solution supports multiple backends through decoupled repositories, such as Azure App Configuration (AzureAppConfigFeatureRepository) and a YAML-based model. It also allows defining custom filters for advanced targeting scenarios, providing a scalable and extensible approach to feature flag management.

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

### Prerequisites

- Java 17+
- Maven 3+
- Spring Boot 3+
- Azure Subscription with App Configuration enabled

### Installation

Clone the repository:

```bash
git clone https://github.com/danieldreuben/FeatureManagement/feature-manager.git

