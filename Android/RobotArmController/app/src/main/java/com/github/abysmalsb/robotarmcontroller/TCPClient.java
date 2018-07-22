package com.github.abysmalsb.robotarmcontroller;

import android.util.Log;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {

    private String serverMessage;
    public final String address;
    public final int port;
    private OnMessageReceived messageListener = null;
    private boolean mRun = false;

    PrintWriter out;
    BufferedReader in;

    public TCPClient(String address, int port, OnMessageReceived listener) {
        this.address = address;
        this.port = port;
        messageListener = listener;
    }

    public void sendMessage(String message) {
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        }
    }

    public void stopClient() {
        Log.d("TCP Client", "C: Disconnecting");
        mRun = false;
    }

    public void run() {
        mRun = true;

        try {
            InetAddress serverAddr = InetAddress.getByName(address);
            Log.d("TCP Client", "C: Connecting to " + address + ":" + port);
            Socket socket = new Socket(serverAddr, port);

            try {
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                Log.d("TCP Client", "C: Sent.");
                Log.d("TCP Client", "C: Done.");
                //receive the message which the server sends back
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    serverMessage = in.readLine();

                    if (serverMessage != null && messageListener != null) {
                        //call the method messageReceived from MyActivity class
                        messageListener.messageReceived(serverMessage);
                    }
                    serverMessage = null;
                }
                Log.d("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");
            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }
        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}