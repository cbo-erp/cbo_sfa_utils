package disable_battery_optimizations.models;

public enum OptimizationVerificationStatus {
    VERIFIED,
    USER_CONFIRMED,
    FAILED,
    UNKNOWN,
    NOT_SUPPORTED;

    public boolean isBackgroundRestrictionDisabled() {
        return this == VERIFIED || this == USER_CONFIRMED || this == NOT_SUPPORTED;
    }

    public boolean isAutoStartEnabled() {
        return this == VERIFIED || this == USER_CONFIRMED || this == NOT_SUPPORTED;
    }

}
