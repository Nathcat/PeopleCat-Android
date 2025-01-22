package com.nathcat.peoplecat_client_android.networking;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.nathcat.peoplecat_server.Packet;

import org.json.simple.JSONObject;

import java.util.ArrayList;

/**
 * Class to handle android IPC messages from a bound application.
 * Manages a buffer of packets received through IPC, then sends these packets to the server once the sequence is complete.
 */
public class MessageHandler extends Handler {

    public static final int FROM_SERVER = 1000;
    private ArrayList<Packet> buffer = new ArrayList<>();
    private ArrayList<Packet> serverBuffer = new ArrayList<>();
    private Messenger replyTo;
    private final NetworkerService ns;

    public MessageHandler(NetworkerService ns) {
        this.ns = ns;
    }

    @Override
    public void handleMessage(Message msg) {
        Packet p;

        if (msg.what == -1) {
            // If this is the case, the application is not asking to buffer a packet,
            // it is asking for the current connection state.
            Bundle state = new Bundle();
            JSONObject d = new JSONObject();
            d.put("authenticated", ns.authenticated);
            d.put("user", ns.user);
            state.putString("json", d.toJSONString());
            Message reply = Message.obtain();
            reply.what = -1;
            reply.obj = state;

            try {
                msg.replyTo.send(reply);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }

            return;
        }
        else if (msg.what == -2) {
            // This value means that the bound application is pre-specifying their handler the service should reply to
            System.out.println("NetworkerService: Setting replyTo with " + msg.replyTo);
            replyTo = msg.replyTo;
            Message m = Message.obtain();
            m.what = -2;
            try {
                replyTo.send(m);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        else if (msg.what == -3) {
            // Application is asking to restart the server connection
            if (ns.socket.isOpen()) ns.socket.send(Packet.createClose().getBytes());
            ns.socket.close();
            ns.openConnection();
            return;
        }
        else if (msg.arg2 == FROM_SERVER) {

            p = NetworkerService.decodePacket(msg);
            System.out.println("Handler got packet FROM SERVER: " + (p.getData() == null ? new JSONObject() : p.getData()).toJSONString());

            serverBuffer.add(p);

            if (p.isFinal) {
                Packet[] R = serverBuffer.toArray(new Packet[0]);
                serverBuffer = new ArrayList<>();

                ns.onResponseFinishCurrent.handle(replyTo, R);
            }

            return;
        }

        // Message should follow the same encoding as specified in encodePacket()
        p = NetworkerService.decodePacket(msg);
        if (!p.isFinal) {
            buffer.add(p);
        }
        else {
            System.out.println("Is final packet, sending sequence to server");
            buffer.add(p);
            Packet[] R = buffer.toArray(new Packet[0]);
            buffer = new ArrayList<>();

            if (R != null) {
                for (Packet Rp : R) {
                    ns.socket.send(Rp.getBytes());
                }
            }

            else {
                System.err.println("Specified callback map contains no relevant callbacks for packet sequence of type " + R[0].type);
            }
        }
    }
}