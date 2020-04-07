package com.SeaMap.myapplication.classes;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.Buffer;

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
            udpSocket.setSoTimeout(500);
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

    public void run() {

        while(true) {

            try {
                udpSocket.receive(incomePacket);
                if(incomePacket.getLength()>=10)
                incomePacketPending = true;
            } catch (SocketTimeoutException e) {
                // send outcome packet
                try {
                    if(mPacketPending) {
                        DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAdd, remotePort);
                        udpSocket.send(packet);
                        mPacketPending  = false;
                    }
                    continue;
                }
                catch (IOException ex)
                {
                    Log.e("Udp:", "IOException e Error:", ex);
                }



            }
            catch (IOException e) {
                Log.e("Udp Send:", "IO Error:", e);
            }
            // check received data...
        }


/*

        try {
            //int udpPort = 50000;

            //udpSocket.bind(new InetSocketAddress(2291));

            //receive data
            byte[] buffer = new byte[2048];
            while (true) {
                udpSocket
                udpSocket.receive(packet);
                String lText = new String(buffer, 0, packet.getLength());
                packet.setLength(buffer.length);
            }
        } catch (SocketException e) {
            Log.e("Udp:", "Socket Error:", e);
        } catch (IOException e) {
            Log.e("Udp Send:", "IO Error:", e);
        }*/

    }
}
 