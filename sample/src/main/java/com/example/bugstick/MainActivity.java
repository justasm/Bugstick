package com.example.bugstick;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.jmedeisis.bugstick.Joystick;
import com.jmedeisis.bugstick.JoystickListener;

public class MainActivity extends AppCompatActivity {

    private static final float MAX_BUG_SPEED_DP_PER_S = 300f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView angleView = (TextView) findViewById(R.id.tv_angle);
        final TextView offsetView = (TextView) findViewById(R.id.tv_offset);

        final BugView bugView = (BugView) findViewById(R.id.bugview);

        final String angleNoneString = getString(R.string.angle_value_none);
        final String angleValueString = getString(R.string.angle_value);
        final String offsetNoneString = getString(R.string.offset_value_none);
        final String offsetValueString = getString(R.string.offset_value);

        Joystick joystick = (Joystick) findViewById(R.id.joystick);
        joystick.setJoystickListener(new JoystickListener() {
            @Override
            public void onDown() {
            }

            @Override
            public void onDrag(float degrees, float offset) {
                angleView.setText(String.format(angleValueString, degrees));
                offsetView.setText(String.format(offsetValueString, offset));

                bugView.setVelocity(
                        (float) Math.cos(degrees * Math.PI / 180f) * offset * MAX_BUG_SPEED_DP_PER_S,
                        -(float) Math.sin(degrees * Math.PI / 180f) * offset * MAX_BUG_SPEED_DP_PER_S);
            }

            @Override
            public void onUp() {
                angleView.setText(angleNoneString);
                offsetView.setText(offsetNoneString);

                bugView.setVelocity(0, 0);
            }
        });
    }
}
