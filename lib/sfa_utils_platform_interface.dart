import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:sfa_utils/models/os_detail.dart';
import 'package:sfa_utils/sfa_utils.dart';

import 'sfa_utils_method_channel.dart';

abstract class SfaUtilsPlatform extends PlatformInterface {
  /// Constructs a SfaUtilsPlatform.
  SfaUtilsPlatform() : super(token: _token);

  static final Object _token = Object();

  static SfaUtilsPlatform _instance = MethodChannelSfaUtils();

  /// The default instance of [SfaUtilsPlatform] to use.
  ///
  /// Defaults to [MethodChannelSfaUtils].
  static SfaUtilsPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [SfaUtilsPlatform] when
  /// they register themselves.
  static set instance(SfaUtilsPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<int> getBatteryPercentage() async {
    throw UnimplementedError(
        'getBatteryPercentage() has not been implemented.');
  }

  Future<String> getMobileIMEI() async {
    throw UnimplementedError('getMobileIMEI() has not been implemented.');
  }

  Future<bool> setMobileIMEI(String imei) async {
    throw UnimplementedError('setMobileIMEI() has not been implemented.');
  }

  Future<DeviceInfo> getOsDetail() async {
    throw UnimplementedError('getOsDetail() has not been implemented.');
  }

  Future<String> hasPermissionLocation() async {
    throw UnimplementedError(
        'hasPermissionLocation() has not been implemented.');
  }

  Future<bool> timeIsAuto() async {
    throw UnimplementedError('timeIsAuto() has not been implemented.');
  }

  Future<bool> developerModeOn() async {
    throw UnimplementedError('timeIsAuto() has not been implemented.');
  }

  Future<bool> openSetting() async {
    throw UnimplementedError('openSetting() has not been implemented.');
  }

  Future<bool> timeZoneIsAuto() async {
    throw UnimplementedError('timeZoneIsAuto() has not been implemented.');
  }

  Future<DataResponse<Map<String, dynamic>>> getLocation() async {
    throw UnimplementedError('getLocation() has not been implemented.');
  }

  /// This method must be awaited to avoid potential crashes in the app.
  /// Failing to await the Future may lead to unhandled exceptions or undefined behavior.
  Future<DataResponse<String>> requestGPS() async {
    throw UnimplementedError('requestGPS() has not been implemented.');
  }

  Future<bool> openFile(String filePath) async {
    throw UnimplementedError('openFile() has not been implemented.');
  }
}
