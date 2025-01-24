package com.nathcat.peoplecat_client_android.networking;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.nathcat.peoplecat_server.Packet;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class which abstracts the handling of passing messages to / from the networker service, and
 * binding to the service.
 * @author Nathan Baines
 */
public class ServiceHandler {
    private class MessageHandler extends Handler {
        private ArrayList<Packet> buffer = new ArrayList<>();

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == -1) {
                JSONObject state;
                try {
                    Bundle b = (Bundle) msg.obj;
                    state = (JSONObject) new JSONParser().parse(b.getString("json"));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                if (onStateReceive != null) {
                    onStateReceive.run((boolean) state.get("authenticated"), (JSONObject) state.get("user"));
                    onStateReceive = null;
                }

                return;
            }
            else if (msg.what == -2) {
                System.out.println("ServiceHandler: Service has replied!");
                return;
            }

            Packet p = NetworkerService.decodePacket(msg);
            buffer.add(p);
            if (p.isFinal) {
                NetworkerService.IRequestCallback c = requestCallbacks.get(p.type);
                Packet[] B = buffer.toArray(new Packet[0]);
                buffer = new ArrayList<>();

                if (c == null) {
                    System.err.println("No application side handler for packet type " + p.type);
                }
                else {
                    Packet[] R = c.handle(B);

                    if (R != null) {
                        for (Packet r : R) {
                            try {
                                serviceMessenger.send(NetworkerService.encodePacket(r));
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    public interface IOnStateReceive {
        void run(boolean authenticated, JSONObject user);
    }

    public interface IOnBind {
        void run();
    }

    private Context context;
    private boolean isBound = false;
    private IOnStateReceive onStateReceive;
    private IOnBind onBind;
    private boolean initialised = false;
    private final Handler messageHandler = new MessageHandler();
    private Messenger serviceMessenger;
    public final Messenger localMessenger = new Messenger(messageHandler);
    private final HashMap<Integer, NetworkerService.IRequestCallback> requestCallbacks = new HashMap<>();

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            System.out.println("ServiceHandler: Bound to service, setting up");
            serviceMessenger = new Messenger(iBinder);
            isBound = true;

            Message m = Message.obtain();
            m.what = -2;
            m.replyTo = localMessenger;
            System.out.println("ServiceHandler: Local messenger is " + localMessenger);
            try {
                serviceMessenger.send(m);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }

            if (onBind != null) onBind.run();

            System.out.println("ServiceHandler: Done!");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            System.out.println("ServiceHandler: Disconnected from service!");
            isBound = false;
            serviceMessenger = null;
        }
    };

    public void init(Context context) {
        if (!initialised) {
            this.context = context;

            //if (!isRunning()) {
                context.startService(new Intent(context, NetworkerService.class));
            //}

            context.bindService(new Intent(context, NetworkerService.class), serviceConnection, Context.BIND_AUTO_CREATE);

            initialised = true;
        }
    }

    public void init(Context context, IOnBind onBind) {
        if (!initialised) {
            this.onBind = onBind;
            init(context);
        }
    }

    /**
     * Register a request handler. Any existing handler will be overwritten.
     * @param type The type of request which should be passed to the handler
     * @param handler The handler function to call when this packet type is received
     */
    public void register(int type, NetworkerService.IRequestCallback handler) {
        requestCallbacks.put(type, handler);
    }

    /**
     * Unregister a request handler.
     * @param type The type of request which is associated with the handler.
     */
    public void unregister(int type) {
        requestCallbacks.remove(type);
    }

    /**
     * Get the current connection state of the networker service
     * @param callback The function to run when the state is received
     */
    public void getState(IOnStateReceive callback) {
        onStateReceive = callback;
        try {
            Message m = Message.obtain();
            m.replyTo = localMessenger;
            m.what = -1;
            serviceMessenger.send(m);
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a sequence of packets to the networker service
     * @param sequence The sequence of packets to send
     */
    private void sendToService(Packet[] sequence) {
        for (Packet p : sequence) {
            try {
                Message m = NetworkerService.encodePacket(p);
                m.replyTo = localMessenger;
                serviceMessenger.send(m);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Send a sequence of packets to the server
     * @param sequence The sequence of packets to send
     * @param callback The callback to execute upon receipt of a response from the server
     */
    public void send(Packet[] sequence, NetworkerService.IRequestCallback callback) {
        requestCallbacks.put(sequence[0].type, callback);
        sendToService(sequence);
    }

    /**
     * Send a sequence of packets to the server. Will use the existing callback to handle the server response
     * (if one is set)
     * @param sequence The sequence of packets to send
     */
    public void send(Packet[] sequence) {
        sendToService(sequence);
    }

    public void waitUntilAuth(IOnStateReceive callback) {
        onStateReceive = callback;

        getState(((authenticated, user) -> {
            if (!authenticated) {
                register(Packet.TYPE_AUTHENTICATE, (Packet[] packets) -> {
                    unregister(Packet.TYPE_AUTHENTICATE);
                    callback.run(true, packets[0].getData());
                    return null;
                });

                register(Packet.TYPE_ERROR, (Packet[] packets) -> {
                    unregister(Packet.TYPE_ERROR);
                    callback.run(false, packets[0].getData());
                    return null;
                });

                Message m = Message.obtain();
                m.what = -3;
                try {
                    serviceMessenger.send(m);

                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                callback.run(authenticated, user);
            }
        }));
    }

    public void onBind(IOnBind f) { this.onBind = f; }

    public boolean isBound() { return isBound; }

    private boolean isRunning() {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (NetworkerService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void close() {
        try {
            if (context != null && isBound()) context.unbindService(serviceConnection);
        }
        catch (IllegalArgumentException e) {
            System.err.println("Tried to unbind from unbound service!");
        }
    }
}
