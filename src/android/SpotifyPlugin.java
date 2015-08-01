/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at
         http://www.apache.org/licenses/LICENSE-2.0
       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package org.apache.cordova.batterystatus;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class SpotifyReceiver extends CordovaPlugin {

    private static final String LOG_TAG = "SpotifyReceiver";

    BroadcastReceiver receiver;

    private CallbackContext spotifyCallbackContext = null;

    /**
     * Constructor.
     */
    public SpotifyReceiver() {
        this.receiver = null;
    }

    /**
     * Executes the request.
     *
     * @param action        	The action to execute.
     * @param args          	JSONArry of arguments for the plugin.
     * @param callbackContext 	The callback context used when calling back into JavaScript.
     * @return              	True if the action was valid, false if not.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        if (action.equals("start")) {
            if (this.spotifyCallbackContext != null) {
                callbackContext.error( "Spotify Receiver already running.");
                return true;
            }
            this.spotifyCallbackContext = callbackContext;

            // We need to listen to spotify events to update meta data
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.spotify.music.metadatachanged");
            if (this.receiver == null) {
                this.receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        updateMetaData(intent);
                    }
                };
                webView.getContext().registerReceiver(this.receiver, intentFilter);
            }

            // Don't return any result now, since status results will be sent when events come in from broadcast receiver
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
            return true;
        }

        else if (action.equals("stop")) {
            removeSpotifyReceiver();
            this.sendUpdate(new JSONObject(), false); // release status callback in JS side
            this.spotifyCallbackContext = null;
            callbackContext.success();
            return true;
        }

        return false;
    }

    /**
     * Stop battery receiver.
     */
    public void onDestroy() {
        removeSpotifyReceiver();
    }

    /**
     * Stop battery receiver.
     */
    public void onReset() {
        removeSpotifyReceiver();
    }

    /**
     * Stop the battery receiver and set it to null.
     */
    private void removeSpotifyReceiver() {
        if (this.receiver != null) {
            try {
                webView.getContext().unregisterReceiver(this.receiver);
                this.receiver = null;
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error unregistering Spotify receiver: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Creates a JSONObject with the current meta data
     *
     * @param spotifyIntent the current battery information
     * @return a JSONObject containing the battery status information
     */
    private JSONObject getMetaData(Intent spotifyIntent) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("artist", spotifyIntent.getStringExtra("artist"));
            obj.put("album", spotifyIntent.getStringExtra("album"));
            obj.put("track", spotifyIntent.getStringExtra("track"));
            obj.put("id", spotifyIntent.getStringExtra("id"));
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return obj;
    }

    /**
     * Updates the JavaScript side whenever the battery changes
     *
     * @param spotifyIntent the current meta data
     * @return
     */
    private void updateMetaData(Intent spotifyIntent) {
        sendUpdate(this.getMetaData(spotifyIntent), true);
    }

    /**
     * Create a new plugin result and send it back to JavaScript
     *
     * @param connection the network info to set as navigator.connection
     */
    private void sendUpdate(JSONObject info, boolean keepCallback) {
        if (this.spotifyCallbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, info);
            result.setKeepCallback(keepCallback);
            this.spotifyCallbackContext.sendPluginResult(result);
        }
    }
}