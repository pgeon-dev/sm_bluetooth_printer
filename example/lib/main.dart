import 'dart:io';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:sm_bluetooth_printer/sm_bluetooth_printer.dart';
import 'package:sm_bluetooth_printer_example/receipt.dart';
import 'package:sm_bluetooth_printer_example/test.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
   bool _isGranted = false;

  @override
  void initState() {
    _requestPermission();
    super.initState();
  }

    void _requestPermission() async {
    var cameraPermission = await Permission.camera.request().isGranted;
    var storagePermission = await Permission.storage.request().isGranted;

    if (cameraPermission && storagePermission) {
      if (Platform.isAndroid) {
        var locationPermission = await Permission.location.request().isGranted;
        if (locationPermission) {
          setState(() {
            _isGranted = true;
          });
        }
      } else {
        setState(() {
          _isGranted = true;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: ReceiptPage(),
      ),
    );
  }
}
