package dev.glassvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

public final class NullUiSettings {
    public enum LayoutMode { FIT, STRETCH, FOUR_THREE }
    private static LayoutMode mode=LayoutMode.FIT;
    private static float scale=1.0f;
    private static String language="ru";
    public static boolean particles=true;
    public static boolean worldParticles=true;
    public static boolean playerTrail=true;
    public enum StretchMode { OFF, FOUR_THREE, SIXTEEN_NINE, MANUAL }
    private static StretchMode stretchMode=StretchMode.FOUR_THREE;
    private NullUiSettings(){}
    public static LayoutMode mode(){return mode;}
    public static float scale(){return scale;}
    public static String language(){return language;}
    public static StretchMode stretchMode(){return stretchMode;}
    public static void setStretchMode(StretchMode value){if(value!=null)stretchMode=value;}
    public static void toggleScreenStretch(){stretchMode=stretchMode==StretchMode.OFF?StretchMode.FOUR_THREE:StretchMode.OFF;}
    public static boolean russian(){return "ru".equals(language);}
    public static void setLanguage(String value){language="en".equalsIgnoreCase(value)?"en":"ru";}
    public static void toggleLanguage(){setLanguage(russian()?"en":"ru");}
    public static void setMode(LayoutMode value){if(value!=null)mode=value;}
    public static void setScale(float value){scale=Math.max(0.75f,Math.min(1.25f,value));}
    public static void adjustScale(float delta){setScale(scale+delta);}
    public static float stretchFactor(MinecraftClient client){
        if(stretchMode==StretchMode.OFF)return 1.0f;
        Window window=client.getWindow();
        int width=window.getFramebufferWidth(), height=window.getFramebufferHeight();
        if(width<=0||height<=0)return 1.0f;
        float screenAspect=width/(float)height;
        float target=switch(stretchMode){
            case FOUR_THREE -> 4.0f/3.0f;
            case SIXTEEN_NINE -> 16.0f/9.0f;
            case MANUAL -> screenAspect/1.25f;
            default -> screenAspect;
        };
        return Math.max(0.65f,Math.min(1.8f,screenAspect/target));
    }
}
