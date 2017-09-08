package com.crfchina.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.crfchina.server.Book;
import com.crfchina.server.IBookManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button btBinder, btGet;
    private IBookManager manager;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            manager = IBookManager.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            manager = null;
        }

    };

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
                            Log.i("AIDL", book.toString());
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
        super.onDestroy();
        unbindService(conn);
    }
}
