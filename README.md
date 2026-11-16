# REST API Test Suite: Rest Assured + Postman/Newman (Demo)

[![Tests](https://github.com/ShipSureQA/api-tests/actions/workflows/api-tests.yml/badge.svg)](https://github.com/ShipSureQA/api-tests/actions/workflows/api-tests.yml) [![CodeQL](https://github.com/ShipSureQA/api-tests/actions/workflows/codeql.yml/badge.svg)](https://github.com/ShipSureQA/api-tests/actions/workflows/codeql.yml) [![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**📊 Live test report (updated on every run):** https://shipsureqa.github.io/api-tests/

> **Demo / self-initiated project** by ShipSure QA. It tests [restful-booker](https://restful-booker.herokuapp.com/apidoc/index.html), a public API built for practising API testing. It is not client work.

The same API is tested two ways, matching what clients most often ask for:

## 1. Rest Assured (Java + TestNG): 26 test runs
- **API client layer** (`BookingClient`): every endpoint in one place, shared `RequestSpecification`, request/response logged only when a check fails
- **POJO models** (Java records + Jackson) for typed request/response bodies
- **Test data factory**: unique random bookings, so tests never depend on shared data
- **JSON Schema validation** of responses
- **Full CRUD lifecycle**: create → get → filter → PUT → PATCH → DELETE → confirm 404
- **Negative & security tests**: missing/invalid token (403), unknown id (404), invalid credentials, incomplete payloads
- **Security tests**: SQL/NoSQL injection, stored XSS and HTML injection payloads must be stored literally with no 5xx; forged tokens cannot delete data
- **Data-driven tests** with TestNG `@DataProvider` (special characters, edge prices)
- **Smoke vs regression suites** (`testng-smoke.xml`, `testng.xml`)

## 2. Postman collection + Newman: 13 requests
- Folders: Health & Auth → Booking lifecycle → Negative & security → Cleanup
- Token and booking id chained between requests via collection variables
- Pre-request script generates fresh test data each run
- Assertions on status codes, body values, response time and JSON schema
- Runs in CI with Newman and an HTML report (htmlextra)

## Findings documented (observed API behaviour)
Comments in the tests note two places where the API differs from common REST conventions. In a client project these would be raised as defects:
- Invalid credentials on `POST /auth` return **200** with a `reason` instead of **401**.
- An incomplete `POST /booking` returns **500** instead of **400**.

## Run
```bash
# Rest Assured (Java 17+, Maven)
mvn test                                  # full regression
mvn test -Dsuite=testng-smoke.xml         # smoke only

# Postman / Newman (Node 20 LTS)
npm install -g newman
newman run postman/restful-booker.postman_collection.json -e postman/restful-booker.postman_environment.json
```
Or import both files from `postman/` into the Postman app and use the Collection Runner.

## CI
GitHub Actions runs both suites on push, on pull requests and daily, and uploads the TestNG and Newman HTML reports as artifacts.

## Quality gates
Every push and pull request runs:
- **Tests:** Rest Assured suite (incl. security tests) + Newman collection
- **CodeQL** static security analysis (weekly + on every change)
- **Dependabot**: weekly dependency and monthly GitHub Actions update PRs
- **Live report** published to GitHub Pages from `main`

## Author
**Nagarasu P · [ShipSure QA](https://shipsureqa.github.io)**. Senior QA automation engineer, available for freelance projects.

## License
[MIT](LICENSE)

