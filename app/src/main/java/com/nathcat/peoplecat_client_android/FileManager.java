package com.nathcat.peoplecat_client_android;

import android.content.Context;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileManager {
    public static final String USER_DATA_PATH = "user.json";

    public static JSONObject getUserData(Context context) {
        org.json.simple.JSONObject userFile = null;
        try {
            FileInputStream fis = new FileInputStream(new File(context.getDataDir(), USER_DATA_PATH));

            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            return (org.json.simple.JSONObject) new JSONParser().parse(new String(buffer));

        } catch (FileNotFoundException e) {
            return null;

        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeUserData(Context context, String username, String password) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(context.getDataDir(), USER_DATA_PATH));
            JSONObject data = new JSONObject();
            data.put("username", username);
            data.put("password", password);

            fos.write(data.toJSONString().getBytes());
            fos.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
