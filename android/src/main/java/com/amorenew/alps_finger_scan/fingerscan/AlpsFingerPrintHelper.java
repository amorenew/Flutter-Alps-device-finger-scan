package com.amorenew.alps_finger_scan.fingerscan;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;

import com.za.finger.FingerHelper;
import com.za.finger.IUsbConnState;

import java.util.ArrayList;

import cn.pda.serialport.Tools;

public class AlpsFingerPrintHelper {

    public static final int CHAR_BUFFER_A = 1;
    public static final int CHAR_BUFFER_B = 2;
    public static final int CHAR_BUFFER_MODEL_MERGE = 3;
    private final int IMAGE_SIZE = 256 * 288;//image size
    FingerHelper mFingerHelper;
    private int statues = 0;
    private long startTime = 0L;
    private long endTime = 0L;
    private String tag = "MainActivity";
    private Handler mHandler = new Handler(); //handle thread message
    private String tempImgPath = "/mnt/sdcard/temp.bmp";
    private Bitmap defaultBm;
    private int fpCharBuffer = 0;
    private int templateNum = 0;
    private String tips = "";
    private FingerListener mFingerListener;
    private Activity mActivity;

    //IUsbConnState is to receive usb finger connect state
    private IUsbConnState usbConnectionState = new IUsbConnState() {
        @Override
        public void onUsbConnected() {
            Loger.e(tag, "onUsbConnected()");
            //connect finger device
            statues = mFingerHelper.connectFingerDev();
            if (statues == mFingerHelper.CONNECT_OK) {
                tips = "connect usb finger device success";
//                editTips.setText(tips);
                Loger.e(tag, tips);
//                tvText.append("\n" + tips);
                mFingerListener.onStatusChange(tips);
            } else {
                tips = "connect usb finger device fail";
//                editTips.setText(tips + " , statues = " + statues);
                Loger.e(tag, tips);
//                tvText.append("\n" + tips);
                mFingerListener.onStatusChange(tips);
            }

//            setAllBtnEnable(true, btnOpen, false);
        }

        @Override
        public void onUsbPermissionDenied() {
            Loger.e(tag, "onUsbPermissionDenied()");
            tips = "usb permission denied";
//            editTips.setText(tips);
            Loger.e(tag, tips);
//            tvText.append("\n" + tips);
            mFingerListener.onStatusChange(tips);
        }

        @Override
        public void onDeviceNotFound() {
            Loger.e(tag, "onDeviceNotFound()");
            tips = "device not found";

//            editTips.setText(tips);
            Loger.e(tag, tips);
//            tvText.append("\n" + tips);
            mFingerListener.onStatusChange(tips);
            closeConnection();

        }
    };
    /**
     * match two finger char, if match score > 60 is the same finger
     */
    private Runnable matchFingerTask = new Runnable() {
        @Override
        public void run() {
            String temp = "";
            long timeCount = 0L;
            endTime = System.currentTimeMillis();
            timeCount = endTime - startTime;
            //search finger time 10s
            if (timeCount > 10000) {
                temp = "get finger print time out";
//                editTips.setText(temp);
                mFingerListener.onStatusChange(temp);
////                setAllBtnEnable(true, btnOpen, false);
                return;
            }
            statues = mFingerHelper.getImage();
            //find finger
            if (statues == mFingerHelper.PS_OK) {
                //first finger
                if (fpCharBuffer == mFingerHelper.CHAR_BUFFER_A) {
                    //gen char to bufferA
                    statues = mFingerHelper.genChar(fpCharBuffer);
                    if (statues == mFingerHelper.PS_OK) {
                        byte[] charBytes = getFingerBufferBytes(mFingerHelper.CHAR_BUFFER_A);

                        temp = "gen finger char to BUFFER_A success, please move finger...";
//                        editTips.setText(temp);
                        mFingerListener.onStatusChange(temp);

                        mFingerListener.onStatusChange(Tools.Bytes2HexString(charBytes, 512));


                        byte[] charBytes2 = new byte[512];

                        statues = mFingerHelper.downChar2Buffer(mFingerHelper.CHAR_BUFFER_A, charBytes2, 512);
                        mFingerListener.onStatusChange("_________________________________________");
                        mFingerListener.onStatusChange("_________________________________________");
                        mFingerListener.onStatusChange("_________________________________________");
                        mFingerListener.onStatusChange(Tools.Bytes2HexString(charBytes2, 512));

                        fpCharBuffer = mFingerHelper.CHAR_BUFFER_B;
                        mHandler.postDelayed(matchFingerTask, 2000);
                    }
                } else if (fpCharBuffer == mFingerHelper.CHAR_BUFFER_B) { //second finger
                    //gen char to bufferB
                    statues = mFingerHelper.genChar(fpCharBuffer);
                    if (statues == mFingerHelper.PS_OK) {
                        temp = "gen finger char to BUFFER_B success" + " \r\n";
//                        editTips.setText(temp);
                        mFingerListener.onStatusChange(temp);
                        //match buffer_a with buffer_b
                        int[] iScore = {0, 0};
                        mFingerHelper.match(iScore);
                        temp = "match buffer_A with buffer_B success, result score" + " = " + iScore[0];
//                        editTips.append(temp);
                        mFingerListener.onStatusChange(temp);

                    }
                }
//                setAllBtnEnable(true, btnOpen, false);
            } else if (statues == mFingerHelper.PS_NO_FINGER) {
                temp = "searching finger,please press finger on sense" + " ,time:" + ((10000 - (endTime - startTime))) / 1000 + "s";
//                editTips.setText(temp);
                mFingerListener.onStatusChange(temp);

                mHandler.postDelayed(matchFingerTask, 1000);
            } else if (statues == mFingerHelper.PS_GET_IMG_ERR) {
                temp = "get image error";
//                editTips.setText(temp);
                mFingerListener.onStatusChange(temp);

//                setAllBtnEnable(true, btnOpen, false);
                return;
            } else {
                temp = "device error";
//                editTips.setText(temp);
                mFingerListener.onStatusChange(temp);

//                setAllBtnEnable(true, btnOpen, false);
                return;
            }
        }
    };
    /**
     * match two finger char, if match score > 60 is the same finger
     */

