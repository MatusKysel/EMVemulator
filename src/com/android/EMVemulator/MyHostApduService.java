package com.android.EMVemulator;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;


public class MyHostApduService extends HostApduService {


    private String ppse;
    private String card_application;
    private String processing_options;
    private String records;
    private String[] crypto_checksum;

    @Override
    public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
        if (ppse == null)
            getCardData();

        if (apdu[0] == (byte) 0 && apdu[1] == (byte) 0xa4 && apdu[2] == (byte) 0x04 && apdu[3] == (byte) 0x00 && apdu[4] == (byte) 0x0E) {
            Log.i("EMVemulator", "Received: " + fromByte2Hex(apdu));

            return fromHex2Byte(ppse);
        }
        if (apdu[0] == (byte) 0 && apdu[1] == (byte) 0xa4 && apdu[2] == (byte) 0x04 && apdu[3] == (byte) 0x00 && apdu[4] == (byte) 0x07) {
            Log.i("EMVemulator", "Received: " + fromByte2Hex(apdu));

            return fromHex2Byte(card_application);
        }
        if (apdu[0] == (byte) 0x80 && apdu[1] == (byte) 0xa8 && apdu[2] == (byte) 0x00 && apdu[3] == (byte) 0x00 && apdu[4] == (byte) 0x02) {
            Log.i("EMVemulator", "Received: " + fromByte2Hex(apdu));

            return fromHex2Byte(processing_options);
        }
        if (apdu[0] == (byte) 0 && apdu[1] == (byte) 0xb2 && apdu[2] == (byte) 0x01 && apdu[3] == (byte) 0x0c && apdu[4] == (byte) 0x00) {
            Log.i("EMVemulator", "Received: " + fromByte2Hex(apdu));

            return fromHex2Byte(records);
        }

        if (apdu[0] == (byte) 0x80 && apdu[1] == (byte) 0x2a && apdu[2] == (byte) 0x8e && apdu[3] == (byte) 0x80 && apdu[4] == (byte) 0x04) {
            Log.i("EMVemulator", "Received: " + fromByte2Hex(apdu));

            int i = Integer.parseInt(fromByte2Hex(apdu).substring(15, 18));
            Log.i("EMVemulator", "Pozor: " + String.valueOf(i));
            return fromHex2Byte(crypto_checksum[i]);
        } else {
            Log.i("EMVemulator", "else-Received: " + fromByte2Hex(apdu) + ";");

            return fromHex2Byte("6A 82");
        }


    }

    public void getCardData() {

        crypto_checksum = new String[1000];
        File myFile = new File("/storage/sdcard0/Download/card.txt");
        FileInputStream fIn = null;
        try {
            fIn = new FileInputStream(myFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
        try {
            ppse = myReader.readLine();
            Log.i("EMVemulator", "Read: " + ppse);
            card_application = myReader.readLine();
            Log.i("EMVemulator", "Read: " + card_application);
            processing_options = myReader.readLine();
            Log.i("EMVemulator", "Read: " + processing_options);
            records = myReader.readLine();
            Log.i("EMVemulator", "Read: " + records);

            for (int i = 0; i < 1000; i++) {
                crypto_checksum[i] = myReader.readLine();
                Log.i("EMVemulator", "Read: " + crypto_checksum[i]);
            }
            myReader.close();
            fIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static byte[] fromHex2Byte(String digits) {
        digits = digits.replace(" ", "");
        final int bytes = digits.length() / 2;
        byte[] result = new byte[bytes];
        for (int i = 0; i < digits.length(); i += 2) {
            result[i / 2] = (byte) Integer.parseInt(digits.substring(i, i + 2), 16);
        }
        return result;
    }

    static protected String fromByte2Hex(byte[] input) {
        StringBuilder result = new StringBuilder();
        for (Byte inputbyte : input) {
            result.append(String.format("%02X" + " ", inputbyte));
        }
        return result.toString();
    }

    @Override
    public void onDeactivated(int reason) {
        Log.i("EMVemulator", "Deactivated: " + reason);
    }
}