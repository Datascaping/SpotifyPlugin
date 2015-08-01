/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/

/**
 * This class contains information about broadcasted meta data
 * @constructor
 */
var cordova = require('cordova'),
    exec = require('cordova/exec');

function handlers() {
  return spotifyPlugin.channels.spotifyPluginStatus.numHandlers;
}



var SpotifyPlugin = function() {
    this.artist = null;
    this.album = null;
    this.track = null;
    this.id = null;
    // Create new event handlers on the window (returns a channel instance)
    this.channels = {
      spotifyPluginstatus:cordova.addWindowEventHandler("spotifyPluginStatus")
    };
    for (var key in this.channels) {
        this.channels[key].onHasSubscribersChange = SpotifyPlugin.onHasSubscribersChange;
    }
};
/**
 * Event handlers for when callbacks get registered for the spotifyPlugin.
 * Keep track of how many handlers we have so we can start and stop the native spotifyPlugin listener
 * appropriately (and hopefully save on spotifyPlugin life!).
 */
SpotifyPlugin.onHasSubscribersChange = function() {
  // If we just registered the first handler, make sure native listener is started.
  if (this.numHandlers === 1 && handlers() === 1) {
      exec(spotifyPlugin._status, spotifyPlugin._error, "SpotifyPlugin", "start", []);
  } else if (handlers() === 0) {
      exec(null, null, "SpotifyPlugin", "stop", []);
  }
};

/**
 * Callback for spotifyPlugin status
 *
 * @param {Object} info            keys: level, isPlugged
 */
SpotifyPlugin.prototype._status = function (info) {

    // hier wordt een json object met de meta data ontcijferd!

    if (info) {
        // Something changed in the meta data. Fire spotifyPluginstatus event
        cordova.fireWindowEvent("spotifyPluginStatus", info);

        spotifyPlugin.artist = info.artist;
        spotifyPlugin.album = info.album;
        spotifyPlugin.track = info.track;
        spotifyPlugin.id = info.id;
    }
};

/**
 * Error callback for spotifyPlugin start
 */
SpotifyPlugin.prototype._error = function(e) {
    console.log("Error initializing SpotifyPlugin: " + e);
};

var spotifyPlugin = new SpotifyPlugin();

module.exports = spotifyPlugin;