    private Runnable matchFingerUserNameTask = new Runnable() {
        @Override
        public void run() {
            String temp = "";
            long timeCount = 0L;
            endTime = System.currentTimeMillis();
            timeCount = endTime - startTime;
            int usersCount = SharedFingers.getInstance().count();
            int userIndex = 0;

            if (usersCount == 0) {
                temp = "No registered users in shared memory";
//                editTips.setText(temp);
                mFingerListener.onStatusChange(temp);
////                setAllBtnEnable(true, btnOpen, false);
                return;
            }

            //search finger time 10s
            if (timeCount > 10000) {
                temp = "get finger print time out";
//                editTips.setText(temp);
                mFingerListener.onStatusChange(temp);
////                setAllBtnEnable(true, btnOpen, false);
                return;
            }

            if (userIndex >= usersCount) {
                temp = "No more registered users";
//                editTips.setText(temp);
                mFingerListener.onStatusChange(temp);
////                setAllBtnEnable(true, btnOpen, false);
                return;
            }

            byte[] charBytes2 = new byte[512];

            statues = mFingerHelper.downChar2Buffer(mFingerHelper.CHAR_BUFFER_A, charBytes2, 512);

            statues = mFingerHelper.getImage();
            //find finger
            if (statues == mFingerHelper.PS_OK) {
                if (fpCharBuffer == mFingerHelper.CHAR_BUFFER_B) { //second finger
                    //gen char to bufferB
                    statues = mFingerHelper.genChar(fpCharBuffer);
                    if (statues == mFingerHelper.PS_OK) {
                        temp = "gen finger char to BUFFER_B success" + " \r\n";
//                        editTips.setText(temp);
                        mFingerListener.onStatusChange(temp);
                        //match buffer_a with buffer_b
                        int[] iScore = {0, 0};
                        mFingerHelper.match(iScore);
                        temp = "match buffer_A with buffer_B success, result score" + " = " + iScore[0];
//                        editTips.append(temp);
                        mFingerListener.onStatusChange(temp);
                    }
                }
//                setAllBtnEnable(true, btnOpen, false);
            } else if (statues == mFingerHelper.PS_NO_FINGER) {
                temp = "searching finger,please press finger on sense" + " ,time:" + ((10000 - (endTime - startTime))) / 1000 + "s";
//                editTips.setText(temp);
                mFingerListener.onStatusChange(temp);

                mHandler.postDelayed(matchFingerTask, 1000);
            } else if (statues == mFingerHelper.PS_GET_IMG_ERR) {
                temp = "get image error";
//                editTips.setText(temp);
                mFingerListener.onStatusChange(temp);

//                setAllBtnEnable(true, btnOpen, false);
                return;
            } else {
                temp = "device error";
//                editTips.setText(temp);
                mFingerListener.onStatusChange(temp);

//                setAllBtnEnable(true, btnOpen, false);
                return;
            }
        }
    };
    private String currentUserName = "";
    /**
     * enroll finger char to flash database
     */
    private Runnable enrollTask = new Runnable() {
        @Override
        public void run() {
            String temp = "";
            long timeCount = 0L;
            endTime = System.currentTimeMillis();
            timeCount = endTime - startTime;
            //search finger time 10s
            if (timeCount > 10000) {
                temp = "get finger image time out";
//                tvText.append("\n" + temp);
                mFingerListener.onStatusChange(temp);
//                setAllBtnEnable(true, btnOpen, false);
                return;
            }

            statues = mFingerHelper.getImage();
            //find finger
            if (statues == mFingerHelper.PS_OK) {
                //first finger
                if (fpCharBuffer == mFingerHelper.CHAR_BUFFER_A) {
                    //gen char to bufferA
                    statues = mFingerHelper.genChar(fpCharBuffer);
                    if (statues == mFingerHelper.PS_OK) {
                        int[] iMaddr = {0, 0};
                        //is exist flash database,database size = 512
                        statues = mFingerHelper.search(mFingerHelper.CHAR_BUFFER_A, 0, 512, iMaddr);
                        if (statues == mFingerHelper.PS_OK) {
                            temp = "the finger print already exist in flash database " + " , User id index[" + iMaddr[0] + "]";
//                            editTips.setText(temp);
//                            tvText.append("\n" + temp);
                            mFingerListener.onStatusChange(temp);
                            byte[] charBytes = getFingerBufferBytes(mFingerHelper.CHAR_BUFFER_A);

//                            SharedFingers.getInstance().addFingerBytes(userName,charBytes);

//                            mFingerListener.onStatusChange(temp);
//                            mFingerListener.onStatusChange(Tools.Bytes2HexString(charBytes, 512));

                            checkFingerUser(charBytes);
//                            setAllBtnEnable(true, btnOpen, false);
                            return;
                        }
                        temp = "gen finger char to BUFFER_A success, please move finger, press again";
//                        editTips.setText(temp);
//                        tvText.append("\n" + temp);
                        mFingerListener.onStatusChange(temp);

                        fpCharBuffer = mFingerHelper.CHAR_BUFFER_B;
                        mHandler.postDelayed(enrollTask, 2000);
                    }
                } else if (fpCharBuffer == mFingerHelper.CHAR_BUFFER_B) { //second finger
                    //gen char to bufferB
                    statues = mFingerHelper.genChar(fpCharBuffer);
                    if (statues == mFingerHelper.PS_OK) {
                        temp = "gen char" + " \r\n";
//                        editTips.setText(temp);
//                        tvText.append("\n" + temp);
                        mFingerListener.onStatusChange(temp);

                        //merge BUFFER_A with BUFFER_B , gen template to MODULE_BUFFER
                        mFingerHelper.regTemplate();
                        int[] iMbNum = {0, 0};
                        mFingerHelper.getTemplateNum(iMbNum);
                        templateNum = iMbNum[0];
                        if (templateNum >= 512) {
                            temp = "flash database is full" + " \r\n";
//                            editTips.setText(temp);
//                            tvText.append("\n" + temp);
                            mFingerListener.onStatusChange(temp);

//                            setAllBtnEnable(true, btnOpen, false);
                            return;
                        }
                        //store template to flash database
                        statues = mFingerHelper.storeTemplate(mFingerHelper.MODEL_BUFFER, templateNum);
                        if (statues == mFingerHelper.PS_OK) {
                            temp = "enroll success" + ", User id index[" + templateNum + "] \r\n";
                            mFingerListener.onStatusChange(temp);
//                            editTips.setText(temp);
//                            tvText.append("\n" + temp);
                            byte[] charBytes = getFingerBufferBytes(mFingerHelper.MODEL_BUFFER);
                            temp = "addFingerBytes for user: " + currentUserName;

                            SharedFingers.getInstance().addFingerBytes(currentUserName, charBytes);
                            mFingerListener.onStatusChange(temp);
                            mFingerListener.onStatusChange(Tools.Bytes2HexString(charBytes, 512));
                        } else {
                            temp = "enroll fail" + ",statues= " + statues + " \r\n";
//                            editTips.setText(temp);
//                            tvText.append("\n" + temp);
                            mFingerListener.onStatusChange(temp);
                        }

                    }
//                    setAllBtnEnable(true, btnOpen, false);
                }

            } else if (statues == mFingerHelper.PS_NO_FINGER) {
                temp = "searching finger,please press finger on sense" + " ,time:" + ((10000 - (endTime - startTime))) / 1000 + "s";
//                editTips.setText(temp);
//                tvText.append("\n" + temp);
                mFingerListener.onStatusChange(temp);

                mHandler.postDelayed(enrollTask, 1000);
            } else if (statues == mFingerHelper.PS_GET_IMG_ERR) {
                temp = "get image error";
//                editTips.setText(temp);
//                tvText.append("\n" + temp);
                mFingerListener.onStatusChange(temp);
//                setAllBtnEnable(true, btnOpen, false);
                return;
            } else {
                temp = "device error";
//                editTips.setText(temp);
//                tvText.append("\n" + temp);
                mFingerListener.onStatusChange(temp);
//                setAllBtnEnable(true, btnOpen, false);
                return;
            }
        }
    };

