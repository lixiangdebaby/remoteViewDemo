// IRemoteRender.aidl
package com.android.surfacecontrolviewhosttest;

// Declare any non-default types here with import statements

interface IRemoteRender {
    //客户端提供surface 、大小
    SurfacePackage getSurfacePackage(int displayId,IBinder hostToken,int width,int height);
    //客户端向服务端传递事件
    boolean onTouch(in MotionEvent motionEvent);
    void onClick();
}