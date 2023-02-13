import 'dart:async';
import 'dart:convert';
import 'package:flutter/services.dart';
import 'package:sm_bluetooth_printer/bluetooth_device.dart';

class SmBluetoothPrinter {
  static const MethodChannel _channel = MethodChannel('sm_bluetooth_printer');

  static Future<bool> get initPrinter async {
    var result = await _channel.invokeMethod('initPrinter');
    return result;
  }

  static Future<List<BluetoothDevice>> get bluetoothDevice async {
    var result = await _channel.invokeMethod('bluetoothDevice');
    List<BluetoothDevice> devices = [];
    if (!result.isEmpty) {
      for (int i = 0; i < result.length; i++) {
        devices.add(BluetoothDevice(
            address: result[i]['address'],
            name: result[i]['name'],
            state: result[i]['state'],
            type: result[i]['type']));
      }
      return devices;
    } else {
      return [];
    }
  }

  static Future<dynamic> connectBluetooth(deviceAddr) async {
    var result;
    try {
      result = await _channel
          .invokeMethod('bluetoothConnect', {"deviceAddr": deviceAddr});
    } on PlatformException catch (e) {
      result = e;
    }
    return result;
  }

  static Future<dynamic> disconnectBluetooth(deviceAddr) async {
    var result;
    try {
      result = await _channel
          .invokeMethod('bluetoothDisconnect', {"deviceAddr": deviceAddr});
    } on PlatformException catch (e) {
      result = e;
    }
    return result;
  }

  static Future<dynamic> testPrint(deviceAddr) async {
    final Map<String, dynamic> params = <String, dynamic>{
      'deviceAddr': deviceAddr,
    };
    var result;

    try {
      result = await _channel.invokeMethod('testPrint', params);
    } on PlatformException catch (e) {
      result = e;
    }
    return result;
  }

  static Future printAWB() async {
    // var filePath = await downloadFile(awbUrl, awb);

    //filePath ? filePath : '/storage/emulated/0/Download/test.pdf',
    final Map<String, dynamic> params = <String, dynamic>{
      'filePath': '/storage/emulated/0/Download/print_awb.pdf'
    };
    var result;
    try {
      result = await _channel.invokeMethod('printAWB', params);
    } on PlatformException catch (e) {
      result = e;
    }
    return result;
  }
}

List<BluetoothDevice> bluetoothDeviceFromJson(String str) =>
    List<BluetoothDevice>.from(
        json.decode(str).map((x) => BluetoothDevice.fromJson(x)));

String bluetoothDeviceToJson(List<BluetoothDevice> data) =>
    json.encode(List<dynamic>.from(data.map((x) => x.toJson())));

class BluetoothDevice {
  BluetoothDevice({
    this.address,
    this.name,
    this.state,
    this.type,
  });

  String? address;
  String? name;
  String? state;
  String? type;

  factory BluetoothDevice.fromJson(Map<String, dynamic> json) =>
      BluetoothDevice(
        address: json["address"],
        name: json["name"],
        state: json["state"],
        type: json["type"],
      );

  Map<String, dynamic> toJson() => {
        "address": address,
        "name": name,
        "state": state,
        "type": type,
      };
}
