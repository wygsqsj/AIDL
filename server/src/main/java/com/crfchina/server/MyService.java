package com.crfchina.server;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MyService extends Service {

    private static final String TAG = "AIDL";
    private List<Book> list = new ArrayList<>();

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
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "服务绑定");
        list = new ArrayList<>();
        list.add(new Book(0, "1984"));
        list.add(new Book(1, "红楼梦"));
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "服务开启");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "服务调用");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "服务关闭");
    }
}
