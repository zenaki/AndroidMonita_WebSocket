package com.kuproy.zenaki.testwebsocket;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MainActivity extends AppCompatActivity {

    private EditText server_address;
    private Button button_connect;
    private EditText data_viewer;
    private EditText message;
    private Button button_send;
    private OkHttpClient client;

    private WebSocket ws;
    private static final int NORMAL_CLOSURE_STATUS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        server_address = (EditText) findViewById(R.id.server_address);
        button_connect = (Button) findViewById(R.id.button_connect);
        data_viewer = (EditText) findViewById(R.id.data_viewer);
        message = (EditText) findViewById(R.id.message);
        button_send = (Button) findViewById(R.id.button_send);

        client = new OkHttpClient();

        button_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (button_connect.getText().toString().equals("Connect")) {
                    connect();
                } else if (button_connect.getText().toString().equals("Disconnect")) {
                    disconnect();
                }
            }
        });

        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(ws, message.getText().toString());
            }
        });
    }

    private void connect() {
//        Request request = new Request.Builder().url("ws://119.18.154.235:1234").build();
        Request request = new Request.Builder().url(server_address.getText().toString()).build();
        EchoWebSocketListener listener = new EchoWebSocketListener();

        ws = client.newWebSocket(request, listener);
        client.dispatcher().executorService().shutdown();

        message.setEnabled(true);
        button_send.setEnabled(true);

        button_connect.setText("Disconnect");
        server_address.setEnabled(false);
    }

    private void disconnect() {
        ws.close(NORMAL_CLOSURE_STATUS, "Goodbye!");

        message.setEnabled(false);
        button_send.setEnabled(false);

        button_connect.setText("Connect");
        server_address.setEnabled(true);
    }

    private void setMessage(final String txt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                data_viewer.setText(txt);
            }
        });
    }

    private void sendMessage(WebSocket webSocket, String text) {
        webSocket.send(text);
    }

    private final class EchoWebSocketListener extends WebSocketListener {

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
//            webSocket.send("id:1");
//            webSocket.send("Hello, this is from Test 4 - WebSocket ....");
//            webSocket.send(ByteString.decodeHex("deadbeef"));
//            webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye!");
            setMessage("CONNECTED");
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            setMessage(text);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            setMessage(bytes.hex());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            setMessage("DISCONNECT");
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            setMessage("Error : " + t.getMessage());
        }
    }
}
