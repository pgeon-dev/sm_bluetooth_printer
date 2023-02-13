import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
// import 'package:esc_pos_bluetooth/esc_pos_bluetooth.dart';
// import 'package:flutter_blue/flutter_blue.dart';
// import 'package:esc_pos_utils/esc_pos_utils.dart';
import 'dart:convert';
import 'dart:io';

import 'package:sm_bluetooth_printer/sm_bluetooth_printer.dart';

class ReceiptPage extends StatefulWidget {
  @override
  _ReceiptPage createState() => _ReceiptPage();
}

class _ReceiptPage extends State<ReceiptPage> {
  bool currencySwitch = true;
  bool orderNumSwitch = true;
  bool dateSwitch = true;
  bool userNameSwitch = true;
  // FlutterBlue flutterBlue = FlutterBlue.instance;

  var bluetoothCharacteristic;
  List<BluetoothDevice>? _deviceList;
  List<BluetoothDevice>? _connectedDevices;
  bool isLoading = false;
  var msg = "Hello,michelle";
  bool showLoading = false;
  bool reloadBluetoothDevice = true;
  bool showPairedDevice = true;

//   PrinterBluetoothManager _printerManager = PrinterBluetoothManager();
//   List _devices = [];

  @override
  void initState() {
    super.initState();
    initPrinter();
  }

  initPrinter() async {
    await SmBluetoothPrinter.initPrinter;
    refreshDevice();
  }

  void refreshDevice() async {
    setState(() {
      isLoading = true;
    });
    _deviceList = await SmBluetoothPrinter.bluetoothDevice;
    setState(() {
      isLoading = false;
    });
  }

  Future connectBluetoothDevice(String deviceAddr) async {
    var result;
    result = await SmBluetoothPrinter.connectBluetooth(deviceAddr);
    if (result is PlatformException) {
      showDialog(
        context: context,
        builder: (BuildContext context) {
          return AlertDialog(
            title: const Text('Error'), // To display the title it is optional
            content: Text(
                result.message ?? ''), // Message which will be pop up on the screen
            // Action widget which will provide the user to acknowledge the choice
            actions: [
              TextButton(
                onPressed: () {
                  Navigator.pop(context);
                }, // function used to perform after pressing the button
                child: const Text('OK'),
              ),
            ],
          );
        },
      );
    }
    print('result connect: $result');
    refreshDevice();
    return result;
  }

  Future disconnectBluetoothDevice(String deviceAddr) async {
    var result;
    try {
      result = await SmBluetoothPrinter.disconnectBluetooth(deviceAddr);
      refreshDevice();
    } on PlatformException catch (e) {
      result = e;
    }
    return result;
  }

  @override
  void dispose() {
    super.dispose();
  }

  Future testPrint({@required String? deviceAddr}) async {
    var result;
    try {
      result = await SmBluetoothPrinter.testPrint(deviceAddr);
    } on PlatformException catch (e) {
      result = e;
    }
    return result;
  }

  @override
  Widget build(BuildContext context) {
    List<BluetoothDevice> connectedDevices =
        _deviceList?.where((device) => device.state == 'Connected').toList() ??
            [];

    print('connectedDevices: ${connectedDevices.length}');

    List<BluetoothDevice> connectableDevices = _deviceList
            ?.where((device) => device.state == 'Disconnected')
            .toList() ??
        [];

    return Scaffold(
        body: SingleChildScrollView(
      child: Column(
        children: [
          Card(
            child: Column(
              children: [
                const Text('Devices Connected'),
                SizedBox(
                  height: 200,
                  child: isLoading
                      ? Center(
                          child: CircularProgressIndicator(
                          color: Colors.blue,
                        ))
                      : ListView.builder(
                          itemCount: connectedDevices.length,
                          itemBuilder: (BuildContext context, int index) {
                            return ListTile(
                              subtitle: Row(
                                children: [
                                  ElevatedButton(
                                      onPressed: () {
                                        testPrint(
                                            deviceAddr: connectedDevices[index]
                                                    .address ??
                                                '');
                                      },
                                      child: const Text('Print')),
                                  const SizedBox(
                                    width: 10,
                                  ),
                                  ElevatedButton(
                                      onPressed: () {
                                        disconnectBluetoothDevice(
                                            connectedDevices[index].address ??
                                                '');
                                      },
                                      child: const Text('Disconnect'))
                                ],
                              ),
                              title: Text(connectedDevices[index].name ?? ''),
                            );
                          }),
                )
              ],
            ),
          ),
          Card(
            child: Column(
              children: [
                const Text('Connectable Devices'),
                Align(
                  alignment: Alignment.topRight,
                  child: IconButton(
                    icon: const Icon(
                      Icons.refresh,
                      color: Colors.black,
                    ),
                    onPressed: () {
                      refreshDevice();
                    },
                  ),
                ),
                SizedBox(
                  height: 200,
                  child: isLoading
                      ? const Center(
                          child: CircularProgressIndicator(
                            color: Colors.blue,
                          ),
                        )
                      : ListView.builder(
                          itemCount: connectableDevices.length,
                          itemBuilder: (BuildContext context, int index) {
                            return ListTile(
                              onTap: () {
                                print('tap: connectBluetoothDevice');
                                connectBluetoothDevice(
                                    connectableDevices[index].address ?? '');
                              },
                              title: Text(connectableDevices[index].name ?? ''),
                              subtitle:
                                  Text(connectableDevices[index].address ?? ''),
                            );
                          }),
                )
              ],
            ),
          )
        ],
      ),
    ));
  }
}
