import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:sm_bluetooth_printer/sm_bluetooth_printer.dart';

void main() {
  const MethodChannel channel = MethodChannel('sm_bluetooth_printer');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });
}
