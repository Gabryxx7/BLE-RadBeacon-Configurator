package com.unimelb.marinig.bletracker.Utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ViewUtils {

    public static void changeAllViewsTextColor(ViewGroup vg, int color, String[] excludeTags) {
        changeAllViewsTextColor(vg, color, Arrays.asList(excludeTags));
    }
    public static void changeAllViewsTextColor(ViewGroup vg, int color, List<String> excludeTags){
        for (int count=0; count < vg.getChildCount(); count++){
            View view = vg.getChildAt(count);
            //In case there are objects that should not change color
            if(excludeTags != null && excludeTags.size() > 0 && excludeTags.contains(view.getTag())){
                continue;
            } else if(view instanceof Button){
                continue; //A Button can be casted to a TextView so it should be checked first
            } else if(view instanceof ImageView){
                ((ImageView)view).getDrawable().mutate().setTint(color); //Due to RecyclerView Caching, without the "mutate()" ALL THE DRAWABLES in the list will be updated
            } else if(view instanceof TextView){
                ((TextView)view).setTextColor(color);
            } else if(view instanceof ViewGroup){
                changeAllViewsTextColor((ViewGroup)view, color, excludeTags);
            }
        }
    }

    public static void changeAllViewsTextColor(ViewGroup vg, int color){
        changeAllViewsTextColor(vg, color, new ArrayList<>());
    }

    public static void setAllViewsEnabled(boolean isEnabled, ViewGroup vg, List<String> excludeTags){
        for (int count=0; count < vg.getChildCount(); count++){
            View view = vg.getChildAt(count);
            //In case there are objects that should not change color
            if(excludeTags != null && excludeTags.size() > 0 && excludeTags.contains(view.getTag())) {
                continue;
            } else if(view instanceof ViewGroup){
                setAllViewsEnabled(isEnabled, (ViewGroup)view, excludeTags);
            }
            else{
                view.setEnabled(isEnabled);
            }
        }
    }
    public static void setAllViewsEnabled(boolean isEnabled, ViewGroup vg, String[] excludeTags){
        setAllViewsEnabled(isEnabled, vg, Arrays.asList(excludeTags));
    }

    public static void setAllViewsEnabled(boolean isEnabled, ViewGroup vg){
        setAllViewsEnabled(isEnabled, vg, new ArrayList<>());
    }
}
