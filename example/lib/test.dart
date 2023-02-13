import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
// import 'package:esc_pos_bluetooth/esc_pos_bluetooth.dart';
// import 'package:flutter_blue/flutter_blue.dart';
// import 'package:esc_pos_utils/esc_pos_utils.dart';
import 'dart:convert';
import 'dart:io';

class Test extends StatefulWidget {
  @override
  _Test createState() => _Test();
}

class _Test extends State<Test> {
  bool currencySwitch = true;
  bool orderNumSwitch = true;
  bool dateSwitch = true;
  bool userNameSwitch = true;
  // FlutterBlue flutterBlue = FlutterBlue.instance;

  var bluetoothCharacteristic;
  var _connectedDevices;
  Future? _deviceList;
  var msg = "Hello,michelle";
  bool showLoading = false;
  bool reloadBluetoothDevice = true;
  bool showPairedDevice = true;
  static const MethodChannel _channel = const MethodChannel('bluetooth');

//   PrinterBluetoothManager _printerManager = PrinterBluetoothManager();
//   List _devices = [];

  @override
  void initState() {
//     FlutterBlue flutterBlue = FlutterBlue.instance;
//     flutterBlue.startScan(timeout: Duration(seconds: 4));

// // Listen to scan results
//     var subscription = flutterBlue.scanResults.listen((results) {
//       // do something with scan results
//       for (ScanResult r in results) {
//         print('${r.device.name} found! rssi: ${r.rssi}');
//       }
//     });

    refreshDevice();
    super.initState();
    // initPrinter();
  }

  initPrinter() {
    // flutterBlue.scanResults.listen((results) {
    //   for (ScanResult r in results) {
    //     if (_devices.length > 0) {
    //       var existDevice = _devices.where((o) => o.id == r.device.id);
    //       if (existDevice.isEmpty) _devices.add(r.device);
    //     } else {
    //       _devices.add(r.device);
    //     }
    //   }
    // });
    //   flutterBlue.state.listen((state) {
    //     if (state == BluetoothState.off) {
    //       showDialog(
    //         context: context,
    //         builder: (BuildContext context) {
    //           return AlertDialog(
    //             insetPadding: EdgeInsets.symmetric(horizontal: 50),
    //             title: Text("Alert"),
    //             content: Builder(builder: (context) {
    //               var height = MediaQuery.of(context).size.height;
    //               var width = MediaQuery.of(context).size.width;
    //               return Container(
    //                 height: height - 650,
    //                 width: width,
    //                 child: Center(
    //                   child: Text(
    //                       "Please make sure to on Bluetooth and Location. Thank you."),
    //                 ),
    //               );
    //             }),
    //             actions: <Widget>[
    //               TextButton(
    //                 onPressed: () => Navigator.of(context).pop(false),
    //                 child: Text('Ok'),
    //               ),
    //             ],
    //           );
    //         },
    //       );
    //     } else if (state == BluetoothState.on) {
    //       setState(() {
    //         showLoading = true;
    //       });
    //       flutterBlue.startScan(timeout: Duration(seconds: 4));
    //       Future.delayed(Duration(milliseconds: 4000)).then((_) {
    //         flutterBlue.stopScan();
    //         setState(() {
    //           showLoading = false;
    //         });
    //       });
    //     }
    //   });
  }

  void refreshDevice() {
    _deviceList = bluetoothDevice();
    setState(() {
      reloadBluetoothDevice = true;
    });
  }

  Future bluetoothDevice() async {
    var result;
    try {
      result = await _channel.invokeMethod('bluetoothDevice');
      if (!result.isEmpty) {
        for (int i = 0; i < result.length; i++) {
          if (result[i]['type'] == '1024')
            result[i]['type'] = "audio_video";
          else if (result[i]['type'] == '256')
            result[i]['type'] = "computer";
          else if (result[i]['type'] == '2304')
            result[i]['type'] = "health";
          else if (result[i]['type'] == '1536')
            result[i]['type'] = "imaging";
          else if (result[i]['type'] == '0')
            result[i]['type'] = "misc";
          else if (result[i]['type'] == '768')
            result[i]['type'] = "networking";
          else if (result[i]['type'] == '1280')
            result[i]['type'] = "peripheral";
          else if (result[i]['type'] == '512')
            result[i]['type'] = "phone";
          else if (result[i]['type'] == '2048')
            result[i]['type'] = "toy";
          else if (result[i]['type'] == '7936')
            result[i]['type'] = "uncategorized";
          else if (result[i]['type'] == '1792') result[i]['type'] = "wearable";
        }
      }
    } on PlatformException catch (e) {
      result = e;
    }

    return Future.value(result);
  }

