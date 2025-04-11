import 'dart:convert';
import 'dart:io';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:sfa_utils/models/os_detail.dart';
import 'package:sfa_utils/sfa_utils.dart';

import 'sfa_utils_platform_interface.dart';

/// An implementation of [SfaUtilsPlatform] that uses method channels.
class MethodChannelSfaUtils extends SfaUtilsPlatform {
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
      print("error $e");
      return -1;
    }
  }

  @override
  Future<DataResponse<Map<String, dynamic>>> getLocation() async {
    try {
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
        return DataResponse.success(map);
      } else {
        return DataResponse.failure("GPS Permission denied");
      }
    } on PlatformException catch (e, stacktrace) {
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
      return DataResponse.failure("Platform Error ${e.code}");
    } catch (e) {
      return DataResponse.failure("Technical Error ${e.toString()}");
    }
  }

  @override
  Future<DeviceInfo> getOsDetail() async {
    try {
      var value = await methodChannel.invokeMethod("getOsDetail", {});
      debugPrint("object $value");
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
    } catch (e, s) {
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
  Future<DataResponse<String>> requestGPS() async {
    try {
      await methodChannel.invokeMethod("requestGPS", {});
      return DataResponse.success("");
    } catch (e) {
      return DataResponse.failure(
        "GPS Permission denied",
        data: e.toString(),
      );
    }
  }

  @override
  Future<bool> openFile(String filePath) async {
    try {
      await methodChannel.invokeMethod("openFile", {"filePath": filePath});
      return true;
    } catch (e) {
      return false;
    }
  }
}
