# Feature Manager Project

This project provides a **Feature Management system** built on **Spring Boot** that allows dynamic enabling/disabling of application features based on various filters such as user targeting, groups, roles, or custom conditions. This feature management module can integrate with **Azure App Configuration** and feature YAML model via decoupled repository (AzzureAppConfigFeatureRepository.java).  Allowing feature flag storage and supports custom filters for advanced targeting.

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

