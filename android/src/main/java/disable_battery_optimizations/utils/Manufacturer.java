package disable_battery_optimizations.utils;

import androidx.annotation.NonNull;

public enum Manufacturer {
    XIAOMI("xiaomi"),
    SAMSUNG("samsung"),
    OPPO("oppo"),
    HUAWEI("huawei"),
    MEIZU("meizu"),
    ONEPLUS("oneplus"),
    LETV("letv"),
    ASUS("asus"),
    HTC("htc"),
    ZTE("zte"),
    VIVO("vivo");

    private final String name;

    Manufacturer(String device){
        name = device;
    }
    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }
}
