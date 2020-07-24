package com.paypal.android.sdk.onetouch.core.network;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
public class EnvironmentManagerUnitTest {

    @Test
    public void isMock_returnsTrueForMockEnvironment() {
        assertTrue(EnvironmentManager.isMock("mock"));
    }

    @Test
    public void isMock_returnsFalseForNonMockString() {
        assertFalse(EnvironmentManager.isMock("sandbox"));
    }

    @Test
    public void isSandbox_returnsTrueForSandboxEnvironment() {
        assertTrue(EnvironmentManager.isSandbox("sandbox"));
    }

    @Test
    public void isSandbox_returnsFalseForNonSandboxString() {
        assertFalse(EnvironmentManager.isSandbox("live"));
    }

    @Test
    public void isLive_returnsTrueForLiveEnvironment() {
        assertTrue(EnvironmentManager.isLive("live"));
    }

    @Test
    public void isLive_returnsFalseForNonLiveString() {
        assertFalse(EnvironmentManager.isLive("mock"));
    }

    @Test
    public void isStage_returnsTrueForNonMockSandboxOrLiveEnvironment() {
        assertTrue(EnvironmentManager.isStage("http://stage.com"));
    }

    @Test
    public void isStage_returnsFalseForMockEnvironment() {
        assertFalse(EnvironmentManager.isStage("mock"));
    }

    @Test
    public void isStage_returnsFalseForSandboxEnvironment() {
        assertFalse(EnvironmentManager.isStage("sandbox"));
    }

    @Test
    public void isStage_returnsFalseForLiveEnvironment() {
        assertFalse(EnvironmentManager.isStage("live"));
    }

    @Test
    public void getEnvironmentUrl_returnsNullForMock() {
        assertNull(EnvironmentManager.getEnvironmentUrl("mock"));
    }

    @Test
    public void getEnvironmentUrl_returnsUrlForSandbox() {
        assertEquals("https://api-m.sandbox.paypal.com/v1/",
                EnvironmentManager.getEnvironmentUrl("sandbox"));
    }

    @Test
    public void getEnvironmentUrl_returnsUrlForLive() {
        assertEquals("https://api-m.paypal.com/v1/",
                EnvironmentManager.getEnvironmentUrl("live"));
    }

    @Test
    public void getEnvironmentUrl_returnsStringForUnknownEnvironment() {
        assertEquals("http://stage.com", EnvironmentManager.getEnvironmentUrl("http://stage.com"));
    }
}