    public AlpsFingerPrintHelper(Activity mActivity, FingerListener mFingerListener) {
        this.mActivity = mActivity;
        this.mFingerListener = mFingerListener;
        this.mFingerListener.onStatusChange("Alps Finger Print Helper created");
    }

    private byte[] getFingerBufferBytes(int CHAR_BUFFER_ID) {
        int[] iCharLen = {0, 0};
        byte[] charBytes = new byte[512];
        //upload char
        mFingerHelper.upCharFromBufferID(CHAR_BUFFER_ID, charBytes, iCharLen);
        return charBytes;
    }

    private void checkFingerUser(byte[] fingerCharBytes) {
        mFingerListener.onStatusChange("");
        String temp = "check Finger User";
//        this.currentUserName = userName;
        mFingerListener.onStatusChange(temp);
//        mFingerListener.onStatusChange(this.currentUserName);
        mFingerListener.onStatusChange(Tools.Bytes2HexString(fingerCharBytes, 512));

        mFingerListener.onStatusChange("start matching finger user name");

        startTime = System.currentTimeMillis();
        endTime = startTime;
        ArrayList<byte[]> fingerBytesList = new ArrayList<>(SharedFingers.getInstance().getFingerBytes().values());

        statues = mFingerHelper.downChar2Buffer(mFingerHelper.CHAR_BUFFER_A, fingerCharBytes, 512);
        for (int i = 0; i < fingerBytesList.size(); i++) {
            byte[] fingerBytes = fingerBytesList.get(i);
            statues = mFingerHelper.downChar2Buffer(mFingerHelper.CHAR_BUFFER_B, fingerBytesList.get(i), 512);
            int[] iScore = {0, 0};
            mFingerHelper.match(iScore);
            temp = "match buffer_A with buffer_B success, result score" + " = " + iScore[0];
            mFingerListener.onStatusChange(temp);
            if (iScore[0] > 0) {
                mFingerListener.onStatusChange("User Name: " + SharedFingers.getInstance().getUserName(fingerBytes));
                return;
            }
        }
    }

//    public void init() {
//        Loger.e(tag, "Check if this device is alps ax6737_65_n");
//        mFingerHelper = new FingerHelper(mActivity, usbConnectionState);
//        mFingerListener.onStatusChange("open finger connection");
//
//        Loger.e(tag, "It is device alps ax6737_65_n");
//        Loger.e(tag, "open finger connection");
//    }

