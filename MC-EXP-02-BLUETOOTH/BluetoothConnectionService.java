package com.example.bluetooth_communication;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;
/**
* Created by User on 12/21/2016.
*/
public class BluetoothConnectionService {
private static final String TAG = "BluetoothConnectionServ";
private static final String appName = "MYAPP";
private static final UUID MY_UUID_INSECURE =
UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
private final BluetoothAdapter mBluetoothAdapter;
Context mContext;
private AcceptThread mInsecureAcceptThread;
private ConnectThread mConnectThread;
private BluetoothDevice mmDevice;
private UUID deviceUUID;
ProgressDialog mProgressDialog;
private ConnectedThread mConnectedThread;
public BluetoothConnectionService(Context context) {
mContext = context;
mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
start();
}
/**
* This thread runs while listening for incoming connections. It behaves * like a server-side client.
It runs until a connection is accepted
* (or until cancelled).
*/
private class AcceptThread extends Thread {
// The local server socket
private final BluetoothServerSocket mmServerSocket;
public AcceptThread(){
BluetoothServerSocket tmp = null;
// Create a new listening server socket
try{
tmp =
mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRe
cord(appName, MY_UUID_INSECURE);
Log.d(TAG, "AcceptThread: Setting up Server using: " +
MY_UUID_INSECURE);
}catch (IOException e){
Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
}
mmServerSocket = tmp;
}
public void run(){
Log.d(TAG, "run: AcceptThread Running.");
BluetoothSocket socket = null;
try{
// This is a blocking call and will only return on a
// successful connection or an exception
Log.d(TAG, "run: RFCOM server socket start.....");
socket = mmServerSocket.accept();
Log.d(TAG, "run: RFCOM server socket accepted connection.");
}catch (IOException e){
Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
}
//talk about this is in the 3rd
if(socket != null){
connected(socket,mmDevice);
}
Log.i(TAG, "END mAcceptThread ");
}
public void cancel() {
Log.d(TAG, "cancel: Canceling AcceptThread.");
try {
mmServerSocket.close();
} catch (IOException e) {
Log.e(TAG, "cancel: Close of AcceptThread
ServerSocket failed. " + e.getMessage() );
}}}
/**
* This thread runs while attempting to make an outgoing connection
* with a device. It runs straight through; the connection either
* succeeds or fails.
*/
private class ConnectThread extends Thread {
private BluetoothSocket mmSocket;
public ConnectThread(BluetoothDevice device, UUID uuid) {
Log.d(TAG, "ConnectThread: started.");
mmDevice = device;
deviceUUID = uuid;
}
public void run(){
BluetoothSocket tmp = null;
Log.i(TAG, "RUN mConnectThread ");
// Get a BluetoothSocket for a connection with the
// given BluetoothDevice
try {
Log.d(TAG, "ConnectThread: Trying to create
InsecureRfcommSocket using UUID: "
+MY_UUID_INSECURE );
tmp =
mmDevice.createRfcommSocketToServiceRecord(devi
ceUUID); } catch (IOException e) {
Log.e(TAG, "ConnectThread: Could not create
InsecureRfcommSocket " + e.getMessage());
}
mmSocket = tmp;
// Always cancel discovery because it will slow down a connection
mBluetoothAdapter.cancelDiscovery();
// Make a connection to the BluetoothSocket
try {
// This is a blocking call and will only return on a
// successful connection or an exception
mmSocket.connect();
Log.d(TAG, "run: ConnectThread connected.");
} catch (IOException e) {
// Close the socket
try {
mmSocket.close();
Log.d(TAG, "run: Closed Socket.");
} catch (IOException e1) {
Log.e(TAG, "mConnectThread: run: Unable to close
connection in socket " + e1.getMessage());
}
Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " +
MY_UUID_INSECURE );
}
//will talk about this in the 3rd video
connected(mmSocket,mmDevice);
}
public void cancel() {
try {
Log.d(TAG, "cancel: Closing Client Socket.");
mmSocket.close();
} catch (IOException e) {
Log.e(TAG, "cancel: close() of mmSocket in
Connectthread failed. " + e.getMessage());
}}}
/**
* Start the chat service. Specifically start AcceptThread to begin a
* session in listening (server) mode. Called by the
Activity onResume() */
public synchronized void start() {
Log.d(TAG, "start");
// Cancel any thread attempting to make a connection
if (mConnectThread != null) {
mConnectThread.cancel();
mConnectThread = null;
}
if (mInsecureAcceptThread == null) {
mInsecureAcceptThread = new AcceptThread();
mInsecureAcceptThread.start();
}}
/**
AcceptThread starts and sits waiting for a connection.
Then ConnectThread starts and attempts to make a connection with
the other devices AcceptThread.
**/
public void startClient(BluetoothDevice device,UUID uuid){
Log.d(TAG, "startClient: Started.");
//initprogress dialog
mProgressDialog =
ProgressDialog.show(mContext,"Connecting Bluetooth"
,"Please Wait...",true);
mConnectThread = new ConnectThread(device, uuid);
mConnectThread.start();
}
/**
Finally the ConnectedThread which is responsible for maintaining the
BTConnection, Sending the data, and
receiving incoming data through input/output streams respectively.
**/
private class ConnectedThread extends Thread {
private final BluetoothSocket mmSocket;
private final InputStream mmInStream;
private final OutputStream mmOutStream;
public ConnectedThread(BluetoothSocket socket) {
Log.d(TAG, "ConnectedThread: Starting.");
mmSocket = socket;
InputStream tmpIn = null;
OutputStream tmpOut = null;
//dismiss the progressdialog when connection is established
try{
mProgressDialog.dismiss();
}catch (NullPointerException e){
e.printStackTrace();
}
try {
tmpIn = mmSocket.getInputStream();
tmpOut = mmSocket.getOutputStream();
} catch (IOException e) {
e.printStackTrace();
}
mmInStream = tmpIn;
mmOutStream = tmpOut;
}
public void run(){
byte[] buffer = new byte[1024]; // buffer store for the stream
int bytes; // bytes returned from read()
// Keep listening to the InputStream until an exception occurs
while (true) {
// Read from the InputStream
try {
bytes = mmInStream.read(buffer);
String incomingMessage = new String(buffer, 0, bytes);
Log.d(TAG, "InputStream: " + incomingMessage);
} catch (IOException e) {
Log.e(TAG, "write: Error reading Input Stream. " +
e.getMessage() ); break;
}}}
//Call this from the main activity to send data to the remote device
public void write(byte[] bytes) {
String text = new String(bytes, Charset.defaultCharset());
Log.d(TAG, "write: Writing to outputstream: " + text);
try {
mmOutStream.write(bytes);
} catch (IOException e) {
Log.e(TAG, "write: Error writing to output stream. " +
e.getMessage() ); }
}
/* Call this from the main activity to shutdown the connection */
public void cancel() {
try {
mmSocket.close();
} catch (IOException e) { }
}}
private void connected(BluetoothSocket mmSocket,
BluetoothDevice mmDevice) { Log.d(TAG, "connected: Starting.");
// Start the thread to manage the connection and
perform transmissions mConnectedThread = new
ConnectedThread(mmSocket);
mConnectedThread.start();
}
/**
* Write to the ConnectedThread in an unsynchronized manner
*
* @param out The bytes to write
* @see ConnectedThread#write(byte[])
*/
public void write(byte[] out) {
// Create temporary object
ConnectedThread r;
// Synchronize a copy of the ConnectedThread
Log.d(TAG, "write: Write Called.");
//perform the write
mConnectedThread.write(out);
}}
DeviceListAdapter.java:
package com.example.bluetooth_communication;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;
public class DeviceListAdapter extends
ArrayAdapter<BluetoothDevice> { private LayoutInflater
mLayoutInflater;
private ArrayList<BluetoothDevice> mDevices;
private int mViewResourceId;
public DeviceListAdapter(Context context, int tvResourceId,
ArrayList<BluetoothDevice> devices){
super(context, tvResourceId,devices);
this.mDevices = devices;
mLayoutInflater = (LayoutInflater)
context.getSystemService(Context.LAYOUT_INFLAT
ER_SERVICE); mViewResourceId = tvResourceId;
}
public View getView(int position, View convertView, ViewGroup parent) { convertView =
mLayoutInflater.inflate(mViewResourceId, null);
BluetoothDevice device = mDevices.get(position);
if (device != null) {
TextView deviceName = (TextView)
convertView.findViewById(R.id.tvDeviceName); TextView
deviceAdress = (TextView)
convertView.findViewById(R.id.tvDeviceAddress);
if (deviceName != null) {
deviceName.setText(device.getName());
}
if (device Address != null) {
deviceAdress.setText(device.getAddress());
}}
return convertView;
}}
device_adapter_view.xml:
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
xmlns:android="http://schemas.android.com/apk/res/android"
android:orientation="vertical"
android:layout_width="match_parent"
android:layout_height="match_parent">
<TextView
android:layout_width="match_parent"
android:layout_height="wrap_content"
android:id="@+id/tvDeviceName"
android:textSize="15sp"/>
<TextView
android:layout_width="match_parent"
android:layout_height="wrap_content"
android:id="@+id/tvDeviceAddress"
android:textSize="15sp"/>
</LinearLayout>
Activity_main.xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout
xmlns:android="http://schemas.android.com/apk/res/android"
xmlns:tools="http://schemas.android.com/tools"
android:id="@+id/activity_main"
android:layout_width="match_parent"
android:layout_height="match_parent">
<Button
android:id="@+id/btnONOFF"
android:layout_width="wrap_content"
android:layout_height="wrap_content"
android:layout_alignParentTop="true"
android:text="ON/OFF" />
<Button
android:text="Enable Discoverable"
android:layout_width="wrap_content"
android:layout_height="wrap_content"
android:id="@+id/btnDiscoverable_on_off"
android:onClick="btnEnableDisable_Discoverable"
android:layout_alignParentTop="true"
android:layout_centerHorizontal="true"/>
<Button
android:layout_width="wrap_content"
android:layout_height="wrap_content"
android:id="@+id/btnFindUnpairedDevices"
android:text="Discover"
android:onClick="btnDiscover"/>
<ListView
android:layout_marginTop="15dp"
android:layout_below="@+id/btnStartConnection"
android:layout_width="match_parent"
android:layout_height="150dp"
android:id="@+id/lvNewDevices"/>
<Button
android:layout_marginTop="10dp"
android:layout_width="wrap_content"
android:layout_height="wrap_content"
android:layout_below="@+id/btnFindUnpairedDevices"
android:id="@+id/btnStartConnection"
android:text="Start Connection"/>
<EditText
android:layout_width="250dp"
android:layout_height="wrap_content"
android:hint="Enter Text Here"
android:layout_below="@+id/lvNewDevices"
android:id="@+id/editText"/>
<Button
android:layout_width="wrap_content"
android:layout_height="wrap_content"
android:text="SEND"
android:id="@+id/btnSend"
android:layout_toRightOf="@+id/editText"
android:layout_below="@+id/lvNewDevices"/>
</RelativeLayout>