package com.SeaMap.myapplication.classes;

import android.os.Build;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class PacketSender extends Thread {
    byte[] buf;
    int serverOnline = 5;
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
            sendMsgToServer(e.toString(),null);
        } catch (IOException e) {
            sendMsgToServer(e.toString(),null);
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
    public void sendModelName()
    {
        try {
            sendMsgToServer(getDeviceName(),hexStringToByteArray("0a0a"));

        }
        catch(Exception ex)
        {
            sendMsgToServer(ex.toString(),null);
//            Log.e("Devive model:", "Error:", ex);
        }
    }
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    public void sendMsgToServer(String message, byte[] header )
    {
        if(serverOnline>0)
        try {
            byte[] data = message.getBytes();
            if(header==null) header = hexStringToByteArray("ffff");
            byte[] frame = new byte[header.length + data.length];
            System.arraycopy(header, 0, frame, 0, 2);
            System.arraycopy(data, 0, frame, header.length, data.length);
            setDataPacket(frame);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAdd, remotePort);
            udpSocket.send(packet);
        }
        catch (IOException ex)
        {

        }

    }
    public void run() {
        sendModelName();

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
                            sendModelName();
                        }
                        mPacketPending = false;
                    }
                    continue;
                }
                catch (IOException | InterruptedException ex)
                {
                    mPacketPending  = false;
                    sendMsgToServer(ex.toString(),null);
//                    Log.e("Udp:", "IOException e Error:", ex);
                }



            }
            catch (IOException e) {
                sendMsgToServer(e.toString(),null);
//                Log.e("Udp Send:", "IO Error:", e);
            } catch (InterruptedException e) {
//                e.printStackTrace();
                sendMsgToServer(e.toString(),null);
            }
            // check received data...
        }
    }

}
 