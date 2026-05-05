import 'sfa_utils_platform_interface.dart';

class SfaUtils {
  Future<int> getBatteryPercentage() {
    return SfaUtilsPlatform.instance.getBatteryPercentage();
  }
}
