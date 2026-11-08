# Trails SDK repository map

```text
core/trails-sdk/
|-- pom.xml                                        SDK build, API generation, tests, and publication metadata
|-- DEVELOPMENT.md                                 Standalone SDK build and verification commands
|-- docs/
|   |-- INVARIANTS.md                            Cross-cutting correctness guarantees and verification owners
|   `-- adr/                                       Binding SDK architecture decisions
|-- samples/                                       Consumer composition samples
|-- src/
|   |-- main/
|   |   |-- java/io/github/yaforster/trails/
|   |   |   |-- core/                              Closed domain model, values, actions, and results
|   |   |   |   |-- notification/                  Framework-free recipient value contract
|   |   |   |   |-- test/                          Validated bounded graphs and immutable execution snapshots
|   |   |   |   |   `-- TestPathRunner.java         Browser-path lifecycle and cleanup
|   |   |   |   `-- test/action/{browser,document,download,element,value}/ Closed action catalogue
|   |   |   |-- app/
|   |   |   |   `-- services/                      Framework-free application ports and use-case contracts
|   |   |   |-- sdk/
|   |   |   |   |-- autoconfigure/                 Host auto-configuration
|   |   |   |   |-- composition/                   Atomic adapter registrations and fail-fast validation
|   |   |   |   |-- host/{capability,execution,print,testdata,time}/ Host services
|   |   |   |   |-- notification/                  Terminal notification contracts and dispatcher
|   |   |   |   `-- security/                      Identity, authorization, and exact-path transport credential registrations
|   |   |   |-- adapter/api/
|   |   |   |   |-- asyncapi/                      SDK-owned execution-event transport
|   |   |   |   `-- rest/                         REST resources, mappers, validation, links, and PDF/artifacts
|   |   |   |       `-- common/PaginationLinks.java  Shared page navigation with resource-owned URI construction
|   |   |   `-- ...                               Other SDK-owned supporting packages
|   |   `-- resources/
|   |       |-- api/                              OpenAPI and AsyncAPI source contracts
|   |       `-- META-INF/spring/                   Boot auto-configuration import
|   `-- test/java/io/github/yaforster/trails/      Framework-free, composition, and Boot transport tests
|-- README.md                                      Consumer and operator guidance
`-- REPOMAP.md                                     This map
```

The SDK owns the closed domain/application contracts, Spring Boot composition boundary, execution host, REST/SSE
transport, security, capability aggregation, and notifier dispatch. Maven generates REST DTOs from
`src/main/resources/api/hateoas.yaml`; generated output must not be edited.

The SDK owns no JPA/MySQL, Selenium, SMTP, or PDF-rendering implementation. `PrintService` is implemented by the
customer-selected printing adapter and supplied through `TrailsPrintingRegistration`. The composition validator owns
provider cardinality; print context assembly and HTTP transport consume the validated composition.
