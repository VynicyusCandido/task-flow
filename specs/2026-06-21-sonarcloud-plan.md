# SonarCloud Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Integrate SonarCloud static analysis into both GitHub Actions CI pipelines, with a Quality Gate that blocks PRs if code quality or test coverage drops on new code.

**Architecture:** Two separate SonarCloud projects (`taskflow-api` and `taskflow-app`), each analyzed by their respective CI workflow. Backend uses JaCoCo to generate XML coverage consumed by the Sonar Maven plugin. Frontend uses Jest `--coverage` to generate lcov consumed by the SonarCloud GitHub Action.

**Tech Stack:** SonarCloud (sonarcloud.io), JaCoCo 0.8.12, sonar-maven-plugin (via `sonar:sonar` goal), SonarSource/sonarcloud-github-action@master, Jest lcov coverage reporter.

## Global Constraints

- Java 21, Spring Boot 3.4.3, Maven wrapper (`./mvnw`) — never use `mvn` directly
- Node 20, npm — use `npm ci` not `npm install`
- SonarCloud organization name: `VynicyusCandido` (exact case used in SonarCloud dashboard)
- Project keys: `VynicyusCandido_taskflow-api` and `VynicyusCandido_taskflow-app` (SonarCloud auto-generates keys as `orgName_repoName` — confirm in dashboard after setup)
- `SONAR_TOKEN` must already exist as a GitHub Actions secret before tasks 2 and 4 are pushed
- Commit style: Conventional Commits one-line — `type(scope): mensagem curta` — no body

## Prerequisites (manual, one-time — do before any task)

These steps happen in the browser, not in code:

