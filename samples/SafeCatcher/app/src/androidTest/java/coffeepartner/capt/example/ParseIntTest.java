package coffeepartner.capt.example;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ParseIntTest {

    @Test
    public void parseInt() {
        try {
            Integer.parseInt(null);
        } catch (NumberFormatException e) {
            throw new AssertionError(e);
        }
    }
}
