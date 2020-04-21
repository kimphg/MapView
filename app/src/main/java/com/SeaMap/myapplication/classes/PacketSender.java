package com.SeaMap.myapplication.classes;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.Buffer;

import static androidx.core.content.ContextCompat.getSystemService;

public class PacketSender extends Thread {
    byte[] buf;
    DatagramSocket udpSocket;
    int udpPort;
    int remotePort = 50000;
    byte[] incomeBuffer = new byte[1000];
    boolean mPacketPending = false;
    boolean incomePacketPending = false;
    InetAddress serverAdd;
    DatagramPacket incomePacket;
    public PacketSender() {

        try {
            incomePacket = new DatagramPacket(incomeBuffer, incomeBuffer.length);
             udpSocket = new DatagramSocket();
             udpPort = udpSocket.getPort();
            udpSocket.setSoTimeout(100);
            serverAdd = InetAddress.getByName("27.72.56.161");
        }
        catch (SocketException e) {
            Log.e("Udp:", "Socket Error:", e);
        } catch (IOException e) {
            Log.e("Udp Send:", "IO Error:", e);
        }
    }
    public void setDataPacket(byte[] packet)
    {
        buf = packet;
        mPacketPending = (buf.length>0);
    }

    public byte[] getAnswer() {
        if(incomePacketPending) {
            byte[] output = new byte[incomePacket.getLength()];
            System.arraycopy(incomeBuffer,0,output,0,incomePacket.getLength());
            incomePacketPending = false;
            return  output;
        }
        else return null;
    }
    /** Returns the consumer friendly device name */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        return manufacturer + " " + model;
    }

    public void run() {
        try {
            byte[] deviceName = getDeviceName().getBytes("UTF-8");
            byte[] data = new byte[20];
            System.arraycopy(deviceName,0,data,0,Math.min(20,deviceName.length));
            setDataPacket(data);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAdd, remotePort);
            udpSocket.send(packet);
        }
        catch(Exception ex)
        {
            Log.e("Devive model:", "Error:", ex);
        }
        int serverOnline = 5;
        while(true) {

            try {
                udpSocket.receive(incomePacket);
                if(incomePacket.getLength()>=10) incomePacketPending = true;
                Thread.sleep(1000);
                serverOnline=5;
            } catch (SocketTimeoutException e) {
                //Log.e("UdpListen:", "Timeout:", e);
                try {
                    if(mPacketPending) {
                        if(serverOnline>0) {
                            DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAdd, remotePort);
                            udpSocket.send(packet);
                            serverOnline--;
                        }
                        else
                        {
                            // kiểm tra xem máy chủ có hoạt động không, nếu không hoạt động thì tạm dừng liên lạc với máy chủ trong 2 phút
                            Thread.sleep(120000);
                            serverOnline = 5;
                        }
                        mPacketPending = false;
                    }
                    continue;
                }
                catch (IOException | InterruptedException ex)
                {
                    mPacketPending  = false;
                    Log.e("Udp:", "IOException e Error:", ex);
                }



            }
            catch (IOException e) {
                Log.e("Udp Send:", "IO Error:", e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // check received data...
        }
    }

}
 