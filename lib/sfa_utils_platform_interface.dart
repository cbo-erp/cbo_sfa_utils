import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:sfa_utils/models/os_detail.dart';

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
    throw UnimplementedError('developerModeOn() has not been implemented.');
  }

  Future<bool> startRecording() async {
    throw UnimplementedError('startRecording() has not been implemented.');
  }

  Future<bool> pauseRecording() async {
    throw UnimplementedError('pauseRecording() has not been implemented.');
  }

  Future<bool> resumeRecording() async {
    throw UnimplementedError('resumeRecording() has not been implemented.');
  }

  Future<String?> stopRecording() async {
    throw UnimplementedError('stopRecording() has not been implemented.');
  }

  Future<bool> openSetting() async {
    throw UnimplementedError('openSetting() has not been implemented.');
  }

  Future<bool> timeZoneIsAuto() async {
    throw UnimplementedError('timeZoneIsAuto() has not been implemented.');
  }

  Future<Map<String, dynamic>> getLocation() async {
    throw UnimplementedError('getLocation() has not been implemented.');
  }

  /// This method must be awaited to avoid potential crashes in the app.
  /// Failing to await the Future may lead to unhandled exceptions or undefined behavior.
  Future<bool> requestGPS() async {
    throw UnimplementedError('requestGPS() has not been implemented.');
  }

  Future<bool> openFile(String filePath) async {
    throw UnimplementedError('openFile() has not been implemented.');
  }

  Future<bool> showEnableAutoStartSettings(
    String title,
    String content,
  ) {
    throw UnimplementedError(
        'showEnableAutoStartSettings() has not been implemented.');
  }

  Future<bool> showDisableManufacturerBatteryOptimizationSettings(
    String title,
    String content,
  ) {
    throw UnimplementedError(
        'showDisableManufacturerBatteryOptimizationSettings() has not been implemented.');
  }

  Future<bool> showDisableBatteryOptimizationSettings() {
    throw UnimplementedError(
        'showDisableBatteryOptimizationSettings() has not been implemented.');
  }

  Future<bool> showDisableAllOptimizationsSettings(
    String autoStartTitle,
    String autoStartContent,
    String manBatteryTitle,
    String manBatteryContent,
  ) {
    throw UnimplementedError(
        'showDisableAllOptimizationsSettings() has not been implemented.');
  }

  Future<bool> get isAutoStartEnabled {
    throw UnimplementedError('isAutoStartEnabled has not been implemented.');
  }

  Future<bool> get isBatteryOptimizationDisabled {
    throw UnimplementedError(
        'isBatteryOptimizationDisabled has not been implemented.');
  }

  Future<bool> get isManufacturerBatteryOptimizationDisabled {
    throw UnimplementedError(
        'isManufacturerBatteryOptimizationDisabled has not been implemented.');
  }

  Future<bool> get isAllBatteryOptimizationDisabled {
    throw UnimplementedError(
        'isAllBatteryOptimizationDisabled has not been implemented.');
  }
}
