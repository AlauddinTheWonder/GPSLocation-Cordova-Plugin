<!---
 license: Licensed to the Apache Software Foundation (ASF) under one
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
-->

# com.alauddin.cordova.gpslocation

This plugin provides information about the device's location, such as
latitude and longitude using Global Positioning System (GPS).
There is no guarantee that the plugin returns the
device's actual location.

## Cordova Version
    This plugin is tested on cordova version: 3.5.0

## Installation
    cordova plugin add https://github.com/AlauddinTheWonder/GPSLocation-Cordova-Plugin.git

## Supported Platforms
    - Android

## Methods
    - navigator.gpslocation.getLocation

### Parameters
    - __gpsLocationHighAccuracy__: _(Boolean)_ Need high accuracy GPS location or not.
    - __callBackSuccess__: The callback that is passed the current position.
    - __callBackError__: _(Optional)_ The callback that executes if an error occurs.

### Example
    var highAccuracy = true; -- Need High Accuracy (Get Location over GPS each time)
    var timeout = 60 * 1000; -- Timeout time for GPS listener
    var max_age = 120 * 1000;-- Max age for returning same location

    navigator.gpslocation.getLocation(highAccuracy, timeout, max_age, onSuccess, onError);
    
    // onSuccess Callback receives a Position object
    //
    var onSuccess = function(position) {
        alert("Latitude: "+ position.latitude + "\n" +
              "Longitude: "+ position.longitude);
    };

    // onError Callback receives a PositionError object
    //
    function onError(error) {
        alert('message: ' + error);
    }




