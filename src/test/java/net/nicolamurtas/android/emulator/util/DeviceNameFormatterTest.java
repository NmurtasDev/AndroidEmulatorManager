package net.nicolamurtas.android.emulator.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DeviceNameFormatter utility class.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
class DeviceNameFormatterTest {

    @Test
    void testFormatDeviceName_WithUnderscore() {
        assertEquals("Pixel 7", DeviceNameFormatter.formatDeviceName("pixel_7"));
        assertEquals("Pixel 8", DeviceNameFormatter.formatDeviceName("pixel_8"));
        assertEquals("Nexus 5X", DeviceNameFormatter.formatDeviceName("nexus_5x"));
    }

    @Test
    void testFormatDeviceName_MultipleUnderscores() {
        assertEquals("My Custom Device", DeviceNameFormatter.formatDeviceName("my_custom_device"));
        assertEquals("Galaxy S 23 Ultra", DeviceNameFormatter.formatDeviceName("galaxy_s_23_ultra"));
    }

    @Test
    void testFormatDeviceName_NoUnderscore() {
        assertEquals("Pixel", DeviceNameFormatter.formatDeviceName("pixel"));
        assertEquals("Nexus", DeviceNameFormatter.formatDeviceName("nexus"));
    }

    @Test
    void testFormatDeviceName_AlreadyCapitalized() {
        assertEquals("Pixel 7", DeviceNameFormatter.formatDeviceName("Pixel_7"));
        assertEquals("MY DEVICE", DeviceNameFormatter.formatDeviceName("MY_DEVICE"));
    }

    @Test
    void testFormatDeviceName_MixedCase() {
        assertEquals("Google Pixel 7", DeviceNameFormatter.formatDeviceName("Google_Pixel_7"));
        assertEquals("My Test Device", DeviceNameFormatter.formatDeviceName("my_Test_Device"));
    }

    @Test
    void testFormatDeviceName_NullInput() {
        assertNull(DeviceNameFormatter.formatDeviceName(null));
    }

    @Test
    void testFormatDeviceName_EmptyString() {
        assertEquals("", DeviceNameFormatter.formatDeviceName(""));
    }

    @Test
    void testFormatDeviceName_WhitespaceOnly() {
        assertEquals("", DeviceNameFormatter.formatDeviceName("   "));
    }

    @Test
    void testFormatDeviceName_SingleCharacter() {
        assertEquals("A", DeviceNameFormatter.formatDeviceName("a"));
        assertEquals("X", DeviceNameFormatter.formatDeviceName("x"));
    }

    @Test
    void testFormatDeviceName_TrailingUnderscore() {
        assertEquals("Pixel", DeviceNameFormatter.formatDeviceName("pixel_"));
        assertEquals("Device", DeviceNameFormatter.formatDeviceName("device__"));
    }

    @Test
    void testFormatDeviceName_LeadingUnderscore() {
        assertEquals("Pixel", DeviceNameFormatter.formatDeviceName("_pixel"));
        assertEquals("Device", DeviceNameFormatter.formatDeviceName("__device"));
    }

    @Test
    void testIsValidAvdName_ValidNames() {
        assertTrue(DeviceNameFormatter.isValidAvdName("MyDevice"));
        assertTrue(DeviceNameFormatter.isValidAvdName("my_device"));
        assertTrue(DeviceNameFormatter.isValidAvdName("my-device"));
        assertTrue(DeviceNameFormatter.isValidAvdName("MyDevice123"));
        assertTrue(DeviceNameFormatter.isValidAvdName("device_123"));
        assertTrue(DeviceNameFormatter.isValidAvdName("a"));
        assertTrue(DeviceNameFormatter.isValidAvdName("A1"));
        assertTrue(DeviceNameFormatter.isValidAvdName("test-device_123"));
    }

    @Test
    void testIsValidAvdName_InvalidNames_WithSpaces() {
        assertFalse(DeviceNameFormatter.isValidAvdName("My Device"));
        assertFalse(DeviceNameFormatter.isValidAvdName("device name"));
        assertFalse(DeviceNameFormatter.isValidAvdName(" device"));
        assertFalse(DeviceNameFormatter.isValidAvdName("device "));
        assertFalse(DeviceNameFormatter.isValidAvdName("my device 123"));
    }

    @Test
    void testIsValidAvdName_InvalidNames_SpecialCharacters() {
        assertFalse(DeviceNameFormatter.isValidAvdName("device!"));
        assertFalse(DeviceNameFormatter.isValidAvdName("device@123"));
        assertFalse(DeviceNameFormatter.isValidAvdName("device#name"));
        assertFalse(DeviceNameFormatter.isValidAvdName("device$"));
        assertFalse(DeviceNameFormatter.isValidAvdName("device%"));
        assertFalse(DeviceNameFormatter.isValidAvdName("device&name"));
        assertFalse(DeviceNameFormatter.isValidAvdName("device*"));
        assertFalse(DeviceNameFormatter.isValidAvdName("device(name)"));
        assertFalse(DeviceNameFormatter.isValidAvdName("device+name"));
        assertFalse(DeviceNameFormatter.isValidAvdName("device=name"));
        assertFalse(DeviceNameFormatter.isValidAvdName("device[name]"));
        assertFalse(DeviceNameFormatter.isValidAvdName("device{name}"));
        assertFalse(DeviceNameFormatter.isValidAvdName("device\\name"));
        assertFalse(DeviceNameFormatter.isValidAvdName("device/name"));
        assertFalse(DeviceNameFormatter.isValidAvdName("device:name"));
        assertFalse(DeviceNameFormatter.isValidAvdName("device;name"));
        assertFalse(DeviceNameFormatter.isValidAvdName("device\"name\""));
        assertFalse(DeviceNameFormatter.isValidAvdName("device'name'"));
        assertFalse(DeviceNameFormatter.isValidAvdName("device<name>"));
        assertFalse(DeviceNameFormatter.isValidAvdName("device,name"));
        assertFalse(DeviceNameFormatter.isValidAvdName("device.name"));
        assertFalse(DeviceNameFormatter.isValidAvdName("device?name"));
    }

    @Test
    void testIsValidAvdName_NullOrEmpty() {
        assertFalse(DeviceNameFormatter.isValidAvdName(null));
        assertFalse(DeviceNameFormatter.isValidAvdName(""));
    }

    @Test
    void testIsValidAvdName_OnlyValidCharacters() {
        // Should only accept: letters, numbers, underscores, hyphens
        assertTrue(DeviceNameFormatter.isValidAvdName("abcdefghijklmnopqrstuvwxyz"));
        assertTrue(DeviceNameFormatter.isValidAvdName("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        assertTrue(DeviceNameFormatter.isValidAvdName("0123456789"));
        assertTrue(DeviceNameFormatter.isValidAvdName("_-_-_"));
        assertTrue(DeviceNameFormatter.isValidAvdName("Test_Device-123"));
    }

    @Test
    void testIsValidAvdName_EdgeCases() {
        // Very long name (should still be valid as long as characters are valid)
        String longName = "a".repeat(100);
        assertTrue(DeviceNameFormatter.isValidAvdName(longName));

        // Mix of all valid characters
        assertTrue(DeviceNameFormatter.isValidAvdName("aA0_-"));

        // Starting with number (should be valid)
        assertTrue(DeviceNameFormatter.isValidAvdName("123device"));

        // Starting with underscore or hyphen (should be valid)
        assertTrue(DeviceNameFormatter.isValidAvdName("_device"));
        assertTrue(DeviceNameFormatter.isValidAvdName("-device"));
    }
}
