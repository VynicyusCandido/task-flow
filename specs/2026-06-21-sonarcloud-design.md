# SonarCloud Integration Design

## Goal

Integrate SonarCloud static analysis into the existing GitHub Actions CI for both `taskflow-api` (Spring Boot) and `taskflow-app` (Next.js), with a Quality Gate that blocks PRs if code quality or coverage drops on new code.

## Architecture

```
GitHub Actions CI
├── api-ci.yml
│   ├── Build + Test
│   ├── JaCoCo (coverage report)
│   └── sonar:sonar ──────────────┐
│                                  │
└── app-ci.yml                     │
    ├── Build + Test               │
    ├── Jest --coverage            │
    └── sonarcloud-github-action ──┤
                                   ▼
                         SonarCloud (sonarcloud.io)
                         ├── taskflow-api project
                         └── taskflow-app project
                                   │
                                   ▼
                         GitHub PR Decoration
                         (inline comments on PRs)
```

Two separate SonarCloud projects — one per subproject. Each CI workflow sends analysis independently. Quality Gate is configured once in SonarCloud and applies to both.

## Backend (`taskflow-api`)

**`pom.xml` changes:**

Add JaCoCo plugin to generate XML coverage report:
```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.12</version>
  <executions>
    <execution><goals><goal>prepare-agent</goal></goals></execution>
    <execution>
      <id>report</id>
      <phase>test</phase>
      <goals><goal>report</goal></goals>
    </execution>
  </executions>
</plugin>
```

Add Sonar properties to `<properties>`:
```xml
<sonar.organization><!-- GitHub org name --></sonar.organization>
<sonar.host.url>https://sonarcloud.io</sonar.host.url>
<sonar.coverage.jacoco.xmlReportPaths>
  target/site/jacoco/jacoco.xml
</sonar.coverage.jacoco.xmlReportPaths>
```

**`.github/workflows/api-ci.yml` — new step after tests:**
```yaml
- name: Analyze with SonarCloud
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
  run: ./mvnw sonar:sonar -Dsonar.projectKey=taskflow-api
```

## Frontend (`taskflow-app`)

**`package.json` — new script:**
```json
"test:coverage": "jest --coverage --coverageReporters=lcov text"
```

**`jest.config.js` — coverage collection:**
```js
collectCoverageFrom: [
  'src/**/*.{ts,tsx}',
  '!src/**/*.d.ts',
  '!src/app/layout.tsx',
  '!src/app/page.tsx',
]
```

**`taskflow-app/sonar-project.properties` — new file:**
```properties
sonar.projectKey=taskflow-app
sonar.organization=<!-- GitHub org name -->
sonar.sources=src
sonar.tests=src
sonar.test.inclusions=**/*.test.ts,**/*.test.tsx
sonar.javascript.lcov.reportPaths=coverage/lcov.info
sonar.typescript.tsconfigPath=tsconfig.json
```

**`.github/workflows/app-ci.yml` — replace test step and add analysis:**
```yaml
- name: Run tests with coverage
  run: npm run test:coverage

- name: Analyze with SonarCloud
  uses: SonarSource/sonarcloud-github-action@master
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
  with:
    projectBaseDir: ./taskflow-app
```

## Quality Gate

Configured in SonarCloud dashboard (not in code). Evaluates only new/modified code per PR ("on New Code" baseline = Previous Version).

| Metric | Condition | Threshold |
|---|---|---|
| Coverage on New Code | less than | 70% |
| Duplicated Lines on New Code | greater than | 3% |
| Maintainability Rating on New Code | worse than | A |
| Reliability Rating on New Code | worse than | A |
| Security Rating on New Code | worse than | A |

**PR flow:**
```
dev opens PR
    │
    ├── tests pass? ──── no ──→ CI fails
    ├── Quality Gate? ── no ──→ CI fails + PR comment
    └── all green ───────────→ PR can be merged
```

## Secrets

One secret required in GitHub repository settings:

- `SONAR_TOKEN` — generated in SonarCloud (My Account → Security → Generate Token), added via GitHub Settings → Secrets and variables → Actions → New repository secret
- `GITHUB_TOKEN` — provided automatically by GitHub Actions, no setup needed

**Never commit the token value to code or config files.**

## SonarCloud Setup Steps (manual, one-time)

1. Create account at sonarcloud.io using GitHub login
2. Import the `task-flow` repository
3. Create two projects: `taskflow-api` and `taskflow-app`
4. Generate a token and add it as `SONAR_TOKEN` in GitHub repo secrets
5. Configure the custom Quality Gate in SonarCloud dashboard
6. Enable PR decoration in SonarCloud project settings (automatic when GITHUB_TOKEN is present)
