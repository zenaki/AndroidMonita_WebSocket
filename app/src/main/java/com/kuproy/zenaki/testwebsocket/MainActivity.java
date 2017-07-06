package com.kuproy.zenaki.testwebsocket;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    private EditText server_address;
    private Button button_connect;
    private EditText data_viewer;
    private EditText message;
    private Button button_send;
    private TableLayout tb_data;
    private OkHttpClient client;

    private WebSocket ws;
    private static final int NORMAL_CLOSURE_STATUS = 1000;

    private String epochtime[];
    private String nama_tu[];
    private String serial_number[];
    private String titik_ukur[];
    private String value[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        server_address = (EditText) findViewById(R.id.server_address);
        button_connect = (Button) findViewById(R.id.button_connect);
        data_viewer = (EditText) findViewById(R.id.data_viewer);
        message = (EditText) findViewById(R.id.message);
        button_send = (Button) findViewById(R.id.button_send);
        tb_data = (TableLayout) findViewById(R.id.tb_data);

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
            try {
                JSONObject data = new JSONObject(text);
                JSONArray array = data.getJSONArray("monita");

                epochtime = new String[array.length()];
                nama_tu = new String[array.length()];
                serial_number = new String[array.length()];
                titik_ukur = new String[array.length()];
                value = new String[array.length()];

                for (int i = 0; i < array.length(); i++) {
                    try {
                        JSONObject obj = array.getJSONObject(i);

                        epochtime[i] = obj.getString("epochtime").toString();
                        nama_tu[i] = obj.getString("nama_tu").toString();
                        serial_number[i] = obj.getString("serial_number").toString();
                        titik_ukur[i] = obj.getString("titik_ukur").toString();
                        value[i] = obj.getString("value").toString();
                    } catch (JSONException e) {
                        // Oops
                    }
                }

                setTable(array.length());
            } catch (JSONException e) {
                Log.e("TestWebSocket", "unexpected JSON exception", e);
            }
//            Log.d("TestWebSocket",data);
//            Log.d("TestWebSocket",text);
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

    private void setTable(final int ln) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (tb_data.getChildCount() >= ln) {
                    for (int i = 0; i < ln; i++) {
                        TableRow row = (TableRow) tb_data.getChildAt(i+1);

                        try {
                            TextView number_view = (TextView) row.getChildAt(0);
                            number_view.setText(String.valueOf(i+1));
                            TextView epochtime_view = (TextView) row.getChildAt(1);
                            epochtime_view.setText(epochtime[i]);
                            TextView nama_tu_view = (TextView) row.getChildAt(2);
                            nama_tu_view.setText(nama_tu[i]);
                            TextView serial_number_view = (TextView) row.getChildAt(3);
                            serial_number_view.setText(serial_number[i]);
                            TextView titik_ukur_view = (TextView) row.getChildAt(4);
                            titik_ukur_view.setText(titik_ukur[i]);
                            TextView value_view = (TextView) row.getChildAt(5);
                            value_view.setText(value[i]);
                        } catch (Exception e) {
                            TextView number_view = (TextView) row.getChildAt(0);
                            number_view.setText("");
                            TextView epochtime_view = (TextView) row.getChildAt(1);
                            epochtime_view.setText("");
                            TextView nama_tu_view = (TextView) row.getChildAt(2);
                            nama_tu_view.setText("");
                            TextView serial_number_view = (TextView) row.getChildAt(3);
                            serial_number_view.setText("");
                            TextView titik_ukur_view = (TextView) row.getChildAt(4);
                            titik_ukur_view.setText("");
                            TextView value_view = (TextView) row.getChildAt(5);
                            value_view.setText("");
                        }
                    }
                } else {
                    if (tb_data.getChildCount() > 1) {
                        tb_data.removeViews(1, ln);
                    }
                    for (int i = 0; i < ln; i++) {
                        TableRow row = new TableRow(MainActivity.this);
                        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                        row.setLayoutParams(lp);
                        row.setPadding(dpToPx(5), dpToPx(5), dpToPx(100), dpToPx(5));
                        row.setId(i+1);

                        TextView number_view = new TextView(MainActivity.this);
                        number_view.setText(String.valueOf(i+1));
                        TextView epochtime_view = new TextView(MainActivity.this);
                        epochtime_view.setText(epochtime[i]);
                        TextView nama_tu_view = new TextView(MainActivity.this);
                        nama_tu_view.setText(nama_tu[i]);
                        TextView serial_number_view = new TextView(MainActivity.this);
                        serial_number_view.setText(serial_number[i]);
                        TextView titik_ukur_view = new TextView(MainActivity.this);
                        titik_ukur_view.setText(titik_ukur[i]);
                        TextView value_view = new TextView(MainActivity.this);
                        value_view.setText(value[i]);

                        row.addView(number_view);
                        row.addView(epochtime_view);
                        row.addView(nama_tu_view);
                        row.addView(serial_number_view);
                        row.addView(titik_ukur_view);
                        row.addView(value_view);

                        tb_data.addView(row);
                    }
                }
            }
        });
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public int pxToDp(int px) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
