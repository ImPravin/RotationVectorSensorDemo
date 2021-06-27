// IRemoteServiceCallback.aidl
package com.sample.mylibrary;

import com.sample.mylibrary.DeviceData;

interface IRemoteServiceCallback {
    /**
     * Called when the service has a new value for you.
     */
    void valueChanged(in DeviceData value);

}