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
    private static StretchMode lastActiveStretch=StretchMode.FOUR_THREE;
    public static final float STRETCH_MIN=0.50f, STRETCH_MAX=2.00f;
    private static float manualStretch=1.25f;
    private NullUiSettings(){}
    public static LayoutMode mode(){return mode;}
    public static float scale(){return scale;}
    public static String language(){return language;}
    public static StretchMode stretchMode(){return stretchMode;}
    public static void setStretchMode(StretchMode value){if(value!=null){stretchMode=value;if(value!=StretchMode.OFF)lastActiveStretch=value;}}
    public static float manualStretch(){return manualStretch;}
    public static void setManualStretch(float value){manualStretch=Math.max(STRETCH_MIN,Math.min(STRETCH_MAX,value));}
    /** Screen Stretch module switch: OFF restores the picture, ON returns to the last chosen preset. */
    public static void setScreenStretchEnabled(boolean on){stretchMode=on?lastActiveStretch:StretchMode.OFF;}
    /** Preset index shown in the GUI: 0 = 4:3, 1 = 16:9, 2 = manual. Works while the module is off too. */
    public static int stretchPresetIndex(){
        StretchMode m=stretchMode==StretchMode.OFF?lastActiveStretch:stretchMode;
        return m==StretchMode.SIXTEEN_NINE?1:(m==StretchMode.MANUAL?2:0);
    }
    public static void selectStretchPreset(int index){
        StretchMode m=index==1?StretchMode.SIXTEEN_NINE:(index==2?StretchMode.MANUAL:StretchMode.FOUR_THREE);
        lastActiveStretch=m;
        if(stretchMode!=StretchMode.OFF)stretchMode=m;
    }
    public static void toggleScreenStretch(){setScreenStretchEnabled(stretchMode==StretchMode.OFF);}
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
            case MANUAL -> screenAspect/manualStretch;
            default -> screenAspect;
        };
        return Math.max(0.40f,Math.min(3.00f,screenAspect/target));
    }
}
