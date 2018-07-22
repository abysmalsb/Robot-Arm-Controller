package com.github.abysmalsb.robotarmcontroller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ConnectActivity extends AppCompatActivity {

    public static final String TCP_ADDRESS = "deviceAddress";
    public static final String TCP_PORT = "deviceName";
    public static final String PREFS_NAME = "addressPreferences";

    private EditText address;
    private EditText port;
    private Button buttonConnect;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        address = findViewById(R.id.address);
        port = findViewById(R.id.port);

        prefs = getSharedPreferences(PREFS_NAME, 0);
        address.setText(prefs.getString(TCP_ADDRESS, ""));
        port.setText(prefs.getString(TCP_PORT, ""));

        buttonConnect = findViewById(R.id.connect);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean leave = true;
                if (address.getText().toString().trim().equalsIgnoreCase("")) {
                    address.setError("This field can not be blank");
                    leave = false;
                }
                if (port.getText().toString().trim().equalsIgnoreCase("")) {
                    port.setError("This field can not be blank");
                    leave = false;
                }
                if (leave) {
                    String addressValue = address.getText().toString();
                    String portString = port.getText().toString();
                    int portValue = Integer.parseInt(portString);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(TCP_ADDRESS, addressValue);
                    editor.putString(TCP_PORT, portString);
                    editor.commit();

                    Intent intent = new Intent(ConnectActivity.this, ConfigureActivity.class);
                    intent.putExtra(TCP_ADDRESS, addressValue);
                    intent.putExtra(TCP_PORT, portValue);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });
    }
}
