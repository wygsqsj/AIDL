package com.crfchina.server;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class MyService extends Service {

    private AtomicBoolean mIsServiceDestoryed = new AtomicBoolean(false);
    private CopyOnWriteArrayList<Book> list;
    private CopyOnWriteArrayList<IOnNewBookArrivedListener> mListener =
            new CopyOnWriteArrayList<>();

    private static final String TAG = "AIDL 服务端:";

    private Handler mHandler = new Handler();

    private Binder mBinder = new IBookManager.Stub() {

        @Override
        public List<Book> getBookList() throws RemoteException {
            synchronized (MyService.class) {
                return list;
            }
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            synchronized (MyService.class) {
                if (!list.contains(book)) {
                    list.add(book);
                }
            }
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
            if (!mListener.contains(listener)) {
                mListener.add(listener);
                Log.i(TAG, "注册监听器成功");
            } else {
                Log.i(TAG, "监听器已经注册");
            }
        }

        @Override
        public void unRegisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
            if (mListener.contains(listener)) {
                mListener.remove(listener);
                Log.i(TAG, "移除监听器成功");
            } else {
                Log.i(TAG, "移除监听器时发现未包含该监听器");
            }

        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "服务绑定");
        list = new CopyOnWriteArrayList<>();
        list.add(new Book(0, "1984"));
        list.add(new Book(1, "红楼梦"));
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "服务开启");
        new Thread(new ServiceWorker()).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "服务调用");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBinder = null;
            }
        }, 3000);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "服务关闭");
    }

    private void onNewBookArrived(Book book) throws RemoteException {
        list.add(book);
        Log.i(TAG, "新的书已经添加："+book.toString());
        for (IOnNewBookArrivedListener listener : mListener) {
            listener.onNewBookArrived(book);
        }
    }

    class ServiceWorker implements Runnable {

        @Override
        public void run() {
            while (!mIsServiceDestoryed.get()){
                try{
                    Thread.sleep(5000);
                }catch(Exception e){
                    e.printStackTrace();
                }
                int bookId = list.size() + 1;
                Book book = new Book(bookId, "新书" + bookId);
                try {
                    onNewBookArrived(book);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
