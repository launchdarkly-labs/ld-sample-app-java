package dev.config;

import com.launchdarkly.sdk.EvaluationDetail;
import com.launchdarkly.sdk.EvaluationReason;
import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.LDClient;
import com.launchdarkly.sdk.server.interfaces.DataSourceStatusProvider;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import java.io.File;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class LD {
    public static final String DASHBOARD_FLAG_KEY = "dashboard";
    public static final String FEATURE_FLAG_1_KEY = "dashboard-progress-meters";
    public static final String FEATURE_FLAG_2_KEY = "dashboard-line-chart";
    public static final String FEATURE_FLAG_3_KEY = "dashboard-bar-chart";

    private static final Pattern SEMVER = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)$");
    private static final String APPLICATION_KEY = "java-sdk-demo";

    private static LDContext context;
    private static LDClient client;
    private static String sdkKey = "";
    private static String defaultEmail = "";
    private static String defaultName = "";
    private static String defaultRole = "";
    private static String defaultTenantId = "";
    private static String appVersion = "";
    private static String gitSha = "";
    private static String deploymentRing = "";

    static {
        loadEnvFile();
    }

    private static void loadEnvFile() {
        File envFile = new File(".env");
        if (!envFile.exists()) {
            showMessage("Warning: .env file is not present");
            return;
        }

        showMessage("Loading " + envFile.getPath());
        Map<String, String> env = readEnvFile();
        sdkKey = env.getOrDefault("LAUNCHDARKLY_SDK_KEY", "");
        defaultEmail = env.getOrDefault("USER_EMAIL", "");
        defaultName = env.getOrDefault("USER_NAME", "");
        defaultRole = env.getOrDefault("USER_ROLE", "");
        defaultTenantId = env.getOrDefault("TENANT_ID", "");
        appVersion = env.getOrDefault("APP_VERSION", "");
        gitSha = env.getOrDefault("GIT_SHA", "");
        deploymentRing = env.getOrDefault("DEPLOYMENT_RING", "");

        if (sdkKey.isEmpty()) {
            showMessage("Warning: LAUNCHDARKLY_SDK_KEY is not present in .env");
        } else {
            showMessage("Loaded SDK key from .env: " + maskSdkKey(sdkKey));
        }
    }

    private static Map<String, String> readEnvFile() {
        Dotenv dotenv = Dotenv.configure().directory(".").filename(".env").load();
        Map<String, String> values = new LinkedHashMap<>();
        for (DotenvEntry entry : dotenv.entries(Dotenv.Filter.DECLARED_IN_ENV_FILE)) {
            if (entry.getValue() != null && !entry.getValue().isBlank()) {
                values.put(entry.getKey(), entry.getValue().trim());
            }
        }
        return values;
    }

    public static String getSdkKey() { return sdkKey; }
    public static String getDefaultEmail() { return defaultEmail; }
    public static String getDefaultName() { return defaultName; }
    public static String getDefaultRole() { return defaultRole; }
    public static String getDefaultTenantId() { return defaultTenantId; }

    /**
     * Connects the SDK and builds the evaluation multi-context.
     * Login (see {@code Main.showSetupDialog}) provides user + tenant_id;
     * {@link TenantDirectory} fills tenant attributes; {@code .env} fills application attributes.
     */
    public static void initialize(String email, String name, String role, String tenantId) throws Exception {
        require(sdkKey, "LAUNCHDARKLY_SDK_KEY is required in the .env file");
        require(email, "User email is required");
        require(name, "User name is required");
        require(role, "User role is required");
        require(tenantId, "tenant_id is required");
        require(appVersion, "APP_VERSION is required in the .env file");
        require(gitSha, "GIT_SHA is required in the .env file");
        require(deploymentRing, "DEPLOYMENT_RING is required in the .env file");
        if (!SEMVER.matcher(appVersion).matches()) {
            throw new IllegalStateException("APP_VERSION must be MAJOR.MINOR.PATCH, got: " + appVersion);
        }

        TenantDirectory.Tenant tenantRecord = TenantDirectory.require(tenantId);
        context = buildEvaluationContext(email, name, role, tenantRecord);
        showMessage("Evaluation context: user=" + name + " tenant=" + tenantRecord.id()
            + " (" + tenantRecord.name() + ", " + tenantRecord.plan() + ", " + tenantRecord.region() + ")"
            + " app=" + APPLICATION_KEY + " " + appVersion + " ring=" + deploymentRing);

        connectClient();
        logFlagEvaluation(DASHBOARD_FLAG_KEY);
        logFlagEvaluation(FEATURE_FLAG_1_KEY);
        logFlagEvaluation(FEATURE_FLAG_2_KEY);
        logFlagEvaluation(FEATURE_FLAG_3_KEY);
    }

    /**
     * Builds the LaunchDarkly multi-context used for every flag evaluation.
     * This is the method to read first when changing targeting attributes.
     */
    static LDContext buildEvaluationContext(
            String userId,
            String userDisplayName,
            String role,
            TenantDirectory.Tenant tenantRecord) {

        // User: identity from login (JWT analog). Targeting: role.
        LDContext user = LDContext.builder(userId)
            .kind("user")
            .name(userDisplayName)
            .set("role", role)
            .build();

        // Tenant: id from login; name/plan/region from TenantDirectory.
        LDContext tenant = LDContext.builder(tenantRecord.id())
            .kind("tenant")
            .name(tenantRecord.name())
            .set("plan", tenantRecord.plan())
            .set("region", tenantRecord.region())
            .build();

        // Application: service metadata from .env (APP_VERSION, GIT_SHA, DEPLOYMENT_RING).
        LDContext application = LDContext.builder(APPLICATION_KEY)
            .kind("application")
            .set("version", appVersion)
            .set("gitSha", gitSha)
            .set("ring", deploymentRing)
            .build();

        return LDContext.multiBuilder()
            .add(user)
            .add(tenant)
            .add(application)
            .build();
    }

    private static void connectClient() throws Exception {
        showMessage("Connecting LaunchDarkly client with " + maskSdkKey(sdkKey) + "...");
        client = new LDClient(sdkKey);
        DataSourceStatusProvider dataSource = client.getDataSourceStatusProvider();
        boolean connected = dataSource.waitFor(DataSourceStatusProvider.State.VALID, Duration.ofSeconds(10));
        DataSourceStatusProvider.Status status = dataSource.getStatus();
        showMessage("SDK initialized=" + client.isInitialized()
            + " version=" + client.version()
            + " dataSource=" + status.getState());

        if (!connected || !client.isInitialized() || status.getState() != DataSourceStatusProvider.State.VALID) {
            String error = status.getLastError() == null
                ? "LaunchDarkly SDK failed to connect"
                : status.getLastError().toString();
            client.close();
            client = null;
            context = null;
            throw new IllegalStateException(error);
        }
        showMessage("LaunchDarkly streaming connection is valid");
    }

    private static void require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(message);
        }
    }

    private static String maskSdkKey(String key) {
        if (key == null || key.length() < 8 || !key.startsWith("sdk-")) {
            return "(unexpected key format)";
        }
        return "sdk-…" + key.substring(key.length() - 4);
    }

    private static void logFlagEvaluation(String flagKey) {
        EvaluationDetail<Boolean> detail = client.boolVariationDetail(flagKey, context, false);
        EvaluationReason reason = detail.getReason();
        showMessage("Evaluated " + flagKey + "=" + detail.getValue() + " reason=" + reason);
        if (reason.getKind() == EvaluationReason.Kind.ERROR
            && reason.getErrorKind() != EvaluationReason.ErrorKind.FLAG_NOT_FOUND) {
            throw new IllegalStateException("Flag evaluation error for " + flagKey + ": " + reason);
        }
        if (reason.getKind() == EvaluationReason.Kind.ERROR
            && reason.getErrorKind() == EvaluationReason.ErrorKind.FLAG_NOT_FOUND) {
            showMessage("Warning: " + flagKey + " was not found in this SDK key's project/environment");
        }
    }

    public static LDContext getContext() {
        if (context == null) {
            throw new IllegalStateException("LaunchDarkly context is not initialized");
        }
        return context;
    }

    public static String getContextLabel() {
        LDContext user = getContext().getIndividualContext("user");
        if (user != null && user.getName() != null && !user.getName().isEmpty()) {
            return user.getName();
        }
        return getContext().getKey();
    }

    public static LDClient getClient() {
        if (client == null) {
            throw new IllegalStateException("LaunchDarkly client is not initialized");
        }
        return client;
    }

    public static void showMessage(String s) {
        System.out.println("*** " + s + " ***\n");
    }
}
