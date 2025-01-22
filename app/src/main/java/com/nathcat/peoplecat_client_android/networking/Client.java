package com.nathcat.peoplecat_client_android.networking;

import android.Manifest;
import android.app.Notification;
import android.content.pm.PackageManager;
import android.net.Network;
import android.os.Message;
import android.os.RemoteException;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.nathcat.peoplecat_client_android.FileManager;
import com.nathcat.peoplecat_client_android.R;
import com.nathcat.peoplecat_server.Packet;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;

/**
 * Manages a WebSocket connection to the server
 */
public class Client extends WebSocketClient {

    private ArrayList<Packet> buffer = new ArrayList<>();
    private final NetworkerService ns;
    public static final int CONNECT_REATTEMPT_DELAY = 2500;

    public Client(NetworkerService ns, URI uri) {
        super(uri);
        this.ns = ns;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("Client connected to server!");

        // Look for existing auth data to attempt authentication with
        JSONObject userFile = FileManager.getUserData(ns);

        if (userFile == null) {
            System.err.println("No existing auth data to use!");
            userFile = new JSONObject();
        }

        ns.unboundCallbacks.put(Packet.TYPE_AUTHENTICATE, (Packet[] R) -> {
            ns.user = R[0].getData();
            ns.authenticated = true;
            System.out.println("Authenticated with user: " + ns.user);
            return null;
        });

        ns.unboundCallbacks.put(Packet.TYPE_ERROR, (Packet[] R) -> {
            JSONObject err = R[0].getData();
            System.err.println("Error from server! '" + err.get("name") + "', '" + err.get("msg") + "'");
            return null;
        });

        ns.unboundCallbacks.put(Packet.TYPE_CLOSE, (Packet[] R) -> {
            System.out.println("Received close packet!");
            return null;
        });

        ns.unboundCallbacks.put(Packet.TYPE_NOTIFICATION_MESSAGE, (Packet[] packets) -> {
            JSONObject userSearch = new JSONObject();
            userSearch.put("id", ((JSONObject) packets[0].getData().get("message")).get("senderId"));

            ns.unboundCallbacks.put(Packet.TYPE_GET_USER, (Packet[] search) -> {
                Notification n = new NotificationCompat.Builder(ns, (String) ns.getText(R.string.app_name))
                        .setSmallIcon(R.drawable.cat_notification)
                        .setContentTitle("New message")
                        .setContentText(search[0].getData().get("username") + " sent a message")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .build();

                if (ActivityCompat.checkSelfPermission(ns, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    System.err.println("Notification permission not granted!");
                    return null;
                }

                NotificationManagerCompat.from(ns).notify(new Random().nextInt(), n);
                return null;
            });

            try {
                ns.messenger.send(NetworkerService.encodePacket(Packet.createPacket(
                        Packet.TYPE_GET_USER,
                        true,
                        userSearch
                )));
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }

            return null;
        });

        if (userFile != null) {
            try {
                ns.messenger.send(NetworkerService.encodePacket(Packet.createPacket(Packet.TYPE_AUTHENTICATE, true, userFile)));
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onMessage(String s) {}

    @Override
    public void onMessage(ByteBuffer byteBuffer) {
        Packet p;
        try {
            p = new Packet(new ByteArrayInputStream(byteBuffer.array()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        buffer.add(p);
        if (p.isFinal) {
            for (Packet r : buffer) {
                Message m = NetworkerService.encodePacket(r);
                m.arg2 = MessageHandler.FROM_SERVER;
                try {
                    ns.messenger.send(m);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }

            buffer = new ArrayList<>();
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        System.out.println("Client: Client was closed, waiting for reconnect.");

        // Re-attempt connection!
        try {
            Thread.sleep(CONNECT_REATTEMPT_DELAY);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Client: Reconnecting...");

        ns.openConnection();
    }

    @Override
    public void onError(Exception e) {
        System.err.println("Connection error! " + e.getMessage());
        e.printStackTrace();

        try {
            Thread.sleep(CONNECT_REATTEMPT_DELAY);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        ns.openConnection();
    }
}