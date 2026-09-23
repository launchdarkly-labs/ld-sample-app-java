package dev.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Looks up tenant metadata after login provides a trusted tenant_id (JWT claim in a real app). */
public final class TenantDirectory {
    public record Tenant(String id, String name, String plan, String region) {}

    private static final Map<String, Tenant> TENANTS = new LinkedHashMap<>();

    static {
        TENANTS.put("1001", new Tenant("1001", "Acme Corp", "enterprise", "us-east-1"));
        TENANTS.put("2002", new Tenant("2002", "Globex", "starter", "eu-west-1"));
        TENANTS.put("3003", new Tenant("3003", "Initech", "growth", "us-west-2"));
    }

    private TenantDirectory() {}

    public static Optional<Tenant> find(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(TENANTS.get(tenantId.trim()));
    }

    public static Tenant require(String tenantId) {
        return find(tenantId).orElseThrow(() -> new IllegalArgumentException(
            "Unknown tenant_id \"" + tenantId + "\". Known tenants: " + String.join(", ", knownIds())
        ));
    }

    public static Set<String> knownIds() {
        return TENANTS.keySet();
    }
}
