package com.amorenew.alps_finger_scan.fingerscan;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SharedFingers {

    private static SharedFingers instance;
    private HashMap<String, byte[]> fingersList;

    private SharedFingers() {
        fingersList = new HashMap<>();
    }

    public static SharedFingers getInstance() {
        if (instance == null)
            instance = new SharedFingers();
        return instance;
    }

    public HashMap<String, byte[]> getFingerBytes() {
        return fingersList;
    }

    public void addFingerBytes(String username, byte[] fingerBytes) {

        fingersList.put(username, fingerBytes);
    }

    public String getUserName(byte[] fingerBytes) {
        for (Map.Entry<String, byte[]> entry : fingersList.entrySet()) {
            if (Arrays.equals(entry.getValue(), fingerBytes)) {
                return entry.getKey();
            }
        }
        return "";
    }

    public int count() {
        return fingersList.size();
    }
}
