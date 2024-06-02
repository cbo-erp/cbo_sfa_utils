import 'sfa_utils_platform_interface.dart';

class SfaUtils {
  Future<int> getBatteryPercentage() {
    return SfaUtilsPlatform.instance.getBatteryPercentage();
  }
}

class DataResponse<T> {
  T? data;
  bool isSuccess;
  String msg;

  DataResponse({
    required this.isSuccess,
    required this.data,
    required this.msg,
  });

  factory DataResponse.success(
    T data, {
    String msg = "",
  }) {
    return DataResponse(
      isSuccess: true,
      data: data,
      msg: msg,
    );
  }

  factory DataResponse.failure(String errorMsg, {T? data}) {
    return DataResponse(
      isSuccess: false,
      data: data,
      msg: errorMsg,
    );
  }
}
