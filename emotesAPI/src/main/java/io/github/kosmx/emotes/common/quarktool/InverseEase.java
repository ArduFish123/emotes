package io.github.kosmx.emotes.common.quarktool;

import io.github.kosmx.emotes.common.tools.Ease;
import io.github.kosmx.emotes.common.tools.Easing;

public class InverseEase {
    public static Ease inverse(Ease ease){
        String str = ease.toString();
        if(str.substring(0, 2).equals("IN") && ! str.substring(0, 5).equals("INOUT")){
            return (Easing.easeFromString("OUT" + str.substring(2)));
        }else if(str.substring(0, 3).equals("OUT")){
            return (Easing.easeFromString("IN" + str.substring(3)));
        }else return ease;
    }
}
