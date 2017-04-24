package com.cleartv.live;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by Lipengfei on 2017/4/18.
 */

public class Utils {

    private static final String TAG = "Utils";
    private static Gson gson = new Gson();

    public static void showToast(Context context,int messageId) {
        Toast.makeText(context, messageId, Toast.LENGTH_LONG).show();
    }

    public static void showToast(Context context,String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static String getStringformAssets(Context context,String filename){
        try {
            InputStream in = context.getAssets().open(filename);
            //获取文件的字节数
            int length = in.available();
            //创建byte数组
            byte[] buffer = new byte[length];
            //将文件中的数据读到byte数组中
            in.read(buffer);
            return new String(buffer, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> ArrayList<T> getBeanListFromJson(String json, Class<T> clazz) {
        try {
            Log.i(TAG, json);
            Type type = new TypeToken<ArrayList<JsonObject>>()
            {}.getType();
            ArrayList<JsonObject> jsonObjects = gson.fromJson(json, type);
            if(jsonObjects == null || jsonObjects.size()<1)
                return null;

            ArrayList<T> arrayList = new ArrayList<>();
            for (JsonObject jsonObject : jsonObjects){
                arrayList.add(gson.fromJson(jsonObject, clazz));
            }
            return arrayList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T getBeanFromJson(String json,Class<T> cls) {
        try {
            Log.i(TAG, json);
            return gson.fromJson(json, cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getValueByKey(String jsonStr, String key) {
        JSONTokener jsonParser = new JSONTokener(jsonStr);
        try {
            JSONObject jsonObj = (JSONObject) jsonParser.nextValue();
            if(jsonObj.has(key)){
                return jsonObj.getString(key);
            }
        } catch (Exception e) {
            Log.e(TAG, "get value by ker failed: " + jsonStr);
            e.printStackTrace();
        }
        return null;
    }

    public static String getAbsUrlByKey(String jsonStr, String key) {
        JSONTokener jsonParser = new JSONTokener(jsonStr);
        try {
            JSONObject jsonObj = (JSONObject) jsonParser.nextValue();
            return PlayerActivity.mainPagePrefix + jsonObj.getString(key);
        } catch (JSONException e) {
            Log.e(TAG, "get value by ker failed: " + jsonStr);
            e.printStackTrace();
            return null;
        } catch (ClassCastException e) {
            Log.e(TAG, "get value by key failed: " + jsonStr);
            e.printStackTrace();
            return null;
        }
    }

    public static int getIntValueByKey(String jsonStr, String key) {
        JSONTokener jsonParser = new JSONTokener(jsonStr);
        try {
            JSONObject jsonObj = (JSONObject) jsonParser.nextValue();
            return jsonObj.getInt(key);
        } catch (Exception e) {
            Log.e(TAG, "get int value by ker failed: " + jsonStr);
            e.printStackTrace();
            return Integer.MIN_VALUE;
        }
    }

    public static long getLongValueByKey(String jsonStr, String key) {
        JSONTokener jsonParser = new JSONTokener(jsonStr);
        try {
            JSONObject jsonObj = (JSONObject) jsonParser.nextValue();
            return jsonObj.getLong(key);
        } catch (Exception e) {
            Log.e(TAG, "get long value by ker failed: " + jsonStr);
            e.printStackTrace();
            return Long.MIN_VALUE;
        }
    }

    public static Double getDoubleValueByKey(String jsonStr, String key) {
        JSONTokener jsonParser = new JSONTokener(jsonStr);
        try {
            JSONObject jsonObj = (JSONObject) jsonParser.nextValue();
            return jsonObj.getDouble(key);
        } catch (Exception e) {
            Log.e(TAG, "get double value by ker failed: " + jsonStr);
            e.printStackTrace();
            return Double.MIN_VALUE;
        }
    }

    public static boolean checkJsonHasKey(String jsonStr, String key) {
        JSONTokener jsonParser = new JSONTokener(jsonStr);
        try {
            JSONObject jsonObj = (JSONObject) jsonParser.nextValue();
            return jsonObj.has(key);
        } catch (Exception e) {
            Log.e(TAG, "get value by ker failed: " + jsonStr);
            e.printStackTrace();
            return false;
        }
    }

}
