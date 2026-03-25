# UgandaEMR Reporting Backend Module

Backend module for the UgandaEMR Reporting project.  
This module provides the server-side APIs, domain models, persistence, preview execution, and report-building services used to define, manage, and run reporting components in UgandaEMR.

## Overview

The reporting backend is responsible for:

- managing reporting metadata and configuration
- exposing REST resources for reporting entities
- storing and retrieving report definitions
- previewing indicator and section SQL
- executing parameterized SQL safely through service-layer methods
- supporting frontend consumers with structured JSON responses

This module is designed to work within the OpenMRS module ecosystem and follows common OpenMRS backend patterns such as:

- service-layer business logic
- Hibernate-backed domain models
- REST resources based on `DelegatingCrudResource`
- action-oriented endpoints for non-CRUD operations like preview

## Main Responsibilities

The backend module currently supports work around:

- **Report Libraries**
- **Report Builder Sections**
- **Indicators**
- **ETL Sources**
- **SQL Preview**
- **Section Preview**
- metadata persistence and retrieval
- backend validation and resource lifecycle operations

## Module Structure

A typical package layout looks like this:

```text
org.openmrs.module.reportbuilder
├── api
│   ├── ReportBuilderService.java
│   └── impl
├── dao
├── dto
├── model
├── web
│   ├── controller
│   ├── resource
│   └── ...
```

### Key package roles

#### `api`
Contains service interfaces and implementations for business logic.

Examples:
- saving and retrieving reporting entities
- retiring and purging resources
- previewing SQL
- searching and paging reporting data

#### `dao`
Contains persistence logic for fetching and storing reporting entities.

#### `model`
Contains domain entities such as:

- `ReportBuilderSection`
- `ETLSource`
- report library entities
- indicator-related entities

#### `dto`
Contains transport and helper objects used for request/response operations.

Examples:
- `SqlPreviewResult`
- request payload DTOs for preview endpoints

#### `web.resource`
Contains REST resources for CRUD-style endpoints.

Examples:
- `ReportBuilderSectionResource`
- `ETLSourceResource`
- `ReportLibraryResource`

#### `web.controller`
Contains action-based endpoints that do not fit normal CRUD semantics.

Examples:
- preview endpoints
- compile/validate endpoints
- execution actions

## Backend Design Principles

### 1. CRUD resources for persisted entities
Resources extending `DelegatingCrudResource<T>` should be used for entities that are actually stored and managed.

Examples:
- sections
- ETL sources
- report libraries

### 2. Controllers for actions
Endpoints such as preview, validate, execute, or compile are better modeled as standalone action endpoints instead of fake CRUD subresources.

Examples:
- previewing SQL for an indicator
- previewing all indicators in a section

### 3. Service-layer ownership
Business logic should remain in the service layer as much as possible.

Resources and controllers should mainly:
- validate request input
- load domain objects
- call the service
- shape the response

### 4. Consistent lifecycle handling
Entities should consistently use either:
- `retired` / `retireReason` for `Retireable`
- `voided` for `Voidable`

Avoid mixing both semantics in the same resource unless the model truly supports that.

## Key REST Endpoints

### Report Builder Section CRUD

Base path:

```text
/ws/rest/v1/reportbuilder/section
```

Typical operations:
- create section
- get section by UUID
- list sections
- search sections
- update section
- retire section
- purge section

### Section Preview

Standalone preview endpoint:

```text
POST /ws/rest/v1/reportbuilder/sectionpreview
```

This endpoint previews SQL for:
- all indicators inside a section, or
- one indicator inside a section if `indicatorUuid` is provided

#### Sample payload

```json
{
  "sectionUuid": "4278ebf9-47ab-40b8-aeca-df3e4a050c79",
  "startDate": "2026-01-01",
  "endDate": "2026-01-31",
  "maxRows": 100,
  "params": {}
}
```

#### Preview a single indicator in a section

```json
{
  "sectionUuid": "4278ebf9-47ab-40b8-aeca-df3e4a050c79",
  "indicatorUuid": "2b9c6c8e-1234-4d9f-8abc-1234567890ab",
  "startDate": "2026-01-01",
  "endDate": "2026-01-31",
  "maxRows": 100,
  "params": {}
}
```

### ETL Source CRUD

Base path:

```text
/ws/rest/v1/reportbuilder/etlsource
```

Typical operations:
- create ETL source
- get ETL source by UUID
- list ETL sources
- search ETL sources
- retire ETL source
- purge ETL source

## Request and Response Patterns

### Common request fields for preview endpoints

- `startDate` — required
- `endDate` — required
- `maxRows` — optional
- `params` — optional map of extra SQL parameters
- `indicatorUuid` — optional, when previewing one indicator in a section
- `sectionUuid` — required for standalone section preview resource

### Example response shape

```json
{
  "sectionUuid": "4278ebf9-47ab-40b8-aeca-df3e4a050c79",
  "results": [
    {
      "indicatorUuid": "2b9c6c8e-1234-4d9f-8abc-1234567890ab",
      "kind": "indicator",
      "name": "Total Clients",
      "code": "TC001",
      "columns": ["total"],
      "rows": [
        [245]
      ],
      "rowCount": 1,
      "truncated": false,
      "error": null
    }
  ]
}
```

## Development Notes

### Standalone preview resource vs subresource
For preview operations, prefer a standalone resource or controller endpoint instead of using `DelegatingSubResource` when there is no persisted child object.

Why:
- preview is an action, not a child entity
- it avoids routing ambiguity
- it keeps endpoint intent clearer

### Paging
Where listing is supported, resources should implement:

- `doGetAll(RequestContext context)`
- `doSearch(RequestContext context)`

and return:
- `NeedsPaging<T>` for paged service responses
- `AlreadyPaged<T>` where appropriate

### Resource representations
Resources should define:
- `DefaultRepresentation`
- `FullRepresentation`
- `getCreatableProperties()`
- `getUpdatableProperties()` where needed

### Error handling
Use:
- `ObjectNotFoundException` when a referenced entity does not exist
- `ResourceDoesNotSupportOperationException` for unsupported CRUD operations
- validation errors for missing required request fields

## Example Backend Workflow

### Previewing a section
1. client sends POST request to `/ws/rest/v1/reportbuilder/sectionpreview`
2. backend validates `sectionUuid`, `startDate`, and `endDate`
3. backend loads `ReportBuilderSection`
4. backend reads `configJson`
5. backend extracts compiled SQL for one or more indicators
6. backend executes preview through `ReportBuilderService`
7. backend returns preview result as JSON

## Build and Run

This module is intended to run inside an OpenMRS-based environment.

Typical development flow:

```bash
mvn clean install
```

Then deploy the built module artifact into your OpenMRS modules directory or run it through your standard local development setup.

for module integration tests in OpenMRS.

## License

Mozilla Public License 2.0