1. Go to [sonarcloud.io](https://sonarcloud.io) → Log in with GitHub
2. Click **+** → **Analyze new project** → select `VynicyusCandido/task-flow`
3. SonarCloud will create two projects automatically if you import the monorepo, or create them manually:
   - Project 1: display name `taskflow-api`, key `VynicyusCandido_taskflow-api`
   - Project 2: display name `taskflow-app`, key `VynicyusCandido_taskflow-app`
4. For each project: **Administration → Analysis Method → set to "GitHub Actions"** (disables automatic analysis)
5. Go to **My Account → Security → Generate Token** → name it `taskflow-ci` → copy value
6. In GitHub: **Settings → Secrets and variables → Actions → New repository secret**
   - Name: `SONAR_TOKEN` — Value: token copied above
7. In SonarCloud: **Organization → Quality Gates → Create** a custom gate named `taskflow` with these conditions:
   - Coverage on New Code < 70% → Fail
   - Duplicated Lines on New Code > 3% → Fail
   - Maintainability Rating on New Code worse than A → Fail
   - Reliability Rating on New Code worse than A → Fail
   - Security Rating on New Code worse than A → Fail
8. Assign this Quality Gate to both projects: each project → **Administration → Quality Gate → taskflow**

---

## Task 1: Backend — JaCoCo + Sonar Maven properties

**Files:**
- Modify: `taskflow-api/pom.xml` (lines 18-24 properties block, lines 112-165 build/plugins block)

**Interfaces:**
- Produces: `target/site/jacoco/jacoco.xml` — consumed by Task 2's `sonar:sonar` goal

- [ ] **Step 1: Add Sonar properties to `<properties>` block**

In `taskflow-api/pom.xml`, inside the existing `<properties>` tag (after line 23 `<project.reporting.outputEncoding>`), add:

```xml
<sonar.organization>VynicyusCandido</sonar.organization>
<sonar.host.url>https://sonarcloud.io</sonar.host.url>
<sonar.coverage.jacoco.xmlReportPaths>
    target/site/jacoco/jacoco.xml
</sonar.coverage.jacoco.xmlReportPaths>
```

- [ ] **Step 2: Add JaCoCo plugin inside `<plugins>` block**

In `taskflow-api/pom.xml`, inside the existing `<plugins>` tag (after the closing `</plugin>` of `maven-resources-plugin`, before `</plugins>`), add:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

- [ ] **Step 3: Run tests and verify JaCoCo report is generated**

```bash
cd taskflow-api
./mvnw test
```

Expected output: `BUILD SUCCESS` and file `target/site/jacoco/jacoco.xml` exists:

```bash
ls target/site/jacoco/jacoco.xml
```

Expected: `target/site/jacoco/jacoco.xml` (no "No such file" error)

- [ ] **Step 4: Commit**

```bash
git add taskflow-api/pom.xml
git commit -m "chore(sonar): add JaCoCo plugin and SonarCloud properties to pom.xml"
```

---

## Task 2: Backend CI — SonarCloud analysis step

**Files:**
- Modify: `.github/workflows/api-ci.yml`

**Interfaces:**
- Consumes: `target/site/jacoco/jacoco.xml` from Task 1
- Consumes: `SONAR_TOKEN` GitHub secret (must exist — see Prerequisites)
- Produces: SonarCloud analysis at `sonarcloud.io/project/overview?id=VynicyusCandido_taskflow-api`

- [ ] **Step 1: Replace the `Run Unit Tests` step to also generate the JaCoCo report**

In `.github/workflows/api-ci.yml`, find:
```yaml
      - name: Run Unit Tests
        run: ./mvnw test
```

Replace with:
```yaml
      - name: Run Unit Tests
        run: ./mvnw verify
```

> `verify` runs `test` + all post-test plugins including JaCoCo's `report` goal. This guarantees `jacoco.xml` exists before Sonar runs.

- [ ] **Step 2: Add SonarCloud analysis step after Publish Test Report**

In `.github/workflows/api-ci.yml`, after the `Publish Test Report` step, add:

```yaml
      - name: Analyze with SonarCloud
        if: github.event_name != 'pull_request' || github.event.pull_request.head.repo.full_name == github.repository
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        run: ./mvnw sonar:sonar -Dsonar.projectKey=VynicyusCandido_taskflow-api
```

> The `if` condition skips Sonar on PRs from forks (those don't have access to secrets), preventing CI failures on external contributions.

- [ ] **Step 3: Verify the full workflow file is valid YAML**

```bash
cat .github/workflows/api-ci.yml
```

Confirm indentation is consistent (2 spaces) and no tab characters.

- [ ] **Step 4: Commit and push**

```bash
git add .github/workflows/api-ci.yml
git commit -m "ci(sonar): add SonarCloud analysis step to backend CI"
git push origin main
```

- [ ] **Step 5: Verify in GitHub Actions**

Go to the repository on GitHub → **Actions** tab → find the latest `TaskFlow API CI` run → confirm the `Analyze with SonarCloud` step completes without error.

Expected: green checkmark on the step and a new analysis visible at `sonarcloud.io`.

---

## Task 3: Frontend — Jest coverage config

**Files:**
- Modify: `taskflow-app/jest.config.ts`
- Modify: `taskflow-app/package.json`

**Interfaces:**
- Produces: `taskflow-app/coverage/lcov.info` — consumed by Task 4's SonarCloud action

- [ ] **Step 1: Add coverage configuration to `jest.config.ts`**

In `taskflow-app/jest.config.ts`, replace the existing `config` object:

```typescript
const config: Config = {
  coverageProvider: 'v8',
  testEnvironment: 'jsdom',
  testPathIgnorePatterns: ['<rootDir>/e2e/'],
  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/src/$1',
  },
  collectCoverageFrom: [
    'src/**/*.{ts,tsx}',
    '!src/**/*.d.ts',
    '!src/app/layout.tsx',
    '!src/app/page.tsx',
  ],
  coverageReporters: ['lcov', 'text'],
};
```

- [ ] **Step 2: Add `test:coverage` script to `package.json`**

In `taskflow-app/package.json`, inside `"scripts"`, add after `"test": "jest"`:

```json
"test:coverage": "jest --coverage",
```

- [ ] **Step 3: Run coverage and verify lcov.info is generated**

```bash
cd taskflow-app
npm run test:coverage
```

Expected: tests pass and file `coverage/lcov.info` is created:

```bash
ls coverage/lcov.info
```

Expected: `coverage/lcov.info` (no error). Also check the text summary printed to console shows coverage percentages.

- [ ] **Step 4: Add coverage directory to `.gitignore`**

In `taskflow-app/.gitignore`, confirm or add:
```
coverage/
```

If not present, add it. Coverage reports are build artifacts and must not be committed.

- [ ] **Step 5: Commit**

```bash
git add taskflow-app/jest.config.ts taskflow-app/package.json taskflow-app/.gitignore
git commit -m "chore(sonar): add Jest lcov coverage config for SonarCloud"
```

---

## Task 4: Frontend — sonar-project.properties + CI update

**Files:**
- Create: `taskflow-app/sonar-project.properties`
- Modify: `.github/workflows/app-ci.yml`

**Interfaces:**
- Consumes: `coverage/lcov.info` from Task 3
- Consumes: `SONAR_TOKEN` GitHub secret
- Produces: SonarCloud analysis at `sonarcloud.io/project/overview?id=VynicyusCandido_taskflow-app`

- [ ] **Step 1: Create `taskflow-app/sonar-project.properties`**

Create the file with this exact content:

```properties
sonar.projectKey=VynicyusCandido_taskflow-app
sonar.organization=VynicyusCandido
sonar.sources=src
sonar.tests=src
sonar.test.inclusions=**/*.test.ts,**/*.test.tsx
sonar.javascript.lcov.reportPaths=coverage/lcov.info
sonar.typescript.tsconfigPath=tsconfig.json
sonar.exclusions=**/*.d.ts,src/app/layout.tsx,src/app/page.tsx
```

- [ ] **Step 2: Replace `Run Unit Tests` step in `app-ci.yml`**

In `.github/workflows/app-ci.yml`, find:
```yaml
      - name: Run Unit Tests
        run: npm run test
```

Replace with:
```yaml
      - name: Run Unit Tests with Coverage
        run: npm run test:coverage
```

- [ ] **Step 3: Add SonarCloud action step after `Build Next.js App`**

In `.github/workflows/app-ci.yml`, after the `Build Next.js App` step, add:

```yaml
      - name: Analyze with SonarCloud
        if: github.event_name != 'pull_request' || github.event.pull_request.head.repo.full_name == github.repository
        uses: SonarSource/sonarcloud-github-action@master
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        with:
          projectBaseDir: ./taskflow-app
```

- [ ] **Step 4: Verify the full workflow file is valid YAML**

```bash
cat .github/workflows/app-ci.yml
```

Confirm indentation is consistent (2 spaces), no tab characters.

- [ ] **Step 5: Commit and push**

```bash
git add taskflow-app/sonar-project.properties .github/workflows/app-ci.yml
git commit -m "ci(sonar): add SonarCloud analysis step to frontend CI"
git push origin main
```

- [ ] **Step 6: Verify in GitHub Actions**

Go to the repository on GitHub → **Actions** tab → find the latest `TaskFlow App CI` run → confirm the `Analyze with SonarCloud` step completes without error.

Expected: green checkmark and new analysis at `sonarcloud.io/project/overview?id=VynicyusCandido_taskflow-app`.

---

## Verification End-to-End

After both tasks 2 and 4 pass in CI:

- [ ] Open a draft PR with any small change
- [ ] Confirm SonarCloud posts a comment on the PR with quality metrics
- [ ] Confirm the `Analyze with SonarCloud` CI check appears on the PR
- [ ] In SonarCloud, confirm the Quality Gate shows **Passed** for both projects
- [ ] Check `sonarcloud.io/organizations/VynicyusCandido/projects` — both `taskflow-api` and `taskflow-app` should appear with green badges
