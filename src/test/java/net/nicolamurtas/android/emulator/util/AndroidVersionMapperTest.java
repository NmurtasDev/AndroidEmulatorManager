package net.nicolamurtas.android.emulator.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AndroidVersionMapper utility class.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
class AndroidVersionMapperTest {

    @Test
    void testGetAndroidVersionName_ValidApiLevels() {
        assertEquals("Android 16", AndroidVersionMapper.getAndroidVersionName("36"));
        assertEquals("Android 15", AndroidVersionMapper.getAndroidVersionName("35"));
        assertEquals("Android 14", AndroidVersionMapper.getAndroidVersionName("34"));
        assertEquals("Android 13", AndroidVersionMapper.getAndroidVersionName("33"));
        assertEquals("Android 12L", AndroidVersionMapper.getAndroidVersionName("32"));
        assertEquals("Android 12", AndroidVersionMapper.getAndroidVersionName("31"));
        assertEquals("Android 11", AndroidVersionMapper.getAndroidVersionName("30"));
        assertEquals("Android 10", AndroidVersionMapper.getAndroidVersionName("29"));
        assertEquals("Android 9", AndroidVersionMapper.getAndroidVersionName("28"));
        assertEquals("Android 8.1", AndroidVersionMapper.getAndroidVersionName("27"));
        assertEquals("Android 8.0", AndroidVersionMapper.getAndroidVersionName("26"));
        assertEquals("Android 7.1", AndroidVersionMapper.getAndroidVersionName("25"));
        assertEquals("Android 7.0", AndroidVersionMapper.getAndroidVersionName("24"));
        assertEquals("Android 6.0", AndroidVersionMapper.getAndroidVersionName("23"));
        assertEquals("Android 5.1", AndroidVersionMapper.getAndroidVersionName("22"));
        assertEquals("Android 5.0", AndroidVersionMapper.getAndroidVersionName("21"));
    }

    @Test
    void testGetAndroidVersionName_UnknownApiLevel() {
        assertEquals("Android API 99", AndroidVersionMapper.getAndroidVersionName("99"));
        assertEquals("Android API 15", AndroidVersionMapper.getAndroidVersionName("15"));
        assertEquals("Android API 1", AndroidVersionMapper.getAndroidVersionName("1"));
    }

    @Test
    void testGetAndroidVersionName_NullOrUnknown() {
        assertEquals("Android (Unknown)", AndroidVersionMapper.getAndroidVersionName(null));
        assertEquals("Android (Unknown)", AndroidVersionMapper.getAndroidVersionName("Unknown"));
    }

    @Test
    void testGetAndroidVersionName_EmptyString() {
        assertEquals("Android API ", AndroidVersionMapper.getAndroidVersionName(""));
    }

    @Test
    void testExtractApiLevelFromConfig_ValidConfig() {
        String configContent = """
                avd.name=TestDevice
                image.sysdir.1=system-images/android-35/google_apis/x86_64/
                hw.device.name=pixel_7
                """;

        assertEquals("35", AndroidVersionMapper.extractApiLevelFromConfig(configContent));
    }

    @Test
    void testExtractApiLevelFromConfig_MultipleLines() {
        String configContent = """
                avd.name=MyDevice
                some.other.property=value
                image.sysdir.1 = system-images/android-34/default/arm64-v8a/
                another.property=test
                """;

        assertEquals("34", AndroidVersionMapper.extractApiLevelFromConfig(configContent));
    }

    @Test
    void testExtractApiLevelFromConfig_DifferentApiLevels() {
        String config30 = "image.sysdir.1=system-images/android-30/google_apis/x86_64/";
        assertEquals("30", AndroidVersionMapper.extractApiLevelFromConfig(config30));

        String config33 = "image.sysdir.1=system-images/android-33/google_apis_playstore/x86_64/";
        assertEquals("33", AndroidVersionMapper.extractApiLevelFromConfig(config33));
    }

    @Test
    void testExtractApiLevelFromConfig_NotFound() {
        String configContent = """
                avd.name=TestDevice
                hw.device.name=pixel_7
                some.property=value
                """;

        assertEquals("Unknown", AndroidVersionMapper.extractApiLevelFromConfig(configContent));
    }

    @Test
    void testExtractApiLevelFromConfig_NullContent() {
        assertEquals("Unknown", AndroidVersionMapper.extractApiLevelFromConfig(null));
    }

    @Test
    void testExtractApiLevelFromConfig_EmptyContent() {
        assertEquals("Unknown", AndroidVersionMapper.extractApiLevelFromConfig(""));
    }

    @Test
    void testExtractApiLevelFromConfig_MalformedLine() {
        String configContent = """
                image.sysdir.1
                image.sysdir.1=
                image.sysdir.1=invalid
                """;

        assertEquals("Unknown", AndroidVersionMapper.extractApiLevelFromConfig(configContent));
    }

    @Test
    void testExtractApiLevelFromTarget_ValidFormats() {
        assertEquals("35", AndroidVersionMapper.extractApiLevelFromTarget("Android 15.0 (API level 35)"));
        assertEquals("34", AndroidVersionMapper.extractApiLevelFromTarget("Android 14 (API level 34)"));
        assertEquals("30", AndroidVersionMapper.extractApiLevelFromTarget("Google APIs (API level 30)"));
    }

    @Test
    void testExtractApiLevelFromTarget_SimpleNumber() {
        assertEquals("35", AndroidVersionMapper.extractApiLevelFromTarget("35"));
        assertEquals("30", AndroidVersionMapper.extractApiLevelFromTarget("android 30 test"));
    }

    @Test
    void testExtractApiLevelFromTarget_NotFound() {
        assertEquals("Unknown", AndroidVersionMapper.extractApiLevelFromTarget("No API level here"));
        assertEquals("Unknown", AndroidVersionMapper.extractApiLevelFromTarget("Android Pie"));
    }

    @Test
    void testExtractApiLevelFromTarget_NullOrEmpty() {
        assertEquals("Unknown", AndroidVersionMapper.extractApiLevelFromTarget(null));
        assertEquals("Unknown", AndroidVersionMapper.extractApiLevelFromTarget(""));
    }

    @Test
    void testExtractApiLevelFromTarget_EdgeCases() {
        // API level at the end without closing parenthesis
        assertEquals("Unknown", AndroidVersionMapper.extractApiLevelFromTarget("Android (API level"));

        // Multiple numbers
        assertEquals("14", AndroidVersionMapper.extractApiLevelFromTarget("Android 14 version 2"));
    }
}
