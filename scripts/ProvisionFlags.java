import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Standalone provisioner (not part of the Swing app).
 *
 * Creates the demo flags in nteixeira-ld-custom and wires dashboard as a
 * prerequisite. Uses only the JDK — no Maven, no extra libraries.
 *
 *   java scripts/ProvisionFlags.java --api-key api-xxxxxxxx
 */
public class ProvisionFlags {
    private static final String DEFAULT_PROJECT = "nteixeira-ld-custom";
    private static final String API = "https://app.launchdarkly.com/api/v2";
    private static final String TAG = "java-sdk-demo";
    private static final List<String> ENVIRONMENTS = List.of("production", "test");

    private static final List<FlagSpec> FLAGS = List.of(
        new FlagSpec("dashboard", "Dashboard",
            "Keystone flag. Must be on (true) before dashboard UI flags can serve true."),
        new FlagSpec("dashboard-progress-meters", "Dashboard Progress Meters",
            "Controls visibility of the dashboard progress meters."),
        new FlagSpec("dashboard-line-chart", "Dashboard Line Chart",
            "Controls visibility of the dashboard line chart."),
        new FlagSpec("dashboard-bar-chart", "Dashboard Bar Chart",
            "Controls visibility of the dashboard bar chart.")
    );

    private static final List<String> PREREQUISITE_CHILDREN = List.of(
        "dashboard-progress-meters",
        "dashboard-line-chart",
        "dashboard-bar-chart"
    );

    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .build();
    private final String apiKey;
    private final String project;

    record FlagSpec(String key, String name, String description) {}

    public static void main(String[] args) throws Exception {
        String apiKey = null;
        String project = DEFAULT_PROJECT;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--api-key" -> apiKey = requireValue(args, ++i, "--api-key");
                case "--project" -> project = requireValue(args, ++i, "--project");
                case "-h", "--help" -> {
                    usage();
                    return;
                }
                default -> {
                    System.err.println("Unknown argument: " + args[i]);
                    usage();
                    System.exit(2);
                }
            }
        }
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("Missing --api-key (LaunchDarkly REST API token, not the SDK key).");
            usage();
            System.exit(2);
        }

        new ProvisionFlags(apiKey, project).run();
    }

    private static String requireValue(String[] args, int index, String flag) {
        if (index >= args.length) {
            throw new IllegalArgumentException(flag + " requires a value");
        }
        return args[index];
    }

    private static void usage() {
        System.out.println("""
            Usage:
              java scripts/ProvisionFlags.java --api-key <LAUNCHDARKLY_API_TOKEN> [--project nteixeira-ld-custom]

            Creates boolean flags used by the Java SDK demo and sets dashboard as a prerequisite
            on the three UI flags. Existing flags are left in place.
            """);
    }

    ProvisionFlags(String apiKey, String project) {
        this.apiKey = apiKey;
        this.project = project;
    }

    void run() throws Exception {
        System.out.println("Provisioning flags in project " + project);
        for (FlagSpec flag : FLAGS) {
            ensureFlag(flag);
        }

        String dashboardTrueId = trueVariationId(getFlag("dashboard"));
        for (String child : PREREQUISITE_CHILDREN) {
            for (String env : ENVIRONMENTS) {
                addPrerequisite(child, env, "dashboard", dashboardTrueId);
            }
        }
        System.out.println("Done.");
    }

    private void ensureFlag(FlagSpec flag) throws Exception {
        HttpResponse<String> created = post("/flags/" + project, """
            {
              "key": "%s",
              "name": "%s",
              "description": "%s",
              "temporary": false,
              "tags": ["%s"],
              "defaults": { "onVariation": 0, "offVariation": 1 }
            }
            """.formatted(flag.key(), jsonEscape(flag.name()), jsonEscape(flag.description()), TAG));

        if (created.statusCode() == 201) {
            System.out.println("Created " + flag.key());
            return;
        }
        if (created.statusCode() == 409) {
            System.out.println("Already exists " + flag.key());
            return;
        }
        throw new IllegalStateException("Create " + flag.key() + " failed: " + created.statusCode() + " " + created.body());
    }

    private String getFlag(String key) throws Exception {
        HttpResponse<String> response = get("/flags/" + project + "/" + key);
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Get " + key + " failed: " + response.statusCode() + " " + response.body());
        }
        return response.body();
    }

    private void addPrerequisite(String flagKey, String env, String prerequisiteKey, String variationId) throws Exception {
        HttpResponse<String> response = patch("/flags/" + project + "/" + flagKey, """
            {
              "comment": "Require %s=true before this UI flag can serve true",
              "environmentKey": "%s",
              "instructions": [{
                "kind": "addPrerequisite",
                "key": "%s",
                "variationId": "%s"
              }]
            }
            """.formatted(prerequisiteKey, env, prerequisiteKey, variationId));

        int status = response.statusCode();
        if (status == 200) {
            System.out.println("Set prerequisite " + prerequisiteKey + " on " + flagKey + " (" + env + ")");
            return;
        }
        if (status == 400 && response.body().contains("already")) {
            System.out.println("Prerequisite already set on " + flagKey + " (" + env + ")");
            return;
        }
        if (status == 405) {
            System.out.println("Skipped " + flagKey + " in " + env + " (environment requires approval)");
            return;
        }
        throw new IllegalStateException(
            "Prerequisite " + flagKey + " " + env + " failed: " + status + " " + response.body());
    }

    private static String trueVariationId(String flagJson) {
        Matcher matcher = Pattern.compile("\"value\"\\s*:\\s*true\\s*,\\s*\"_id\"\\s*:\\s*\"([^\"]+)\"")
            .matcher(flagJson);
        if (matcher.find()) {
            return matcher.group(1);
        }
        matcher = Pattern.compile("\"_id\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"value\"\\s*:\\s*true")
            .matcher(flagJson);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalStateException("Could not find true variation id in flag JSON");
    }

    private HttpResponse<String> get(String path) throws Exception {
        return send(request(path).GET().build());
    }

    private HttpResponse<String> post(String path, String body) throws Exception {
        return send(request(path)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build());
    }

    private HttpResponse<String> patch(String path, String body) throws Exception {
        return send(request(path)
            .header("Content-Type", "application/json; domain-model=launchdarkly.semanticpatch")
            .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
            .build());
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder()
            .uri(URI.create(API + path))
            .timeout(Duration.ofSeconds(30))
            .header("Authorization", apiKey)
            .header("Accept", "application/json");
    }

    private HttpResponse<String> send(HttpRequest request) throws Exception {
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String jsonEscape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
