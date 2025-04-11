class DeviceInfo {
  /// possible values : android / ios
  final String platform;

  /// possible values : apple/ samsung / huawei / oppo / vivo / xiaomi / oneplus / realme / google
  final String manufacturer;

  /// possible values
  /// android : 10 / 11 / 12 / 13 / 14
  /// ios : 10.3.4 / 11.0 / 12.0 / 13.0 / 14.0
  final String osVersion;

  /// possible values : iPhone 12 / iPhone 13 / iPhone 14 / iPhone 15 / Samsung Galaxy S21 / Samsung Galaxy S22
  final String deviceModel;

  /// possible values:
  /// android : sdk_int values -> 21,22,23,24,25
  /// ios : major os version -> 10, 11, 12, 13, 14, 15
  final int sdkVersion;

  DeviceInfo({
    required this.platform,
    required this.manufacturer,
    required this.osVersion,
    required this.deviceModel,
    required this.sdkVersion,
  });

  factory DeviceInfo.fromMap(Map<String, dynamic> map) {
    return DeviceInfo(
      platform: map['platform'] ?? "",
      manufacturer: map['manufacturer'] ?? "",
      osVersion: "${map['os_version'] ?? ""}",
      deviceModel: map['device_model'] ?? "",
      sdkVersion: int.tryParse("${map['sdk_version']}") ?? 0,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'platform': platform,
      'manufacturer': manufacturer,
      'os_version': osVersion,
      'device_model': deviceModel,
      'sdk_version': sdkVersion,
    };
  }

  @override
  String toString() {
    return 'DeviceInfoModel(platform: $platform, manufacturer: $manufacturer, osVersion: $osVersion, deviceModel: $deviceModel)';
  }
}
