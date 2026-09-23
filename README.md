# LaunchDarkly Java SDK Demo

A Java Swing app that evaluates LaunchDarkly flags with a user + tenant + application multi-context.

Flags live in project `nteixeira-ld-custom`.

## Prerequisites

- Java 25
- Maven
- Server-side SDK key for `nteixeira-ld-custom` in `.env`

## Setup

Copy `.env.example` to `.env` and set at least `LAUNCHDARKLY_SDK_KEY`. Other values prefill login and supply application metadata:

```
LAUNCHDARKLY_SDK_KEY=your-sdk-key
USER_EMAIL=your-email@example.com
USER_NAME="Your Name"
USER_ROLE=admin
TENANT_ID=1001
APP_VERSION=4.18.0
GIT_SHA=abc1234
DEPLOYMENT_RING=stable
```

The SDK key is read only from `.env`. The app will not start without it or without a streaming connection.

## Evaluation context

Context is built in one place: `LD.buildEvaluationContext()` in [`src/main/java/dev/config/LD.java`](src/main/java/dev/config/LD.java).

| Kind | Key | Attributes | Where it comes from |
|---|---|---|---|
| `user` | email | `name`, `role` | Login dialog — [`Main.showSetupDialog()`](src/main/java/dev/main/Main.java) |
| `tenant` | tenant_id | `name`, `plan`, `region` | Login `tenant_id`, then [`TenantDirectory`](src/main/java/dev/config/TenantDirectory.java) |
| `application` | `java-sdk-demo` | `version`, `gitSha`, `ring` | `.env`: `APP_VERSION`, `GIT_SHA`, `DEPLOYMENT_RING` |

Known tenant IDs: `1001` (Acme / enterprise / us-east-1), `2002` (Globex / starter / eu-west-1), `3003` (Initech / growth / us-west-2).

`APP_VERSION` must be `MAJOR.MINOR.PATCH`.

Flags are evaluated with that multi-context in [`Dashboard`](src/main/java/dev/form/Dashboard.java) (`boolVariation` / flag listeners).

## Feature flags

In `nteixeira-ld-custom`:

- `dashboard` — keystone prerequisite
- `dashboard-progress-meters`
- `dashboard-line-chart`
- `dashboard-bar-chart`

The three UI flags require `dashboard` to be on (true).

## Running

```bash
mvn clean compile exec:java
```

Or package and run:

```bash
mvn clean package
java -jar dist/launchdarkly-java-demo.jar
```

## Project structure

```
src/main/java/dev/
├── config/    LD.java (SDK + context), TenantDirectory.java
├── main/      Login dialog and window
├── form/      Dashboard and other screens
├── chart/     Chart components
├── component/ Header and menu
├── event/     Menu events
└── swing/     Custom Swing widgets
```

## License

MIT — see [LICENSE](LICENSE).
