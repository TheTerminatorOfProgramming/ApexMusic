package com.ttop.app.equalizer;

public class Settings {

    public static boolean        isEqualizerEnabled = false;
    public static boolean        isEqualizerReloaded = true;
    public static int[]          seekbarpos          = new int[5];
    public static int            presetPos;
    public static short          reverbPreset        = -1;
    public static short          bassStrength        = -1;
    public static EqualizerModel equalizerModel;
    public static double         ratio               = 1.0;
    public static boolean        isEditing           = false;
}
