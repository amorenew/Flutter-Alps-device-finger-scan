package com.amorenew.alps_finger_scan;

import com.amorenew.alps_finger_scan.fingerscan.AlpsFingerPrintHelper;
import com.amorenew.alps_finger_scan.fingerscan.FingerListener;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

/**
 * AlpsFingerScanPlugin
 */
public class AlpsFingerScanPlugin implements MethodCallHandler, EventChannel.StreamHandler, FingerListener {
    private static final String FINGER_STATUS_CHANNEL_NAME =
            "com.amorenew.alps_finger_scan/finger/status";
    private final Registrar registrar;
    private AlpsFingerPrintHelper alpsFingerPrintHelper;
    private Result result;
    private PublishSubject<String> fingerStatus = PublishSubject.create();

    AlpsFingerScanPlugin(Registrar registrar) {
        this.registrar = registrar;
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel methodChannel =
                new MethodChannel(registrar.messenger(), "alps_finger_scan");

        final EventChannel fingerStatusEventChannel =
                new EventChannel(registrar.messenger(), FINGER_STATUS_CHANNEL_NAME);

        final AlpsFingerScanPlugin instance = new AlpsFingerScanPlugin(registrar);

        fingerStatusEventChannel.setStreamHandler(instance);
        methodChannel.setMethodCallHandler(instance);
    }

//    private void initConnection() {
//        alpsFingerPrintHelper.init();
//        result.success(true);
//    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        this.result = result;

        if (alpsFingerPrintHelper == null)
            alpsFingerPrintHelper = new AlpsFingerPrintHelper(registrar.activity(), this);

        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("openConnection")) {
            openConnection();
        } else if (call.method.equals("closeConnection")) {
            closeConnection();
        } else if (call.method.equals("clearDatabase")) {
            clearDatabase();
        } else if (call.method.equals("registerFinger")) {
            registerFinger();
        } else {
            result.notImplemented();
        }
    }

    private void openConnection() {
        alpsFingerPrintHelper.openConnection();
        result.success(true);
    }

    private void registerFinger() {
        alpsFingerPrintHelper.enroll("Miro");
        result.success(true);
    }

    private void closeConnection() {
        alpsFingerPrintHelper.closeConnection();
        result.success(true);
    }

    private void clearDatabase() {
        alpsFingerPrintHelper.clearDatabase();
        result.success(true);
    }

    @Override
    public void onStatusChange(String status) {
        fingerStatus.onNext(status);
    }

    @Override
    public void onListen(Object value, final EventChannel.EventSink eventSink) {
        fingerStatus.subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(String value) {
                eventSink.success(value);
            }

            @Override
            public void onError(Throwable error) {
            }

            @Override
            public void onComplete() {
//                eventSink.endOfStream();
            }
        });
    }

    @Override
    public void onCancel(Object o) {

    }
}
