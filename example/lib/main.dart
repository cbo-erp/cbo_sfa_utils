import 'package:flutter/material.dart';
import 'dart:async';

// import 'package:flutter/services.dart';
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
  final _sfaUtilsPlugin = SfaUtils();

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Future<void> _isolateMain(RootIsolateToken rootIsolateToken) async {
  //   // Register the background isolate with the root isolate.
  //   BackgroundIsolateBinaryMessenger.ensureInitialized(rootIsolateToken);

  //   SfaUtilsPlatform.instance
  //       .getBatteryPercentage()
  //       .then((value) => print("value===> $value"));
  // }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    int batterLevelVal = await _sfaUtilsPlugin.getBatteryPercentage();

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
        body: Center(
          child: Text('Battery level: $batterLevel'),
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

    SfaUtilsPlatform.instance.requestGPS().then((value) {
      debugPrint("value1===> ${value.isSuccess}");
    });
  }

  // Future<void> _onPressed() async {
  //   var receivePort = ReceivePort();
  //   // Here runMyIsolate methos should be a top level function
  //   await Isolate.spawn<RootIsolateToken?>(
  //       runMyIsolate, RootIsolateToken.instance);
  //   print(await receivePort.first);
  // }
}

// We declare a top level function here for an isolated callback function
// void runMyIsolate(RootIsolateToken? args) {
//   print("In runMyIsolate ");
//   if (args == null) {
//     return;
//   }
//   BackgroundIsolateBinaryMessenger.ensureInitialized(args);
//   SfaUtilsPlatform.instance
//       .getBatteryPercentage()
//       .then((value) => print("value===> $value"));
//   // Isolate.exit(sendPort, args);
// }
