import 'dart:convert';
import 'dart:io';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:sfa_utils/models/os_detail.dart';

import 'sfa_utils_platform_interface.dart';

/// An implementation of [SfaUtilsPlatform] that uses method channels.
class MethodChannelSfaUtils extends SfaUtilsPlatform {
  Future<Map<String, dynamic>>? _ongoingLocationRequest;
  Future<bool>? _ongoingRequestGPSRequest;
  final methodChannel = const MethodChannel('com.cbo.sfa.utils.native');

  @override
  Future<int> getBatteryPercentage() async {
    try {
      var value = await methodChannel.invokeMethod("getBatteryPercentage", {});
      if (value != null) {
        return int.tryParse("$value") ?? 0;
      } else {
        return 0;
      }
    } catch (e) {
      debugPrint("error $e");
      return -1;
    }
  }

  @override
  Future<Map<String, dynamic>> getLocation() async {
    if (_ongoingLocationRequest != null) {
      // Return the ongoing request if already in progress
      debugPrint(
          "MethodChannelSfaUtils:Another getLocation request in progress, waiting for completion");
      return _ongoingLocationRequest!;
    }

    // Assign the future to prevent duplicate calls
    debugPrint(
        "MethodChannelSfaUtils:Making new getLocation request and waiting for completion");
    _ongoingLocationRequest = _fetchLocation();

    // Wait for result and reset the future afterward
    try {
      final result = await _ongoingLocationRequest!;
      debugPrint("MethodChannelSfaUtils:_ongoingLocationRequest completed");
      return result;
    } finally {
      _ongoingLocationRequest = null;
    }
  }

  Future<Map<String, dynamic>> _fetchLocation() async {
    try {
      if (!Platform.isAndroid) {
        throw Exception(
          "Method call not allowed for this platform ${Platform.operatingSystem}",
        );
      }

      var value = await methodChannel.invokeMethod("getLocation", {});
      if (value != null) {
        Map<String, dynamic> data = Map<String, dynamic>.from(value);

        Map<String, dynamic> map = {
          'latitude': data["latitude"] ?? 0.0,
          'longitude': data["longitude"] ?? 0.0,
          'speed': data["speed"] ?? 0.0,
          'accuracy': data["accuracy"] ?? 0.0,
          'altitude': data["altitude"] ?? 0.0,
          'isMock': data["isMock"] ?? false,
          // 'hasAccuracy': _data["hasAccuracy"] ?? false,
          // 'hasAltitude': _data["hasAltitude"] ?? false,
          // 'hasSpeed': _data["hasSpeed"] ?? false,
          'speed_accuracy': 0.0,
          'heading': 0.0,
          'verticalAccuracy': 0.0,
          'headingAccuracy': 0.0,
          'time': 0.0,
          'elapsedRealtimeNanos': 0.0,
          'elapsedRealtimeUncertaintyNanos': 0.0,
          'satelliteNumber': 0,
          'provider': "GPS"
        };
        return map;
      } else {
        throw Exception("GPS Permission denied");
      }
    } on PlatformException catch (e, s) {
      switch (e.code) {
        case "LOCATION_NOT_FOUND":
        case "FAKE_GPS_DETECTED":
          // Handle permission denied error
          break;
        case "PERMISSION_DENIED_NEVER_ASK":
          // Handle permission permanently denied error
          break;
        case "LOCATION_SERVICE_DISABLED":
          // Handle location service disabled error
          break;
        case "PERMISSION_DENIED":
          // Handle fake GPS detected error
          break;
        default:
          // Handle other platform exceptions
          break;
      }

      throw Exception("Platform Error ${e.code}, $s");
    } catch (e) {
      throw Exception("Technical Error ${e.toString()}");
    }
  }

  @override
  Future<DeviceInfo> getOsDetail() async {
    try {
      var value = await methodChannel.invokeMethod("getOsDetail", {});
      return DeviceInfo.fromMap(jsonDecode(jsonEncode(value)));
    } catch (e) {
      debugPrint("exception $e");
      return DeviceInfo.fromMap({});
    }
  }

  @override
  Future<String> getMobileIMEI() async {
    try {
      final value = await methodChannel.invokeMethod("getMobileIMEI", {});
      return value != null ? value.toString() : "";
    } catch (e) {
      return "";
    }
  }

  @override
  Future<bool> setMobileIMEI(String imei) async {
    try {
      var value = await methodChannel.invokeMethod(
        "setMobileIMEI",
        {"uniqueToken": imei},
      );
      return value != null;
    } catch (e) {
      return false;
    }
  }

  @override
  Future<String> hasPermissionLocation() async {
    try {
      var value = await methodChannel.invokeMethod("hasLocationPermission", {});
      debugPrint("channel >> hasPermissionLocation - $value}");
      return value.toString();
    } catch (e) {
      return "";
    }
  }

  @override
  Future<bool> timeIsAuto() async {
    try {
      return Platform.isAndroid
          ? await methodChannel.invokeMethod("timeIsAuto", {})
          : true;
    } catch (e) {
      return true;
    }
  }

  @override
  Future<bool> developerModeOn() async {
    try {
      return Platform.isAndroid
          ? await methodChannel.invokeMethod("developerModeOn", {})
          : false;
    } catch (e) {
      return false;
    }
  }

  @override
  Future<bool> startRecording() async {
    try {
      return await methodChannel.invokeMethod("startRecording", {});
    } catch (e) {
      return false;
    }
  }

  @override
  Future<bool> pauseRecording() async {
    try {
      return await methodChannel.invokeMethod("pauseRecording", {});
    } catch (e) {
      return false;
    }
  }

