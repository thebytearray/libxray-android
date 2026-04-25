package org.thebytearray.libxray.android

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("org.thebytearray.libxray.android.test", appContext.packageName)
    }

    @Test
    fun nativeXrayVersionReturnsNonEmpty() {
        val out = LibXray.xrayVersion()
        assertTrue("expected libXray version payload", out.isNotEmpty())
    }
}