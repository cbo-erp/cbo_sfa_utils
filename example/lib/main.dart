import 'dart:io';
import 'dart:isolate';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:sfa_utils/models/os_detail.dart';
import 'package:sfa_utils/sfa_utils.dart';
import 'package:sfa_utils/sfa_utils_platform_interface.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  int batterLevel = -1;
  DeviceInfo? deviceInfo;
  bool webView = false;
  var gpsEnabled;
  final _sfaUtilsPlugin = SfaUtils();

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    int batterLevelVal = await _sfaUtilsPlugin.getBatteryPercentage();
    deviceInfo = await SfaUtilsPlatform.instance.getOsDetail();
    gpsEnabled = await SfaUtilsPlatform.instance.hasPermissionLocation();

    print("ABC");

    if (Platform.isIOS && deviceInfo!.sdkVersion < 13) {
      webView = false;
    } else {
      webView = true;
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      batterLevel = batterLevelVal;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('SFA PLUGIN EXAMPLEs'),
        ),
        body: SingleChildScrollView(
          child: Column(
            children: [
              Center(
                child: Text('Battery level: $batterLevel'),
              ),
              Center(
                child: Text('GPS Enabled: $gpsEnabled'),
              ),
              Center(
                child: Text('OS VERSION: ${deviceInfo?.sdkVersion ?? "N/A"}'),
              ),
              Center(
                child: Text('WEB view: $webView'),
              ),
              OutlinedButton(
                  onPressed: () async {
                    print("Getting Location");
                    final request =
                        await SfaUtilsPlatform.instance.getLocation();
                    print(request);
                  },
                  child: const Text(
                    "Request Location NOT WORKING",
                    style: TextStyle(color: Colors.red),
                  )),
              OutlinedButton(
                onPressed: () async {
                  print("Developer Mode check");
                  final request =
                      await SfaUtilsPlatform.instance.developerModeOn();
                  print(request);
                },
                child: const Text(
                  "Developer mode",
                ),
              ),
              OutlinedButton(
                onPressed: () async {
                  print("getBatteryPercentage");
                  final req =
                      await SfaUtilsPlatform.instance.getBatteryPercentage();
                  print("Request is $req");
                },
                child: const Text("getBatteryPercentage"),
              ),
              OutlinedButton(
                onPressed: () async {
                  print("getMobileIMEI");
                  final req = await SfaUtilsPlatform.instance.getMobileIMEI();
                  print("Request is $req");
                },
                child: const Text(
                  "getMobileIMEI  NOT WORKING",
                  style: TextStyle(color: Colors.red),
                ),
              ),
              OutlinedButton(
                onPressed: () async {
                  print("setMobileIMEI");
                  final req =
                      await SfaUtilsPlatform.instance.setMobileIMEI("saa");
                  print("Request is $req");
                },
                child: const Text("setMobileIMEI"),
              ),
              OutlinedButton(
                onPressed: () async {
                  print("getOsDetail");
                  final req = await SfaUtilsPlatform.instance.getOsDetail();
                  print("Request is $req");
                },
                child: const Text("getOsDetail"),
              ),
              OutlinedButton(
                onPressed: () async {
                  print("hasPermissionLocation");
                  final req =
                      await SfaUtilsPlatform.instance.hasPermissionLocation();
                  print("Request is $req");
                },
                child: const Text(
                  "hasPermissionLocation NOT WORKING",
                  style: TextStyle(color: Colors.red),
                ),
              ),
              OutlinedButton(
                onPressed: () async {
                  print("timeIsAuto");
                  final req = await SfaUtilsPlatform.instance.timeIsAuto();
                  print("Request is $req");
                },
                child: const Text("timeIsAuto"),
              ),
              OutlinedButton(
                onPressed: () async {
                  print("developerModeOn");
                  final req = await SfaUtilsPlatform.instance.developerModeOn();
                  print("Request is $req");
                },
                child: const Text("developerModeOn"),
              ),
              OutlinedButton(
                onPressed: () async {
                  print("openSetting");
                  final req = await SfaUtilsPlatform.instance.openSetting();
                  print("Request is $req");
                },
                child: const Text("openSetting"),
              ),
              OutlinedButton(
                onPressed: () async {
                  print("timeZoneIsAuto");
                  final req = await SfaUtilsPlatform.instance.timeZoneIsAuto();
                  print("Request is $req");
                },
                child: const Text("timeZoneIsAuto"),
              ),
              OutlinedButton(
                onPressed: () async {
                  print("getLocation");
                  final req = await SfaUtilsPlatform.instance.getLocation();
                  print("Request is $req");
                },
                child: const Text("getLocation"),
              ),
              OutlinedButton(
                onPressed: () async {
                  print("requestGPS");
                  final req = await SfaUtilsPlatform.instance.requestGPS();
                  print("Request is $req");
                },
                child: const Text("requestGPS"),
              ),
              OutlinedButton(
                onPressed: () async {
                  print("openFile");
                  // final req = await SfaUtilsPlatform.instance.openFile();
                  // print("Request is $req");
                },
                child: const Text("openFile"),
              ),
            ],
          ),
        ),
        floatingActionButton: FloatingActionButton(
          onPressed: _onPressedRequestGPS,
          // onPressed: _onPressed,
          child: const Icon(Icons.add_circle_outline),
        ),
      ),
    );
  }

  // Callback function for Text Button Event this should be a class member
  Future<void> _onPressedRequestGPS() async {
    // BackgroundIsolateBinaryMessenger.ensureInitialized(rootIsolateToken);

    final value = await SfaUtilsPlatform.instance.requestGPS();

    print("value1===> ${value.isSuccess}");
    print("value2===> ${value.msg}");
    print("value3===> ${value.data}");
  }

  Future<void> _onPressed() async {
    var receivePort = ReceivePort();
    // Here runMyIsolate methos should be a top level function
    await Isolate.spawn<RootIsolateToken?>(
        runMyIsolate, RootIsolateToken.instance);
    print(await receivePort.first);
  }
}

// We declare a top level function here for an isolated callback function
void runMyIsolate(RootIsolateToken? args) {
  print("In runMyIsolate ");
  if (args == null) {
    return;
  }
  BackgroundIsolateBinaryMessenger.ensureInitialized(args);
  SfaUtilsPlatform.instance
      .getBatteryPercentage()
      .then((value) => print("value===> $value"));
  // Isolate.exit(sendPort, args);
}
