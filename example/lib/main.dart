import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:alps_finger_scan/alps_finger_scan.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  String _fingerStatus = '';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    AlpsFingerScan.stream.receiveBroadcastStream().listen(_updateTimer);

    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await AlpsFingerScan.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    bool platformVersion2;

    try {
      // platformVersion2 = await AlpsFingerScan.openConnection;
    } on PlatformException {
      platformVersion2 = false;
    }

    print(platformVersion);
    print(platformVersion2);

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  void _openFingerConnection() async {
    bool success;
    try {
      success = await AlpsFingerScan.openConnection;
    } on PlatformException {
      success = false;
    }
    print(success);
  }

  void _closeFingerConnection() async {
    bool success;
    try {
      success = await AlpsFingerScan.closeConnection;
    } on PlatformException {
      success = false;
    }
    print(success);
  }

  void _clearFingerDatabase() async {
    bool success;
    try {
      success = await AlpsFingerScan.clearDatabase;
    } on PlatformException {
      success = false;
    }
    print(success);
  }

  void _registerFinger() async {
    bool success;
    try {
      success = await AlpsFingerScan.registerFinger;
    } on PlatformException {
      success = false;
    }
    print(success);
  }

  void _updateTimer(dynamic timer) {
    debugPrint("Timer $timer");
    print("Timer $timer");
    setState(() => _fingerStatus += "\n$timer");
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
            child: Column(
          children: <Widget>[
            Text(
              '$_fingerStatus',
              style: TextStyle(color: Colors.deepPurple, fontSize: 28),
            ),
            SizedBox(
              height: 20,
            ),
            RaisedButton(
              child: Text('open connection'),
              textColor: Colors.white,
              color: Colors.blue,
              onPressed: () => this._openFingerConnection(),
            ),
            SizedBox(
              height: 20,
            ),
            RaisedButton(
              child: Text('close connection'),
              textColor: Colors.white,
              color: Colors.blue,
              onPressed: () => this._closeFingerConnection(),
            ),
            SizedBox(
              height: 20,
            ),
            RaisedButton(
              child: Text('clear finger database'),
              textColor: Colors.white,
              color: Colors.blue,
              onPressed: () => this._clearFingerDatabase(),
            ),
            SizedBox(
              height: 20,
            ),
            RaisedButton(
              child: Text('register finger'),
              textColor: Colors.white,
              color: Colors.blue,
              onPressed: () => this._registerFinger(),
            )
          ],
        )),
      ),
    );
  }
}
