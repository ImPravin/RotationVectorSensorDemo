// IRemoteService.aidl
package com.sample.mylibrary;

import com.sample.mylibrary.IRemoteServiceCallback;

interface IRemoteService {

    void registerCallback(IRemoteServiceCallback cb);

    void unregisterCallback(IRemoteServiceCallback cb);
}