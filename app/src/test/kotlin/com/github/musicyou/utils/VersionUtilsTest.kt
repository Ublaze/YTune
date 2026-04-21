package com.github.musicyou.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionUtilsTest {

    @Test
    fun `equal versions return zero`() {
        assertEquals(0, compareVersions("1.0.0", "1.0.0"))
    }

    @Test
    fun `equal versions with v prefix return zero`() {
        assertEquals(0, compareVersions("v1.2.3", "v1.2.3"))
    }

    @Test
    fun `newer major version is greater`() {
        assertTrue(compareVersions("2.0.0", "1.9.9") > 0)
    }

    @Test
    fun `newer minor version is greater`() {
        assertTrue(compareVersions("1.2.0", "1.1.9") > 0)
    }

    @Test
    fun `newer patch version is greater`() {
        assertTrue(compareVersions("1.0.2", "1.0.1") > 0)
    }

    @Test
    fun `older version is less`() {
        assertTrue(compareVersions("1.0.0", "1.0.1") < 0)
    }

    @Test
    fun `v prefix is stripped correctly`() {
        assertEquals(0, compareVersions("v2.0.0", "2.0.0"))
    }

    @Test
    fun `shorter version string is padded with zeros`() {
        assertEquals(0, compareVersions("1.0", "1.0.0"))
        assertTrue(compareVersions("1.1", "1.0.9") > 0)
    }

    @Test
    fun `non-numeric segments default to zero`() {
        assertEquals(0, compareVersions("1.x.0", "1.0.0"))
    }
}
