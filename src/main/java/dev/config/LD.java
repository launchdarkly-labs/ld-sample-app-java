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

public class LD {
    private static LDContext context;
    private static LDClient client;
    private static String SDK_KEY = "";

    private static String defaultEmail = "";
    private static String defaultName = "";
    private static String defaultRole = "";
    private static String defaultTenant = "";
    
    // Feature flag keys in project nteixeira-ld-custom
    public static final String DASHBOARD_FLAG_KEY = "dashboard";
    public static final String FEATURE_FLAG_1_KEY = "dashboard-progress-meters";
    public static final String FEATURE_FLAG_2_KEY = "dashboard-line-chart";
    public static final String FEATURE_FLAG_3_KEY = "dashboard-bar-chart";
    
    static {
        loadEnvValues();
    }

    private static String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return "";
    }
    
    private static String fromEnvFile(Dotenv dotenv, String key) {
        for (DotenvEntry entry : dotenv.entries(Dotenv.Filter.DECLARED_IN_ENV_FILE)) {
            if (key.equals(entry.getKey())) {
                return firstNonEmpty(entry.getValue());
            }
        }
        return "";
    }

    private static void loadEnvValues() {
        File envFile = new File(".env");
        if (!envFile.exists()) {
            showMessage("Warning: .env file is not present");
            showMessage("Warning: LAUNCHDARKLY_SDK_KEY is not present in .env");
            return;
        }

        showMessage("Found .env file at: " + envFile.getPath());
        Dotenv dotenv = Dotenv.configure()
            .directory(".")
            .filename(".env")
            .load();
        SDK_KEY = fromEnvFile(dotenv, "LAUNCHDARKLY_SDK_KEY");
        defaultEmail = fromEnvFile(dotenv, "USER_EMAIL");
        defaultName = fromEnvFile(dotenv, "USER_NAME");
        defaultRole = fromEnvFile(dotenv, "USER_ROLE");
        defaultTenant = fromEnvFile(dotenv, "TENANT");
        if (!SDK_KEY.isEmpty()) {
            showMessage("Loaded SDK key from .env: " + maskSdkKey(SDK_KEY));
        }
        if (!defaultEmail.isEmpty()) {
            showMessage("Loaded user email from .env");
        }
        if (!defaultName.isEmpty()) {
            showMessage("Loaded user name from .env");
        }
        if (!defaultRole.isEmpty()) {
            showMessage("Loaded user role from .env");
        }
        if (!defaultTenant.isEmpty()) {
            showMessage("Loaded tenant from .env");
        }

        if (SDK_KEY.isEmpty()) {
            showMessage("Warning: LAUNCHDARKLY_SDK_KEY is not present in .env");
        }
    }
    
    public static String getSdkKey() {
        return SDK_KEY;
    }
    
    public static String getDefaultEmail() {
        return defaultEmail;
    }
    
    public static String getDefaultName() {
        return defaultName;
    }

    public static String getDefaultRole() {
        return defaultRole;
    }

    public static String getDefaultTenant() {
        return defaultTenant;
    }
    
    public static void initialize(String email, String name, String role, String tenant) throws Exception {
        String finalEmail = firstNonEmpty(email, defaultEmail);
        String finalName = firstNonEmpty(name, defaultName);
        String finalRole = firstNonEmpty(role, defaultRole);
        String finalTenant = firstNonEmpty(tenant, defaultTenant);

        if (SDK_KEY.isEmpty()) {
            throw new IllegalStateException("LAUNCHDARKLY_SDK_KEY is required in the .env file");
        }
        if (finalEmail.isEmpty() || finalName.isEmpty() || finalRole.isEmpty() || finalTenant.isEmpty()) {
            throw new IllegalStateException("User email, name, role, and tenant are required");
        }
        
        LDContext userContext = LDContext.builder(finalEmail)
            .kind("user")
            .name(finalName)
            .set("email", finalEmail)
            .set("role", finalRole)
            .build();

        LDContext tenantContext = LDContext.builder(finalTenant)
            .kind("tenant")
            .build();

        context = LDContext.createMulti(userContext, tenantContext);
        showMessage("Created multi-context for user " + finalName
            + " (email=" + finalEmail + ", role=" + finalRole + ") tenant=" + finalTenant);
        
        showMessage("Initializing LaunchDarkly client with " + maskSdkKey(SDK_KEY) + "...");
        client = new LDClient(SDK_KEY);
        boolean connected = client.getDataSourceStatusProvider()
            .waitFor(DataSourceStatusProvider.State.VALID, Duration.ofSeconds(10));
        DataSourceStatusProvider.Status dataSourceStatus = client.getDataSourceStatusProvider().getStatus();
        showMessage("SDK initialized=" + client.isInitialized()
            + " version=" + client.version()
            + " offline=" + client.isOffline()
            + " dataSource=" + dataSourceStatus.getState()
            + (dataSourceStatus.getLastError() == null ? "" : " lastError=" + dataSourceStatus.getLastError()));

        if (!connected || !client.isInitialized() || dataSourceStatus.getState() != DataSourceStatusProvider.State.VALID) {
            String error = dataSourceStatus.getLastError() == null
                ? "LaunchDarkly SDK failed to connect"
                : dataSourceStatus.getLastError().toString();
            client.close();
            client = null;
            context = null;
            throw new IllegalStateException(error);
        }

        showMessage("LaunchDarkly streaming connection is valid");
        logFlagEvaluation(DASHBOARD_FLAG_KEY);
        logFlagEvaluation(FEATURE_FLAG_1_KEY);
        logFlagEvaluation(FEATURE_FLAG_2_KEY);
        logFlagEvaluation(FEATURE_FLAG_3_KEY);
    }

    private static String maskSdkKey(String key) {
        if (key == null || key.length() < 8) {
            return "(invalid key)";
        }
        if (!key.startsWith("sdk-")) {
            return "(unexpected key format, expected sdk-... length " + key.length() + ")";
        }
        return "sdk-…" + key.substring(key.length() - 4) + " (length " + key.length() + ")";
    }

    private static void logFlagEvaluation(String flagKey) {
        EvaluationDetail<Boolean> detail = client.boolVariationDetail(flagKey, context, false);
        EvaluationReason reason = detail.getReason();
        showMessage("Evaluated " + flagKey + "=" + detail.getValue()
            + " reason=" + reason
            + (detail.isDefaultValue() ? " (SDK default)" : " (from LaunchDarkly)"));
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
