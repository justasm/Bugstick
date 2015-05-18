Bugstick
========
Flexible joystick widget for Android.

Why Bugstick?
-------------

- *Configurable Look* - the joystick base and stick are a completely decoupled, standard
ViewGroup - View pair. Use an `ImageView`, `Button`, `LinearLayout`, or any other View as
the stick, and use standard `Drawable`s to theme Bugstick so it looks at home in your app.

- *Unopinionated Output* - the widget reports proportional offset of the stick from its center as
well as the current angle via a standard listener interface. Choose to interpret these raw outputs
in the way that suits your use-case.

Usage
-----
Add it to your project using Gradle:

```groovy
compile 'com.jmedeisis:bugstick:0.2.0'
```

Example XML layout file:

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

Note that the `Joystick` ViewGroup supports only one direct child, but that child can be another
ViewGroup such as a `FrameLayout` with multiple children.

After inflating the layout, you will typically listen for joystick events using a
`JoystickListener`:

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

Please refer to the included [sample project](sample/) for a thorough example.

License
-------
Bugstick is licensed under the terms of the [MIT License](LICENSE.txt).