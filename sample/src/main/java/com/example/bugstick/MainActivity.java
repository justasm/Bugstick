package com.example.bugstick;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.jmedeisis.bugstick.Joystick;
import com.jmedeisis.bugstick.JoystickListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView angleView = (TextView) findViewById(R.id.tv_angle);
        final TextView offsetView = (TextView) findViewById(R.id.tv_offset);

        Joystick joystick = (Joystick) findViewById(R.id.joystick);
        joystick.setJoystickListener(new JoystickListener() {
            @Override
            public void onDown() {

            }

            @Override
            public void onDrag(float degrees, float offset) {
                angleView.setText("Angle: " + degrees);
                offsetView.setText("Offset: " + offset);
            }

            @Override
            public void onUp() {
                angleView.setText("Angle: none");
                offsetView.setText("Offset: none");
            }
        });
    }
}
