package com.nathcat.peoplecat_client_android.networking;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.nathcat.peoplecat_client_android.R;
import com.nathcat.peoplecat_server.Packet;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * Foreground service which manages background and application level networking tasks off the main thread of the application.
 * @author Nathan Baines
 */
public class NetworkerService extends Service {



    /**
     * Callback for handling a sequence of packets from the server, provides application level functionality depending
     * on the type of packet.
     */
    public interface IRequestCallback {
        Packet[] handle(Packet[] packets);
    }

    /**
     * Callback for handling a sequence of packets from the server, will pass the sequence through the appropriate handler.
     */
    protected interface IOnResponseFinish {
        void handle(Messenger replyTo, Packet[] packets);
    }

    public static final URI SERVER_URI;

    static {
        try {
            SERVER_URI = new URI("wss://nathcat.net:1234");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public NotificationChannel serviceChannel;
    public NotificationChannel eventChannel;
    protected Client socket;
    protected HashMap<Integer, IRequestCallback> unboundCallbacks = new HashMap<>();
    protected boolean authenticated = false;
    protected JSONObject user;
    protected Handler messageHandler = new MessageHandler(this);
    protected Messenger messenger = new Messenger(messageHandler);
    protected IOnResponseFinish onResponseFinishWhenBound = (Messenger replyTo, Packet[] p) -> {
        // Authentication packets will need to be handled here as well as on the bound application
        if (p[0].type == Packet.TYPE_AUTHENTICATE) {
            unboundCallbacks.get(Packet.TYPE_AUTHENTICATE).handle(p);
        }

        System.out.println("NetworkerService: Server response bound callback, replyTo is " + replyTo);
        if (replyTo != null) {
            for (Packet r : p) {
                try {
                    replyTo.send(encodePacket(r));
                } catch (RemoteException e) {
                    System.err.println("NetworkerService: An error occurred while sending a packet to the bound process. " + e.getMessage());
                }
            }
        }
        else {
            System.err.println("NetworkerService: There is no specified messenger to reply to!");
        }
    };

    protected IOnResponseFinish onResponseFinishUnbound = (Messenger replyTo, Packet[] p) -> {
        IRequestCallback c = unboundCallbacks.get(p[0].type);
        if (c != null) {
            c.handle(p);
        }
        else {
            System.err.println("No callback for type " + p[0].type + " in the current service state! (Unbound)");
        }
    };

    protected IOnResponseFinish onResponseFinishCurrent = onResponseFinishUnbound;

    /**
     * Encode a packet into a message which may be used with Android messenger / handler IPC.
     * @param p The packet to encode
     * @return The resulting message object
     */
    public static Message encodePacket(Packet p) {
        Message m = Message.obtain();
        m.arg1 = p.isFinal ? 1 : 0;
        m.what = p.type;
        if (p.getData() != null) {
            Bundle b = new Bundle();
            b.putString("json", p.getData().toJSONString());
            m.obj = b;
        }
        return m;
    }

    /**
     * Decode a packet from and Android IPC message
     * @param m The message to decode
     * @return The decoded packet
     */
    public static Packet decodePacket(Message m) {
        try {
            JSONObject d = null;
            if (m.obj != null) {
                d = (JSONObject) new JSONParser().parse(((Bundle) m.obj).getString("json"));
            }

            return Packet.createPacket(m.what, m.arg1 == 1, d);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Attempt to open a new connection to the server
     */
    protected void openConnection() {
        if (socket != null && !socket.isClosed()) socket.close();
        socket = new Client(this, SERVER_URI);
        authenticated = false;
        user = null;
        socket.connect();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("NetworkerService: Bound to application!");
        onResponseFinishCurrent = onResponseFinishWhenBound;
        return messenger.getBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        System.err.println("NetworkerService: Rebind!");
        onResponseFinishCurrent = onResponseFinishWhenBound;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        System.out.println("NetworkerService: Unbound from application!");
        onResponseFinishCurrent = onResponseFinishUnbound;
        return true;
    }

    @RequiresApi(api = 34)
    @Override
    public void onCreate() {
        serviceChannel = new NotificationChannel((String) getText(R.string.service_notif_channel), "PeopleCat Service", NotificationManager.IMPORTANCE_NONE);
        getSystemService(NotificationManager.class).createNotificationChannel(serviceChannel);

        eventChannel = new NotificationChannel((String) getText(R.string.event_notif_channel), "PeopleCat Events", NotificationManager.IMPORTANCE_HIGH);
        getSystemService(NotificationManager.class).createNotificationChannel(eventChannel);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (socket == null || socket.isClosed()) {
            startForeground(1, new NotificationCompat.Builder(this, (String) getText(R.string.service_notif_channel))
                    .setContentTitle("PeopleCat")
                    .setContentText("PeopleCat service is running")
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setSmallIcon(R.drawable.cat_notification)
                    .build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING
            );
            
            openConnection();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        System.out.println("NetworkerService: Service is being destroyed!");
        socket.send(Packet.createClose().getBytes());
        socket.close();
    }
}
