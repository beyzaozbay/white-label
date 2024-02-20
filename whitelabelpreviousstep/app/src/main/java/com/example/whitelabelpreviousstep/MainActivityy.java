package com.example.whitelabelpreviousstep;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

public class MainActivityy extends AppCompatActivity {

    private static final String FLASK_URL = "http://10.0.2.2:5000/get_data"; // loopback interface, i.e. localhost

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Allow insecure connections for testing purposes
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

        // Initialize the Volley request queue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create a JSON object to send to the Flask server
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("on-prem", "burak23"); // Change the value as needed
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create a JsonObjectRequest to send the JSON object to the Flask server
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, FLASK_URL, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Process the response on the main thread
                        handleResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error response on the main thread
                handleErrorResponse(error);
            }
        });

        // Add the request to the request queue
        queue.add(jsonObjectRequest);
    }

    private void handleResponse(JSONObject response) {
        try {
            if (response.has("on-prem")) {
                String value = response.getString("on-prem");
                // Check the value received from the server and change the app icon, launcher icon, and app name accordingly
                if ("burak23".equals(value)) {
                    changeAppIconAndName(R.mipmap.imga, R.string.launcher_name_one, R.drawable.imga, "Burak 23");
                } else if ("burak24".equals(value)) {
                    changeAppIconAndName(R.mipmap.imgb, R.string.launcher_name_two, R.drawable.imgb, "Burak 24");
                }

            } else {
                Log.e("FLASK_RESPONSE", "Key 'on-prem' not found in JSON data");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleErrorResponse(VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.statusCode == 500) {
            Log.e("SSL_HANDSHAKE_ERROR", "SSL/TLS handshake failed: " + error.getMessage());
        } else {
            Log.e("VOLLEY_ERROR", error.toString());
        }
    }

    private void changeAppIconAndName(int launcherIconId, int appNameId, int homeIconId, String appName) {
        PackageManager packageManager = getApplicationContext().getPackageManager();

        // Set the new launcher icon
        getApplicationInfo().icon = launcherIconId;

        // Set the new app name
        getApplicationInfo().labelRes = appNameId;
        setTitle(getString(appNameId));

        // Set the new task description with the app icon
        BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(homeIconId);
        Bitmap bitmap = bitmapDrawable.getBitmap();
        ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(getString(appNameId), bitmap, Color.TRANSPARENT);
        setTaskDescription(taskDescription);

        // Log a message to indicate that the app icon and name have been changed
        Log.d("FLASK_RESPONSE", "App icon and name changed successfully");
    }
}