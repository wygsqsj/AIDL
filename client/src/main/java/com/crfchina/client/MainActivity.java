package com.crfchina.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.crfchina.server.Book;
import com.crfchina.server.IBookManager;
import com.crfchina.server.IOnNewBookArrivedListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int MESSAGE_NEW_BOOK_ARRIVED = 1;
    private static final String TAG = "AIDL 客户端：";
    private Button btBinder, btGet;
    private IBookManager manager;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_NEW_BOOK_ARRIVED:
                    Log.i(TAG, "服务端已经添加新书，客户端已收到");
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    //监听器获取到新的数据要切换到UI线程进行操作
    private IOnNewBookArrivedListener mOnNewBookArrivedListener =
            new IOnNewBookArrivedListener.Stub() {
                @Override
                public void onNewBookArrived(Book newBook) throws RemoteException {
                    mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED,newBook).sendToTarget();
                }
            };


    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IBookManager bookManager = IBookManager.Stub.asInterface(service);
            try{
                manager = bookManager;
                List<Book> list = bookManager.getBookList();
                Log.i(TAG, "服务端图书库大小：" + list.size());
                Book newBook = new Book(999, "三体");
                bookManager.addBook(newBook);
                List<Book> newList = bookManager.getBookList();
                Log.i(TAG, "客户端添加三体后，重新获取到的图书库大小：" + newList.size());

                //客户端注册监听器
                bookManager.registerListener(mOnNewBookArrivedListener);
            }catch(Exception e){
                e.printStackTrace();
            }
//            linkToDeath();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            manager = null;
        }

    };

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.i("AIDL", "服务端挂了");

            if (manager == null) {
                Log.i("AIDL", "what?");
                return;
            }
            manager.asBinder().unlinkToDeath(mDeathRecipient, 0);
            manager = null;
            //重新绑定服务
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.crfchina.server", "com.crfchina.server.MyService"));
            startService(intent);
            boolean flag = bindService(intent, conn, Context.BIND_AUTO_CREATE);
            Log.i("AIDL", "重新绑定" + flag);
        }
    };

    private void linkToDeath() {
        if (manager == null) {
            return;
        }
        try {
            manager.asBinder().linkToDeath(mDeathRecipient, 0);
            Log.i("AIDL", "死亡代理设置成功");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btBinder = (Button) findViewById(R.id.button2);
        btGet = (Button) findViewById(R.id.button);
        initListener();
    }

    private void initListener() {
        btBinder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.crfchina.server", "com.crfchina.server.MyService"));
                startService(intent);
                boolean flag = bindService(intent, conn, Context.BIND_AUTO_CREATE);
                Log.i("AIDL", flag + "");
            }
        });
        btGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (manager != null) {
                        List<Book> books = manager.getBookList();
                        for (Book book : books) {
                            Log.i(TAG, "服务端图书库书本"+book.toString());
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (manager != null &&
                manager.asBinder().isBinderAlive()) {
            try{
                Log.i(TAG, "客户端退出了");
                manager.unRegisterListener(mOnNewBookArrivedListener);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        unbindService(conn);
        super.onDestroy();
    }
}
