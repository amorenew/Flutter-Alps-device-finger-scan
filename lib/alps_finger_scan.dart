import 'dart:async';

import 'package:flutter/services.dart';

class AlpsFingerScan {
  static const MethodChannel _channel =
      const MethodChannel('alps_finger_scan');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
