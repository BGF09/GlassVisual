package dev.glassvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

public final class NullUiSettings {
    public enum LayoutMode { FIT, STRETCH, FOUR_THREE }
    private static LayoutMode mode=LayoutMode.FIT;
    private static float scale=1.0f;
    private static String language="ru";
    public static boolean particles=true;
    private NullUiSettings(){}
    public static LayoutMode mode(){return mode;}
    public static float scale(){return scale;}
    public static String language(){return language;}
    public static boolean russian(){return "ru".equals(language);}
    public static void setLanguage(String value){language="en".equalsIgnoreCase(value)?"en":"ru";}
    public static void toggleLanguage(){setLanguage(russian()?"en":"ru");}
    public static void setMode(LayoutMode value){if(value!=null)mode=value;}
    public static void setScale(float value){scale=Math.max(0.75f,Math.min(1.25f,value));}
    public static void adjustScale(float delta){setScale(scale+delta);}
    public static void setScreenRatio(int numerator,int denominator){
        if(numerator<=0||denominator<=0)return;
        MinecraftClient client=MinecraftClient.getInstance(); Window window=client.getWindow();
        if(window.isFullscreen())window.toggleFullscreen();
        int h=Math.max(480,window.getHeight()); int w=Math.max(640,Math.round(h*(numerator/(float)denominator)));
        window.setWindowedSize(w,h);
    }
}
