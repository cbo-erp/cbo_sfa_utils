package disable_battery_optimizations.models;

import androidx.annotation.DrawableRes;
import java.util.List;

public class BatteryGuide {
    public final String title;
    public final String description;
    public final List<String> steps;
    @DrawableRes public final int imageRes;
    public final String videoUrl;
    public final String warning;

    public BatteryGuide(String title, String description, List<String> steps, int imageRes, String videoUrl, String warning) {
        this.title = title;
        this.description = description;
        this.steps = steps;
        this.imageRes = imageRes;
        this.videoUrl = videoUrl;
        this.warning = warning;
    }
}
