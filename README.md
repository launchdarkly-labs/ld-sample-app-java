# LaunchDarkly Java SDK Demo

A Java Swing app that evaluates LaunchDarkly flags with a user + tenant + application multi-context.

## Prerequisites

- Java 25
- Maven
- A [server-side SDK key](https://app.launchdarkly.com/settings/sdk-keys) in `.env` as `LAUNCHDARKLY_SDK_KEY`

## Setup

Copy `.env.example` to `.env` and set at least `LAUNCHDARKLY_SDK_KEY`. Get that key from [SDK keys](https://app.launchdarkly.com/settings/sdk-keys). Other values prefill login and supply application metadata:

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

Create the demo flags with a LaunchDarkly **API access token** from [Authorization tokens](https://app.launchdarkly.com/settings/authorization/tokens) (not the SDK key):

```bash
java scripts/ProvisionFlags.java --api-key api-xxxxxxxx
```

That script is standalone JDK-only and is not used by the Swing app. It creates `dashboard` (prerequisite) plus `dashboard-progress-meters`, `dashboard-line-chart`, and `dashboard-bar-chart`.

## Evaluation context

LaunchDarkly does not evaluate flags for “the app” in general. Every evaluation needs a **context**: who (or what) is asking. This demo sends **three contexts at once** (a multi-context) so you can target by user, by tenant, or by the running application.

The three pieces are assembled in `LD.buildEvaluationContext()`:

[`src/main/java/dev/config/LD.java`](src/main/java/dev/config/LD.java#L117-L151)

| Kind | Context key | Attributes LaunchDarkly can target | Where it comes from |
|---|---|---|---|
| `user` | email | built-in `name`, custom `role` | Login — [`Main.showSetupDialog()`](src/main/java/dev/main/Main.java#L26-L131) |
| `tenant` | tenant id (`1001`, `2002`, or `3003`) | built-in `name`, custom `plan`, `region` | Login tenant id, then [`TenantDirectory`](src/main/java/dev/config/TenantDirectory.java#L14-L18) |
| `application` | hardcoded `java-sdk-demo` | custom `version`, `gitSha`, `ring` | `.env`: `APP_VERSION`, `GIT_SHA`, `DEPLOYMENT_RING` (loaded in [`LD.loadEnvFile()`](src/main/java/dev/config/LD.java#L40-L63)) |

Known tenants: `1001` (Acme / enterprise / us-east-1), `2002` (Globex / starter / eu-west-1), `3003` (Initech / growth / us-west-2).

`APP_VERSION` must be `MAJOR.MINOR.PATCH`.

The dashboard evaluates flags with that same multi-context in [`Dashboard.init()`](src/main/java/dev/form/Dashboard.java#L91-L93) and listens for live changes in [`initializeFlagTracking()`](src/main/java/dev/form/Dashboard.java#L265-L298).

### How the context is built (step by step)

Think of login as a stand-in for a verified JWT. The dialog is not talking to LaunchDarkly yet. It only collects identity. Then the app looks up extra facts and packages everything LaunchDarkly needs.

1. **Process starts.** `LD` loads `.env` in [`loadEnvFile()`](src/main/java/dev/config/LD.java#L40-L63). That gives you the SDK key, login prefills (`USER_EMAIL`, `USER_NAME`, `USER_ROLE`, `TENANT_ID`), and application metadata (`APP_VERSION`, `GIT_SHA`, `DEPLOYMENT_RING`). If the SDK key is missing, the app warns and stops.

2. **Login.** [`showSetupDialog()`](src/main/java/dev/main/Main.java#L26-L131) shows email, name, role, and tenant id (prefilled from `.env`). You can change them. Tenant id must be `1001`, `2002`, or `3003` — otherwise login is rejected. On OK, it calls [`LD.initialize(...)`](src/main/java/dev/main/Main.java#L121).

3. **Resolve the tenant.** `initialize` does **not** trust the rest of the tenant fields from the form. It takes only `tenant_id` and looks it up in [`TenantDirectory.require()`](src/main/java/dev/config/TenantDirectory.java#L29-L32). That returns name, plan, and region (the way a real app would use a tenant service after Spring Security has already validated `tenant_id` on the JWT).

4. **Build three contexts.** [`buildEvaluationContext()`](src/main/java/dev/config/LD.java#L117-L151) creates:
   - **user** — key = email, display name, `role`
   - **tenant** — key = tenant id, name, `plan`, `region`
   - **application** — key = `java-sdk-demo`, `version` / `gitSha` / `ring` from `.env`

5. **Combine them.** `LDContext.multiBuilder()` stacks those three into one evaluation context. LaunchDarkly rules can then say things like “role is admin” **and** “tenant plan is enterprise” **and** “app version is 4.18.0” in a single evaluation.

6. **Connect and evaluate.** The SDK streams flag config, then [`Dashboard`](src/main/java/dev/form/Dashboard.java#L91-L93) calls `boolVariation` with that multi-context. Listeners reuse the same context so toggles in LaunchDarkly update the UI live.

If a targeting rule does not match, it is usually because the rule’s **context kind** is wrong (for example targeting `user.region` when `region` lives on **tenant**), or `dashboard` is still off so the UI flags stay false.

## Feature flags

The app evaluates:

- `dashboard` — keystone prerequisite
- `dashboard-progress-meters`
- `dashboard-line-chart`
- `dashboard-bar-chart`

The three UI flags require `dashboard` to be on (true). Creating them with an API access token is covered under [Setup](#setup).

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
