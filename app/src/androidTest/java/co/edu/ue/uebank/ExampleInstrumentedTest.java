package co.edu.ue.uebank;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

//Clase
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    //Prueba paquete de la app
    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("co.edu.ue.uebank", appContext.getPackageName());
    }
}