  Future connectBluetoothDevice(deviceAddr) async {
    var result;
    try {
      result = await _channel
          .invokeMethod('bluetoothConnect', {"deviceAddr": deviceAddr});
    } on PlatformException catch (e) {
      result = e;
    }
    return result;
  }

  Future disconnectBluetoothDevice() async {
    var result;
    try {
      result = await _channel.invokeMethod('bluetoothDisconnect');
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

  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: const Text('Receipt'),
        ),
        body: ListView(children: [
          Card(
              margin: EdgeInsets.only(top: 6, left: 6, right: 6, bottom: 10),
              elevation: 2.0,
              shape: RoundedRectangleBorder(
                side: BorderSide(width: 0.1),
                borderRadius: BorderRadius.circular(10),
              ),
              child: Container(
                  child: Column(
                children: [
                  SizedBox(
                    height: 40,
                    child: ListTile(
                        title: Text(
                      "Printer",
                      style: TextStyle(fontSize: 12, color: Colors.grey),
                    )),
                  ),
                  ListTile(
                    title: Text('Devices Connected',
                        style: TextStyle(fontSize: 14)),
                  ),
                  if (reloadBluetoothDevice == true) refreshBluetoothDevice(1)
                  // ListTile(
                  //   leading: Icon(Icons.print),
                  //   title: Text(_connectedDevices.device.name),
                  //   subtitle: Row(
                  //     mainAxisAlignment: MainAxisAlignment.start,
                  //     children: [
                  //       Padding(
                  //           padding: EdgeInsets.only(right: 10),
                  //           child:
                  //               Text(_connectedDevices.device.id.toString())),
                  //       SizedBox(
                  //         width : 30,
                  //         height: 40,
                  //         child: ElevatedButton(
                  //           textColor: Colors.white,
                  //           color: Colors.blue,
                  //           onPressed: () async {
                  //             // await _connectedDevices.device.disconnect();
                  //             // setState(() {
                  //             //   _connectedDevices = null;
                  //             // });
                  //             // initPrinter();
                  //           },
                  //           child: Text(
                  //             "Disconnect",
                  //             style: TextStyle(fontSize: 11),
                  //           ),
                  //         ),
                  //       ),
                  //       // SizedBox(
                  //       //   width: 50,
                  //       //   child: ElevatedButton(
                  //       //     textColor: Colors.white,
                  //       //     color: Colors.blue,
                  //       //     onPressed: () async {
                  //       //       await printToBT(printStr: "Hello, welcome to flutter");
                  //       //     },
                  //       //     child: Text(
                  //       //       "Print",
                  //       //       style: TextStyle(fontSize: 11),
                  //       //     ),
                  //       //   ),
                  //       // ),
                  //     ],
                  //   ),
                  //   onTap: () {
                  //     // connectPrinterWidget();
                  //   },
                  // ),
                ],
              ))),
          widgetAvailableDevices()
          // printerOptionWidget()
          // connectPrinterWidget()
        ]));
  }

  Widget refreshBluetoothDevice(connectType) {
    return FutureBuilder(
      future: _deviceList, // async work
      builder: (BuildContext context, AsyncSnapshot snapshot) {
        // print(snapshot);
        switch (snapshot.connectionState) {
          case ConnectionState.waiting:
            return Container(
                padding: EdgeInsets.only(bottom: 5),
                child: SizedBox(
                  width: 50,
                  height: 50,
                  child: CircularProgressIndicator(
                    backgroundColor: Colors.white,
                    strokeWidth: 5,
                  ),
                ));
          default:
            if (snapshot.data is PlatformException || snapshot.hasError) {
              PlatformException e = snapshot.data is PlatformException
                  ? snapshot.data
                  : snapshot.error;
              // print("hasError "+snapshot.hasError.toString());
              // print("hasError_data "+e.message.toString());
              return ListTile(
                  title: Text(e.message ?? '',
                      style: TextStyle(
                        fontSize: 13,
                        color: Colors.grey,
                      )));
            } else {
              print("hasData " + snapshot.hasData.toString());
              print("hasData_data " + snapshot.data.toString());
              var result;
              var unconnected =
                  snapshot.data.where((o) => o['state'] == "Disconnected");
              var connected =
                  snapshot.data.where((o) => o['state'] == "Connected");
              if (connectType == 1) // connected device
              {
                if (!connected.isEmpty) {
                  result = displayDeviceList(connected, connectType);
                }
              } else {
                if (!unconnected.isEmpty) {
                  result = displayDeviceList(unconnected, connectType);
                }
              }

              return result != null ? result : ListTile(title: Text(""));
            }
        }
      },
    );
  }

  Widget displayDeviceList(deviceList, connectType) {
    var allItem;
    if (connectType == 1) {
      allItem = deviceList.map<Widget>((o) {
        return Column(children: <Widget>[
          ListTile(
            // leading : o['type'] != '1536' ? Icon(Icons.bluetooth) : Icon(Icons.print),
            title: o['name'] != null ? Text(o['name']) : Text(""),
            subtitle: Row(
              mainAxisAlignment: MainAxisAlignment.start,
              children: [
                Text(o['address']),
                SizedBox(
                  width: 70,
                  child: Padding(
                      padding: EdgeInsets.only(left: 5),
                      child: ElevatedButton(
                        style: ElevatedButton.styleFrom(
                          primary: Color(0xFF13a338),
                        ),
                        // color: Colors.blue,
                        child: Text(
                          "Test",
                          style: TextStyle(color: Colors.white, fontSize: 10),
                        ),
                        onPressed: () async {
                          await testPrint(deviceAddr: o['address']);
                        },
                      )),
                ),
                SizedBox(
                  width: 100,
                  child: Padding(
                      padding: EdgeInsets.only(left: 5),
                      child: ElevatedButton(
                        style: ElevatedButton.styleFrom(
                          primary: Color(0xFF13a338),
                        ),
                        child: Text(
                          "Disconnect",
                          style: TextStyle(color: Colors.white, fontSize: 10),
                        ),
                        onPressed: () async {
                          var disconnect = await disconnectBluetoothDevice();
                          if (disconnect is PlatformException) {
                            PlatformException e = disconnect;
                          } else {
                            refreshDevice();
                          }
                        },
                      )),
                )
              ],
            ),
            onTap: () {},
          )
        ]);
      }).toList();
    } else {
      var countList = 0;
      allItem = deviceList.map<Widget>((o) {
        countList++;
        return Column(children: <Widget>[
          ListTile(
            // leading : o['type'] != '1536' ? Icon(Icons.bluetooth) : Icon(Icons.print),
            title: o['name'] != null ? Text(o['name']) : Text(""),
            subtitle: Text(o['address']),
            onTap: () async {
              var connect = await connectBluetoothDevice(o['address']);
              if (connect is PlatformException) {
                PlatformException e = connect;
              } else {
                refreshDevice();
              }
            },
          ),
          if (countList != deviceList.length)
            Divider(
              thickness: 1,
            )
        ]);
      }).toList();
    }
    return Container(
        child: Column(
      children: allItem,
    ));
  }

  Widget widgetAvailableDevices() {
    return Card(
        margin: EdgeInsets.only(top: 6, left: 6, right: 6, bottom: 10),
        elevation: 2.0,
        shape: RoundedRectangleBorder(
          side: BorderSide(width: 0.1),
          borderRadius: BorderRadius.circular(10),
        ),
        child: Container(
            child: Column(children: [
          ListTile(
            title: Row(
              children: [
                Flexible(
                  flex: 2,
                  child: Align(
                    alignment: Alignment.topLeft,
                    child: Text('Connectable Devices',
                        style: TextStyle(fontSize: 14)),
                  ),
                ),
                if (showLoading == true)
                  Flexible(
                    flex: 1,
                    child: Align(
                        alignment: Alignment.topRight,
                        child: SizedBox(
                          height: 20,
                          width: 20,
                          child: CircularProgressIndicator(
                            strokeWidth: 3,
                          ),
                        )),
                  ),
                Align(
                  alignment: Alignment.topRight,
                  child: IconButton(
                    icon: Icon(
                      Icons.refresh,
                      color: Colors.black,
                    ),
                    onPressed: () {
                      refreshDevice();
                    },
                  ),
                )
              ],
            ),
          ),
          if (reloadBluetoothDevice == true) refreshBluetoothDevice(2)
          // StreamBuilder(
          //     stream: flutterBlue.scanResults,
          //     builder: (BuildContext context, AsyncSnapshot snapshot) {
          //       if (snapshot.connectionState == ConnectionState.waiting) {
          //         return ListTile(title: Text('Please wait its loading...'));
          //       } else {
          //         if (snapshot.hasError)
          //           return ListTile(title: Text('Error: ${snapshot.error}'));
          //         else {
          //   _deviceList = snapshot.data;
          //           return ListView.builder(
          //               physics: const NeverScrollableScrollPhysics(),
          //               scrollDirection: Axis.vertical,
          //               shrinkWrap: true,
          //               itemCount: _deviceList.length,
          //               itemBuilder: (c, i) {
          //                 var checkDeviceExist;
          //                 ScanResult r;
          //                 BluetoothDeviceType b;
          //                 if (i != 0) {
          //                   r = _deviceList[i - 1];
          //                   checkDeviceExist = r.device.id.toString();
          //                   r = _deviceList[i];
          //                 } else
          //                   r = _deviceList[i];
          //                 if (checkDeviceExist == null ||
          //                     checkDeviceExist != r.device.id.toString() ||
          //                     (_connectedDevices != null &&
          //                         _connectedDevices.device.id != r.device.id))
          //                   b = r.device.type;
          //                 return ListTile(
          //                     leading: b == BluetoothDeviceType.unknown
          //                         ? Icon(Icons.bluetooth)
          //                         : Icon(Icons.print),
          //                     title: Text(r.device.name),
          //                     subtitle: Row(
          //                       mainAxisAlignment: MainAxisAlignment.start,
          //                       children: [
          //                         Padding(
          //                             padding: EdgeInsets.only(right: 15),
          //                             child: Text(
          //                               r.device.id.toString(),
          //                             )),
          //                       ],
          //                     ),
          //                     onTap: () async {
          //                       var getConnectedDevice =
          //                           await flutterBlue.connectedDevices;
          //                       var error;

          //                       if (getConnectedDevice.length == 0) {
          //                         try {
          //                           await r.device.connect();
          //                         } catch (e) {
          //                           error = e;
          //                         }
          //                       }

          //                       if (error == null) {
          //                         setState(() {
          //                           _connectedDevices = r;
          //                           _deviceList.remove(r);
          //                         });
          //                       }
          //                     });
          //               });
          //         }
          //       }
          //     }),
        ])));
  }
  // connectPrinterWidget() {
  //   AlertDialog alert = AlertDialog(
  //       title: Text("Printer"),
  //       content: Builder(builder: (context) {
  //         var height = MediaQuery.of(context).size.height;
  //         var width = MediaQuery.of(context).size.width;
  //         return Container(
  //             height: height - 538,
  //             width: width - 100,
  //             child: _devices.isEmpty
  //                 ? Center(child: Text(_devicesMsg ?? ""))
  //                 : ListView.builder(
  //                     itemCount: _devices.length,
  //                     itemBuilder: (c, i) {
  //                       return ListTile(
  //                         leading: Icon(Icons.print),
  //                         title: Text(_devices[i].name),
  //                         subtitle: Row(
  //                           mainAxisAlignment: MainAxisAlignment.spaceEvenly,
  //                           children: [
  //                             Text(
  //                               _devices[i].id.toString(),
  //                             ),
  //                             if(_connectedDevices != null && _devices[i].id.toString() == _connectedDevices)
  //                             SizedBox(
  //                               width: 60,
  //                               height : 40,
  //                               child : ElevatedButton(
  //                                 textColor: Colors.white,
  //                                 color: Colors.blue,
  //                                 onPressed: () async{
  //                                   await _devices[i].disconnect();
  //                                 },
  //                                 child : Text("Disconnect")
  //                               )
  //                             )
  //                           ],
  //                         ),
  //                         onTap: () async {
  //                           await _devices[i].connect();
  //                           setState(() {
  //                             _connectedDevices = _devices[i].id.toString();
  //                           });
  //                           // await _devices[i].disconnect();
  //                           // await _devices[i].connect();
  //                           // List<BluetoothService> services =
  //                           //     await _devices[i].discoverServices();
  //                           // services.forEach((service) {
  //                           //   List<Widget> characteristicsWidget =
  //                           //       new List<Widget>();
  //                           //   List<BluetoothCharacteristic> blueChar =
  //                           //       service.characteristics;
  //                           //   blueChar.forEach((f) {
  //                           //     // print(f);
  //                           //     characteristicsWidget.add(
  //                           //       Align(
  //                           //         alignment: Alignment.centerLeft,
  //                           //         child: Column(
  //                           //           children: <Widget>[
  //                           //             Row(
  //                           //               children: <Widget>[
  //                           //                 Text(f.uuid.toString(),
  //                           //                     style: TextStyle(
  //                           //                         fontWeight:
  //                           //                             FontWeight.bold)),
  //                           //               ],
  //                           //             ),
  //                           //             Row(
  //                           //               children: <Widget>[
  //                           //                 ..._buildReadWriteNotifyButton(f),
  //                           //               ],
  //                           //             ),
  //                           //             Divider(),
  //                           //           ],
  //                           //         ),
  //                           //       ),
  //                           //     );
  //                           //   });
  //                           //   // do something with service
  //                           // });
  //                           // // var passData = await convertRedablePdf();
  //                           // await bluetoothCharacteristic
  //                           //     .write(utf8.encode("Hello"));
  //                           // await _devices[i].disconnect();
  //                         },
  //                       );
  //                     }));
  //       }));

  //   // show the dialog
  //   showDialog(
  //     context: context,
  //     builder: (BuildContext context) {
  //       return alert;
  //     },
  //   );
  // }

  Widget printerOptionWidget() {
    ListTile currency = ListTile(
        title: Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text("Display currency"),
        Switch(
          value: currencySwitch,
          onChanged: (value) {
            setState(() {
              currencySwitch = value;
            });
          },
          activeTrackColor: Colors.lightGreenAccent,
          activeColor: Colors.green,
        ),
      ],
    ));

    ListTile orderNumber = ListTile(
        title: Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text("Display order number"),
        Switch(
          value: orderNumSwitch,
          onChanged: (value) {
            setState(() {
              orderNumSwitch = value;
            });
          },
          activeTrackColor: Colors.lightGreenAccent,
          activeColor: Colors.green,
        ),
      ],
    ));

    ListTile date = ListTile(
        title: Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text("Display Date"),
        Switch(
          value: dateSwitch,
          onChanged: (value) {
            setState(() {
              dateSwitch = value;
            });
          },
          activeTrackColor: Colors.lightGreenAccent,
          activeColor: Colors.green,
        ),
      ],
    ));

    ListTile userName = ListTile(
        title: Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text("Display Date"),
        Switch(
          value: userNameSwitch,
          onChanged: (value) {
            setState(() {
              userNameSwitch = value;
            });
          },
          activeTrackColor: Colors.lightGreenAccent,
          activeColor: Colors.green,
        ),
      ],
    ));

    return Card(
        margin: EdgeInsets.only(top: 6, left: 6, right: 6, bottom: 10),
        elevation: 2.0,
        shape: RoundedRectangleBorder(
          side: BorderSide(width: 0.1),
          borderRadius: BorderRadius.circular(10),
        ),
        child: Container(
            child: Column(
          children: [
            SizedBox(
              height: 40,
              child: ListTile(
                  title: Text(
                "Options",
                style: TextStyle(fontSize: 12, color: Colors.grey),
              )),
            ),
            ListTile(
                title: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text("Header"),
                Icon(Icons.edit, size: 35),
              ],
            )),
            ListTile(
                title: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text("Footer"),
                Icon(Icons.edit, size: 35),
              ],
            )),
            currency,
            orderNumber,
            date,
            userName
          ],
        )));
  }
}
