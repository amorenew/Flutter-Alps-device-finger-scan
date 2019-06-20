import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

class AlpsFingerScan {
  static const MethodChannel _channel = const MethodChannel('alps_finger_scan');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool> get openConnection async {
    final bool success = await _channel.invokeMethod('openConnection');
    return success;
  }

  static Future<bool> get closeConnection async {
    final bool success = await _channel.invokeMethod('closeConnection');
    return success;
  }

  static Future<bool> get clearDatabase async {
    final bool success = await _channel.invokeMethod('clearDatabase');
    return success;
  }

  static Future<bool> get registerFinger async {
    final bool success = await _channel.invokeMethod('registerFinger');
    return success;
  }

  static const stream =
      const EventChannel('com.amorenew.alps_finger_scan/finger/status');

  // StreamSubscription _statusSubscription = null;

  // void _enableStausReceiver() {
  //   if (_statusSubscription == null) {
  //     _statusSubscription = stream.receiveBroadcastStream().listen(_updateTimer);
  //   }
  // }

  // void _disableStausReceiver() {
  //   if (_statusSubscription != null) {
  //     _statusSubscription.cancel();
  //     _statusSubscription = null;
  //   }
  // }

  // void _updateTimer(timer) {
    // debugPrint("Timer $timer");
//    setState(() => _timer = timer);
  // }

}
