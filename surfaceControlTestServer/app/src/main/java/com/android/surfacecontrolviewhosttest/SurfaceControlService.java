package com.android.surfacecontrolviewhosttest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceControlViewHost;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

import java.util.concurrent.CountDownLatch;


public class SurfaceControlService extends Service {
    private final String TAG = "SurfaceControlService";
    private Handler mHandler ;
    private SurfaceControlViewHost mSurfaceControlViewHost;


    private ImageView mImageView;
    public SurfaceControlService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public IBinder onBind(Intent intent) {
       return  mBinder;
    }
    private IBinder mBinder = new IRemoteRender.Stub() {
        @Override
        public SurfaceControlViewHost.SurfacePackage getSurfacePackage(int displayId, IBinder hostToken, int width, int height) throws RemoteException {
            Log.d(TAG, "getSurfacePackage, displayId=" + displayId + ", hostToken=" + hostToken + ", width=" + width + ", height=" + height);
            final SurfaceControlViewHost.SurfacePackage[] result = new SurfaceControlViewHost.SurfacePackage[1];
            final CountDownLatch latch = new CountDownLatch(1);
            mHandler.post(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.R)
                @Override
                public void run() {
                    //创建 SurfaceControlViewHost
                    Context context = getBaseContext();
                    Display display = context.getSystemService(DisplayManager.class).getDisplay(displayId);
                    mSurfaceControlViewHost = new SurfaceControlViewHost(context, display,hostToken);

                    mImageView = new ImageView(context);
                    mImageView.setLayoutParams(new ViewGroup.LayoutParams(width,height));
                    mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    mImageView.setImageResource(R.drawable.test);
                    // 确保可以接收触摸事件
                    mImageView.setClickable(true);
                    mImageView.setFocusable(true);
                    mImageView.setFocusableInTouchMode(true);
                    mSurfaceControlViewHost.setView(mImageView,width,height);
                    result[0] = mSurfaceControlViewHost.getSurfacePackage();
                    latch.countDown();//每调用一次就减 1
                }
            });
            try{
                latch.await();//等待子进程完成任务，再执行主进程
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            return result[0];
        }

        @Override
        public boolean onTouch(MotionEvent motionEvent) throws RemoteException {
            final CountDownLatch latch = new CountDownLatch(1);
            Log.d(TAG,"onTouch is called.");
            final boolean[] result = new boolean[1];

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    MotionEvent newEvent = MotionEvent.obtain(motionEvent);
                    newEvent.recycle();
                    //mImageView.setImageResource(R.drawable.ic_launcher_background);
                    Log.d(TAG,"onTouch is called.");
                    latch.countDown();
                }
            });
            try{
                latch.await();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public void onClick() throws RemoteException {
            final CountDownLatch latch = new CountDownLatch(1);
            Log.d(TAG,"onTouch is called.");
            final boolean[] result = new boolean[1];

            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    mImageView.setImageResource(R.drawable.ic_launcher_background);
                    Log.d(TAG,"onClick is called.");
                    latch.countDown();
                }
            });
            try{
                latch.await();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mSurfaceControlViewHost != null) {
            mSurfaceControlViewHost.release();
        }
    }
}