  @override
  Future<bool> resumeRecording() async {
    try {
      return await methodChannel.invokeMethod("resumeRecording", {});
    } catch (e) {
      return false;
    }
  }

  @override
  Future<String?> stopRecording() async {
    try {
      return await methodChannel.invokeMethod('stopRecording');
    } catch (e) {
      return "";
    }
  }

  @override
  Future<bool> openSetting() async {
    try {
      await methodChannel.invokeMethod("openSetting", {});
      return true;
    } catch (e) {
      return false;
    }
  }

  @override
  Future<bool> timeZoneIsAuto() async {
    try {
      return Platform.isAndroid
          ? await methodChannel.invokeMethod("timeZoneIsAuto", {})
          : true;
    } catch (e) {
      return true;
    }
  }

  @override
  Future<bool> requestGPS() async {
    if (_ongoingRequestGPSRequest != null) {
      // Return the ongoing request if already in progress
      debugPrint(
          "MethodChannelSfaUtils:Another requestGPS request in progress, waiting for completion");
      return _ongoingRequestGPSRequest!;
    }

    // Assign the future to prevent duplicate calls
    debugPrint(
        "MethodChannelSfaUtils:Making new requestGPS request and waiting for completion");
    _ongoingRequestGPSRequest = _requestGpsPermission();

    // Wait for result and reset the future afterward
    try {
      final result = await _ongoingRequestGPSRequest!;

      debugPrint("MethodChannelSfaUtils:_ongoingRequestGPSRequest completed");
      return result;
    } finally {
      _ongoingRequestGPSRequest = null;
    }
  }

  Future<bool> _requestGpsPermission() async {
    try {
      if (!Platform.isAndroid) {
        throw Exception(
          "Method call not allowed for this platform ${Platform.operatingSystem}",
        );
      }
      var result = await methodChannel.invokeMethod("requestGPS", {});
      return result == true;
    } catch (e) {
      debugPrint("error while requesting GPS >>>> ${e.toString()}");
      throw Exception("GPS Permission denied");
    }
  }

  @override
  Future<bool> openFile(String filePath) async {
    try {
      await methodChannel.invokeMethod("openFile", {"filePath": filePath});
      return true;
    } catch (e) {
      debugPrint("openFile exception $e");
      return false;
    }
  }

  @override
  Future<bool> showEnableAutoStartSettings(
    String title,
    String content,
  ) async {
    try {
      if (!Platform.isAndroid) return true;
      bool? result = await methodChannel.invokeMethod<bool?>(
        'showEnableAutoStart',
        {"title": title, "content": content},
      );
      return result ?? false;
    } catch (e) {
      debugPrint("showEnableAutoStartSettings exception $e");
      return false;
    }
  }

  @override
  Future<bool> showDisableManufacturerBatteryOptimizationSettings(
    String title,
    String content,
  ) async {
    try {
      if (!Platform.isAndroid) return true;
      bool? result = await methodChannel.invokeMethod<bool?>(
        'showDisableManBatteryOptimization',
        {"title": title, "content": content},
      );
      return result ?? false;
    } catch (e) {
      debugPrint(
          "showDisableManufacturerBatteryOptimizationSettings exception $e");
      return false;
    }
  }

  @override
  Future<bool> showDisableBatteryOptimizationSettings() async {
    try {
      if (!Platform.isAndroid) return true;
      bool? result = await methodChannel.invokeMethod<bool?>(
        'showDisableBatteryOptimization',
      );
      return result ?? false;
    } catch (e) {
      debugPrint("showDisableBatteryOptimizationSettings exception $e");
      return false;
    }
  }

  @override
  Future<bool> showDisableAllOptimizationsSettings(
    String autoStartTitle,
    String autoStartContent,
    String manBatteryTitle,
    String manBatteryContent,
  ) async {
    try {
      if (!Platform.isAndroid) return true;
      bool? result = await methodChannel.invokeMethod<bool?>(
        'disableAllOptimizations',
        {
          "autoStartTitle": autoStartTitle,
          "autoStartContent": autoStartContent,
          "manBatteryTitle": manBatteryTitle,
          "manBatteryContent": manBatteryContent,
        },
      );
      return result ?? false;
    } catch (e) {
      debugPrint("showDisableAllOptimizationsSettings exception $e");
      return false;
    }
  }

  @override
  Future<bool> get isAutoStartEnabled async {
    try {
      if (!Platform.isAndroid) return true;
      bool? value =
          await methodChannel.invokeMethod<bool?>("isAutoStartEnabled");
      return value ?? true;
    } catch (e) {
      return true;
    }
  }

  @override
  Future<bool> get isBatteryOptimizationDisabled async {
    try {
      if (!Platform.isAndroid) return true;
      bool? value = await methodChannel
          .invokeMethod<bool?>("isBatteryOptimizationDisabled");
      return value ?? true;
    } catch (e) {
      return true;
    }
  }

  @override
  Future<bool> get isManufacturerBatteryOptimizationDisabled async {
    try {
      if (!Platform.isAndroid) return true;
      bool? value = await methodChannel
          .invokeMethod<bool?>("isManBatteryOptimizationDisabled");
      return value ?? true;
    } catch (e) {
      return true;
    }
  }

  @override
  Future<bool> get isAllBatteryOptimizationDisabled async {
    try {
      if (!Platform.isAndroid) return true;
      bool? value =
          await methodChannel.invokeMethod<bool?>("isAllOptimizationsDisabled");
      return value ?? true;
    } catch (e) {
      return true;
    }
  }
}
