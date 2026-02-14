package com.android.surfacecontroltestclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceControlViewHost;
import android.view.SurfaceView;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.surfacecontrolviewhosttest.IRemoteRender;

public class MainActivity extends AppCompatActivity {
    public static final String SERVICE_PKG_NAME = "com.android.surfacecontrolviewhosttest";
    public static final String SERVICE_CLASS_NAME = "com.android.surfacecontrolviewhosttest.SurfaceControlService";

    private ServiceConnection mServiceConnection ;
    private IRemoteRender mRemoteRender;
    private IBinder mBinderToken;
    private SurfaceView mSurfaceView;
    private Context mContext;
    private String TAG = MainActivity.class.getSimpleName();
    private SurfaceControlViewHost.SurfacePackage mSurfacePackage;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            clearBind();
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        connectService(this);
        setUp();
    }
    private void connectService(Context context){
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(SERVICE_PKG_NAME,SERVICE_CLASS_NAME));
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mRemoteRender = IRemoteRender.Stub.asInterface(service);
                Log.d(TAG,"onServiceConnected is called");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG,"onServiceDisconnected is called");
                clearBind();
            }
        };
        boolean result = context.bindService(intent,mServiceConnection,Context.BIND_AUTO_CREATE);
        Log.d(TAG,"result = "+result);
    }
    private void unbindService(Context context) {
        if (mServiceConnection != null) {
            context.unbindService(mServiceConnection);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.R)
    public void onClickDraw(View view){
        mBinderToken = mSurfaceView.getHostToken();
        try {
            mSurfacePackage = mRemoteRender.getSurfacePackage(0,mBinderToken,mSurfaceView.getWidth(),mSurfaceView.getHeight());
            mSurfaceView.setChildSurfacePackage(mSurfacePackage);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.R)
    private void setUp(){
        mSurfaceView = findViewById(R.id.surface_view);
        mSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick is called");
                try {
                    mRemoteRender.onClick();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                try {
                    return mRemoteRender.onTouch(event);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mContext);
    }
    private void clearBind(){
        Log.d(TAG,"clearBind is called");
        if(mRemoteRender != null){
            mRemoteRender.asBinder().unlinkToDeath(mDeathRecipient,0);
            mRemoteRender = null;
        }
    }
}