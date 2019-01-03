package coffeepartner.capt.example;

import android.util.Log;

import coffeepartner.capt.sample.safecatcher.rt.Match;

public class Matches {

    @Match(owner = "java/lang/Integer", name = "parseInt", desc = "(Ljava/lang/String;)I")
    public static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            Log.e("catch", "parseInt : " + s, e);
        }
        return 0;
    }
}
