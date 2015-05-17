Bugstick
========
Flexible joystick widget for Android.

Why Bugstick?
-------------

- *Configurable Look* - the joystick base and stick are a completely decoupled, standard
`ViewGroup` - `View` pair. Use an `ImageView`, `Button`, `LinearLayout`, or any other `View` as
the stick, and use standard `Drawable`s to theme Bugstick so it looks at home in your app.

- *Unopinionated Output* - the widget reports proportional offset of the stick from its center as
well as the current angle via a standard listener interface. Choose to interpret these raw outputs
in the way that suits your use-case.

Usage
-----

```xml
<com.jmedeisis.bugstick.Joystick
    android:id="@+id/joystick"
    android:layout_width="@dimen/base_size"
    android:layout_height="@dimen/base_size"
    android:background="@drawable/bg_base">

    <!-- You may use any View here. -->
    <Button
        android:layout_width="@dimen/stick_size"
        android:layout_height="@dimen/stick_size"
        android:background="@drawable/bg_stick" />

</com.jmedeisis.bugstick.Joystick>
```

```java
Joystick joystick = (Joystick) findViewById(R.id.joystick);
joystick.setJoystickListener(new JoystickListener() {
    @Override
    public void onDown() {
        // ..
    }

    @Override
    public void onDrag(float degrees, float offset) {
        // ..
    }

    @Override
    public void onUp() {
        // ..
    }
});
```

License
-------
Bugstick is licensed under the terms of the [MIT License](LICENSE.txt).