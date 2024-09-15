import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:sfa_utils/sfa_utils.dart';

import 'sfa_utils_platform_interface.dart';

/// An implementation of [SfaUtilsPlatform] that uses method channels.
class MethodChannelSfaUtils extends SfaUtilsPlatform {
  final methodChannel = const MethodChannel('com.cbo.sfa.utils.native');

  @override
  Future<int> getBatteryPercentage() async {
    try {
      var value = await methodChannel.invokeMethod("getBatteryPercentage", {});
      if (value != null && value is Map<dynamic, dynamic>) {
        return int.tryParse("${value["data"]}") ?? 0;
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
      if (value != null && value is Map<dynamic, dynamic>) {
        if (int.parse("${value["status"]}") != 1) {
          return DataResponse.failure(value["msg"] ?? "Failure");
        }

        final dataValue =
            value["data"].runtimeType == String ? {} : value["data"];

        Map<String, dynamic> data = Map<String, dynamic>.from(dataValue);

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
  Future<Map<String, dynamic>> getOsDetail() async {
    try {
      var value = await methodChannel.invokeMethod("getOsDetail", {});
      if (value != null && value is Map<dynamic, dynamic>) {
        if (int.parse("${value["status"]}") == 1) {
          final jsonString = jsonEncode(value["data"]);
          return Map<String, dynamic>.from(jsonDecode(jsonString));
        }
      }
      return {};
    } catch (e) {
      return {};
    }
  }

  @override
  Future<String> getMobileIMEI() async {
    try {
      final value = await methodChannel.invokeMethod("getMobileIMEI", {});
      if (value != null && value is Map<dynamic, dynamic>) {
        return "${value["data"]}";
      } else {
        return "";
      }
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
      if (value != null && value is Map<dynamic, dynamic>) {
        return value["data"] == 1;
      } else {
        return false;
      }
    } catch (e, s) {
      return false;
    }
  }

  @override
  Future<String> hasPermissionLocation() async {
    try {
      var value = await methodChannel.invokeMethod("hasLocationPermission", {});

      if (value != null && value is Map<dynamic, dynamic>) {
        debugPrint("channel >> hasPermissionLocation - ${jsonEncode(value)}");

        return value["data"].toString();
      } else {
        return "";
      }
    } catch (e) {
      return "";
    }
  }

  @override
  Future<bool> timeIsAuto() async {
    try {
      return await methodChannel.invokeMethod("timeIsAuto", {});
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
      return await methodChannel.invokeMethod("timeZoneIsAuto", {});
    } catch (e) {
      return false;
    }
  }

  @override
  Future<Map<String, dynamic>> requestGPS() async {
    try {
      return await methodChannel.invokeMethod("requestGPS", {});
    } catch (e) {
      return {"status": "0", "msg": "Technical Error $e"};
    }
  }
}
