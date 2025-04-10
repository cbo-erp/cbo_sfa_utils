class DeviceInfo {
  final String platform;
  final String manufacturer;
  final String osVersion;
  final String deviceModel;
  final String sdkVersion;

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
      sdkVersion: map['sdk_version'] ?? "",
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
