# LaunchDarkly Java SDK Demo

A Java Swing application demonstrating feature flag functionality using the LaunchDarkly Java Server SDK.

Flags live in the LaunchDarkly project `nteixeira-ld-custom`.

## Prerequisites

- Java 25
- Maven
- LaunchDarkly SDK key for `nteixeira-ld-custom`

## Setup

1. Clone the repository
2. Copy `.env.example` to `.env` and add your SDK key plus user and tenant values:
   ```
   LAUNCHDARKLY_SDK_KEY=your-sdk-key
   USER_EMAIL=your-email@example.com
   USER_NAME="Your Name"
   USER_ROLE=admin
   TENANT=acme
   ```

Evaluations use a multi-context: a `user` context (key and `email` attribute from `USER_EMAIL`, plus `role`) and a `tenant` context whose key is `TENANT`.

The SDK key is read only from the project `.env` file. The app does not run without a valid key and a successful SDK connection.

## Building

To build the executable jar:
```bash
mvn clean package
```

This will create `launchdarkly-java-demo.jar` in the `dist` directory.

## Running

There are two ways to run the application:

1. Using Maven:
   ```bash
   mvn clean compile exec:java
   ```

2. Using the jar file:
   ```bash
   java -jar dist/launchdarkly-java-demo.jar
   ```

## Feature Flags

The application uses these flags in `nteixeira-ld-custom`:

- `dashboard` - Keystone prerequisite flag for the dashboard features
- `dashboard-progress-meters` - Controls the visibility of progress meters
- `dashboard-line-chart` - Controls the line chart display
- `dashboard-bar-chart` - Controls the main chart display

`dashboard-progress-meters`, `dashboard-line-chart`, and `dashboard-bar-chart` require `dashboard` to be on (true) before they can serve true.

## Development

The project uses:
- Java 25 and Java Swing for the UI
- LaunchDarkly Java Server SDK 7.16.0 for feature flags
- Maven for build management
- dotenv-java for environment variable management

### Project Structure

```
src/main/java/dev/ - Source code
├── chart/     - Chart components and utilities
├── component/ - UI components
├── config/    - LaunchDarkly configuration
├── event/     - Menu events
├── form/      - Application forms
├── main/      - Application entry point
└── swing/     - Custom Swing components
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
