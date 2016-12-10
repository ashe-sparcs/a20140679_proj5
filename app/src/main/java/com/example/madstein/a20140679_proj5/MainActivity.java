package com.example.madstein.a20140679_proj5;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    TextView textResponse;
    EditText editTextAddress, editTextPort;
    Button buttonConnect;
    RadioGroup radioGroupOperation;
    RadioButton radioButtonEncrypt, radioButtonDecrypt;
    Spinner spinnerShift;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextAddress = (EditText)findViewById(R.id.address);
        editTextPort = (EditText)findViewById(R.id.port);
        buttonConnect = (Button)findViewById(R.id.connect);
        textResponse = (TextView)findViewById(R.id.response);
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
                    AlertDialog.Builder operationAlert = new AlertDialog.Builder(MainActivity.this);
                    operationAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
                } else {
                    shiftValue = (byte)Integer.parseInt(shiftString);
                    MyClientTask myClientTask = new MyClientTask(
                            addressString,
                            Integer.parseInt(portString),
                            operationID,
                            shiftValue);
                    myClientTask.execute();
                }
            }
        };

    public class MyClientTask extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        byte operationID, shift;
        String request = "i love you";
        String response = "";
        ByteBuffer buffer;

        MyClientTask(String addr, int port, byte id, byte shiftValue){
            dstAddress = addr;
            dstPort = port;
            operationID = id;
            shift = shiftValue;
        }

        @Override
        protected void onPreExecute() {
            // Make message.
            buffer = ByteBuffer.allocate(8+request.length());
            buffer.put(operationID);
            buffer.put(shift);
            buffer.put(new byte[] {0x00, 0x00});
            buffer.putInt(8+request.length());
            buffer.put(request.getBytes());

            /*
            buffer.flip();
            ByteBuffer dbuf = ByteBuffer.allocate(28);
            while (buffer.hasRemaining()) {
                short num = buffer.getShort(); // 1
                dbuf.putShort(num);
            }
            byte[] bytes = dbuf.array(); // { 0, 1 }
            for (int i = 0; i < bytes.length; i++) {
                Log.i("buffer", String.valueOf(bytes[i]));
            }
    `       */
            Log.i("buffer", new String(buffer.array()));
            Toast.makeText(getApplicationContext(), new String(buffer.array()), Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            SocketAddress socketAddress = new InetSocketAddress(dstAddress, dstPort);
            SocketChannel socket = null;
            byte[] data = new byte[8+request.length()];

            try {
                socket = SocketChannel.open(socketAddress);

                buffer.flip();
                socket.write(buffer);
                buffer.clear();

                while(response.length() < request.length()) {
                    socket.read(buffer);
                    buffer.flip();
                    buffer.get(data);
                    response += new String(data);
                    Log.i("response", response);
                }
    /*
     * notice:
     * inputStream.read() will block if no data return
     */
                /*
                while ((bytesRead = inputStream.read(buffer)) != -1){
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response += byteArrayOutputStream.toString("UTF-8");
                }
                */

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "IOException: " + e.toString();
            }finally{
                if(socket != null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            textResponse.setText(response);
            super.onPostExecute(result);
        }
    }
}
