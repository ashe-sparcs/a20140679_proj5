
/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.madstein.a20140679_proj5;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.view.View.OnClickListener;

/**
 * Example that exercises client side of {@link DocumentsContract}.
 */
public class MainActivity extends Activity {

    //Variables for original application
    TextView textResponse, textFind;
    EditText editTextAddress, editTextPort, editTextOutputFileName;
    Button buttonConnect, buttonFindInput;
    RadioGroup radioGroupOperation;
    RadioButton radioButtonEncrypt, radioButtonDecrypt;
    Spinner spinnerShift;

    // Variables for file chooser
    private static final String TAG = "DocumentsSample";
    private static final int CODE_READ = 42;
    String inputFileString = null;
    String outputFileString = null;
    String request = "abcdeabcdeabcde";
    String requestOriginal = "abcdeabcdeabcde";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_main);
        buttonFindInput = (Button)findViewById(R.id.find);
        buttonFindInput.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, CODE_READ);
            }
        });

        editTextAddress = (EditText)findViewById(R.id.address);
        editTextPort = (EditText)findViewById(R.id.port);
        editTextOutputFileName = (EditText)findViewById(R.id.output);
        buttonConnect = (Button)findViewById(R.id.connect);
        textResponse = (TextView)findViewById(R.id.response);
        textFind = (TextView)findViewById(R.id.find_result);
        radioGroupOperation = (RadioGroup) findViewById(R.id.operation);
        radioButtonEncrypt = (RadioButton) findViewById(R.id.encrypt);
        radioButtonDecrypt = (RadioButton) findViewById(R.id.decrypt);
        spinnerShift = (Spinner) findViewById(R.id.shift);

        buttonConnect.setOnClickListener(buttonConnectOnClickListener);
    }

    View.OnClickListener buttonConnectOnClickListener =
            new View.OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    byte operationID = 2;
                    byte shiftValue;
                    if (radioButtonEncrypt.isChecked()) {
                        operationID = 0;
                    } else if(radioButtonDecrypt.isChecked()) {
                        operationID = 1;
                    }
                    String addressString = editTextAddress.getText().toString();
                    String portString = editTextPort.getText().toString();
                    String shiftString = spinnerShift.getSelectedItem().toString();
                    outputFileString = editTextOutputFileName.getText().toString();
                    if (addressString.matches("") || portString.matches("")) {
                        AlertDialog.Builder dstAlert = new AlertDialog.Builder(MainActivity.this);
                        dstAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        dstAlert.setMessage("Enter address and port");
                        dstAlert.show();
                    } else if (operationID == 2) {
                        AlertDialog.Builder operationAlert =
                                new AlertDialog.Builder(MainActivity.this);
                        operationAlert
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        operationAlert.setMessage("Encryption/Decryption check required");
                        operationAlert.show();
                    } else if (shiftString.matches("Shift")) {
                        AlertDialog.Builder shiftAlert = new AlertDialog.Builder(MainActivity.this);
                        shiftAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        shiftAlert.setMessage("Select shift value");
                        shiftAlert.show();
                    } else if (outputFileString.matches("")) {
                        AlertDialog.Builder outFileAlert = new AlertDialog.Builder(MainActivity.this);
                        outFileAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        outFileAlert.setMessage("Decide output file name");
                        outFileAlert.show();
                    } else {
                        shiftValue = (byte)Integer.parseInt(shiftString);
                        MyClientTask myClientTask = new MyClientTask(
                                addressString,
                                Integer.parseInt(portString),
                                operationID,
                                shiftValue,
                                outputFileString);
                        myClientTask.execute();
                    }
                }
            };

    public int realLength(byte[] data) {
        int size = 0;
        while (size < data.length) {
            if (data[size] == 0) {
                break;
            }
            size++;
        }
        return size;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final ContentResolver cr = getContentResolver();

        clearLog();

        final Uri uri = data != null ? data.getData() : null;
        if (uri != null) {
            /*
            inputFileString = uri.getPath();
            log("inputFilePath=" + inputFileString);
            log("isDocumentUri=" + DocumentsContract.isDocumentUri(this, uri));
            */
        } else {
            log("inputFilePath=NOT FOUND");
            return;
        }

        if (requestCode == CODE_READ) {
            try {
                cr.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException e) {
                log("FAILED TO TAKE PERMISSION", e);
            }
            InputStream is = null;
            try {
                is = cr.openInputStream(uri);
                byte[] requestArr = readFullyNoClose(is);
                log("read length=" + requestArr.length);
                requestOriginal = new String(requestArr);
                request = new String(requestArr);
                Log.i("request", request);
            } catch (Exception e) {
                log("FAILED TO READ", e);
            } finally {
            closeQuietly(is);
            }
        }
    }

    private void clearLog() {
        textFind.setText(null);
    }

    private void log(String msg) {
        log(msg, null);
    }

    private void log(String msg, Throwable t) {
        Log.d(TAG, msg, t);
        textFind.setText(textFind.getText() + "\n" + msg);
    }

    public static byte[] readFullyNoClose(InputStream in) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count;
        while ((count = in.read(buffer)) != -1) {
            bytes.write(buffer, 0, count);
        }
        return bytes.toByteArray();
    }

    public static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    public class MyClientTask extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        byte operationID, shift;
        String response = "";
        ByteBuffer buffer;
        String inputFileName;
        String outputFileName;

        MyClientTask(String addr, int port, byte id, byte shiftValue, String outputFile){
            dstAddress = addr;
            dstPort = port;
            operationID = id;
            shift = shiftValue;
            inputFileName = inputFileString;
            outputFileName = outputFile;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            SocketAddress socketAddress = new InetSocketAddress(dstAddress, dstPort);
            SocketChannel socket = null;
            byte[] data = new byte[8 + request.length()];

            while (response.length() < requestOriginal.length()) {
                request = requestOriginal.substring(response.length());
                try {
                    buffer = ByteBuffer.allocate(8 + request.length());
                    buffer.put(operationID);
                    buffer.put(shift);
                    buffer.put(new byte[]{0x00, 0x00});
                    buffer.putInt(8 + request.length());
                    buffer.put(request.getBytes());
                    Log.i("buffer", "fuck");
                    Log.i("buffer", new String(buffer.array()));
                    socket = SocketChannel.open();
                    socket.connect(socketAddress);

                    buffer.flip();
                    int bytesWritten = socket.write(buffer);
                    Log.i("bytesWritten", String.valueOf(bytesWritten));
                    buffer.clear();
                    Log.i("START", "YES");
                    while (true) {
                        int bytesRead = socket.read(buffer);
                        Log.i("bytesRead", String.valueOf(bytesRead));
                        if (bytesRead == 0 || bytesRead == -1) {
                            break;
                        }
                        buffer.flip();
                        buffer.get(data, 0, 8);
                        buffer.get(data, 0, bytesRead - 8);
                        int realDataLength = realLength(data);
                        response += new String(data, 0, realDataLength);
                        Arrays.fill(data, (byte) 0);
                        Log.i("response", response);
                        Log.i("LOOP", "YES");
                    }
                    Log.i("END", "YES");

                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    response = "UnknownHostException: " + e.toString();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    response = "IOException: " + e.toString();
                } finally {
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
            if (checkExternalStorage()) {
                try {
                    File path = Environment.getExternalStoragePublicDirectory
                            ("");
                    File f = new File(path, outputFileName + ".txt"); // 경로, 파일명
                    //log("outputFilePath=" + f.getAbsolutePath());
                    FileWriter write = new FileWriter(f, false);
                    PrintWriter out = new PrintWriter(write);
                    out.println(response);
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.i("display", "DONE");
            //textResponse.setText(response);
            super.onPostExecute(result);
        }
    }

    boolean checkExternalStorage() {
        String state = Environment.getExternalStorageState();
        // 외부메모리 상태
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // 읽기 쓰기 모두 가능
            return true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
            //읽기전용
            return false;
        } else {
            // 읽기쓰기 모두 안됨
            return false;
        }
    }
}