    public void openConnection() {
        if (mFingerHelper != null) {
            mFingerHelper.init();
        } else {
            Loger.e(tag, "Check if this device is alps ax6737_65_n");
            mFingerHelper = new FingerHelper(mActivity, usbConnectionState);
            mFingerHelper.init();
            Loger.e(tag, "It is device alps ax6737_65_n");
            Loger.e(tag, "open finger connection");
        }
        mFingerListener.onStatusChange("open finger connection");
    }

    public void closeConnection() {
        mFingerHelper.close();
//                      tvText.append("\n" + "close finger connection");
        mFingerListener.onStatusChange("close finger connection");
        Loger.e(tag, "close finger connection");
    }

    public void enroll(String userName) {
        startTime = System.currentTimeMillis();
        endTime = startTime;
        fpCharBuffer = mFingerHelper.CHAR_BUFFER_A;
        //run match finger char task
        this.currentUserName = userName;
        mHandler.postDelayed(enrollTask, 0);
    }

    public boolean clearDatabase() {
        statues = mFingerHelper.emptyChar();
        mFingerListener.onStatusChange("clear database");

        if (statues == mFingerHelper.PS_OK) {
            mFingerListener.onStatusChange("clear finger database success");
            return true;
        }
        mFingerListener.onStatusChange("clear finger database failed");
        return false;
    }

    public void scanFinger() {
        mFingerListener.onStatusChange("start scanning finger");

        startTime = System.currentTimeMillis();
        endTime = startTime;

        fpCharBuffer = mFingerHelper.CHAR_BUFFER_A;
        //run match finger char task
        mHandler.postDelayed(matchFingerTask, 0);
    }

    public void scanFinger2() {
        mFingerListener.onStatusChange("start scanning finger");

        startTime = System.currentTimeMillis();
        endTime = startTime;

        fpCharBuffer = mFingerHelper.CHAR_BUFFER_A;
        //run match finger char task
        mHandler.postDelayed(matchFingerTask, 0);
    }
//    public void scanFinger() {
//        mFingerListener.onStatusChange("start scanning finger");
//
//        startTime = System.currentTimeMillis();
//        endTime = startTime;
//
//        fpCharBuffer = mFingerHelper.CHAR_BUFFER_A;
//        //run match finger char task
//        mHandler.postDelayed(matchFingerTask, 0);
//    }
}
