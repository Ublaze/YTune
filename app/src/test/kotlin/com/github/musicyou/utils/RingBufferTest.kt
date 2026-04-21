package com.github.musicyou.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RingBufferTest {

    @Test
    fun `initial values are set by the init block`() {
        val buffer = RingBuffer(3) { index -> index * 10 }
        assertEquals(0, buffer.getOrNull(0))
        assertEquals(10, buffer.getOrNull(1))
        assertEquals(20, buffer.getOrNull(2))
    }

    @Test
    fun `getOrNull returns null for out-of-bounds index`() {
        val buffer = RingBuffer(2) { 0 }
        assertNull(buffer.getOrNull(5))
        assertNull(buffer.getOrNull(-1))
    }

    @Test
    fun `append overwrites the oldest element`() {
        val buffer = RingBuffer(3) { 0 }
        buffer.append(1)
        buffer.append(2)
        buffer.append(3)
        // Buffer is now [1, 2, 3]; next append wraps around to slot 0
        buffer.append(4)
        assertEquals(4, buffer.getOrNull(0))
        assertEquals(2, buffer.getOrNull(1))
        assertEquals(3, buffer.getOrNull(2))
    }

    @Test
    fun `buffer of size one is always overwritten`() {
        val buffer = RingBuffer(1) { -1 }
        assertEquals(-1, buffer.getOrNull(0))
        buffer.append(42)
        assertEquals(42, buffer.getOrNull(0))
        buffer.append(99)
        assertEquals(99, buffer.getOrNull(0))
    }

    @Test
    fun `size property reflects the configured capacity`() {
        val buffer = RingBuffer(5) { 0 }
        assertEquals(5, buffer.size)
    }
}
