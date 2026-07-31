package com.example.vigil.detection

import org.junit.Assert.assertEquals
import org.junit.Test

class TextNormalizerTest {

    @Test
    fun spacedOutWord_isCollapsed() {
        assertEquals("free money now", TextNormalizer.normalize("f r e e money now"))
    }

    @Test
    fun dotSeparatedWord_isCollapsed() {
        assertEquals("send a gift card", TextNormalizer.normalize("send a g.i.f.t card"))
    }

    @Test
    fun invisibleCharacters_areStripped() {
        assertEquals("free money", TextNormalizer.normalize("free​ money‍"))
    }

    @Test
    fun ordinaryAbbreviation_isUnaffected() {
        assertEquals("call the U.S. office", TextNormalizer.normalize("call the U.S. office"))
    }

    @Test
    fun ordinaryText_isUnaffected() {
        assertEquals("Hey, are we still on for dinner?", TextNormalizer.normalize("Hey, are we still on for dinner?"))
    }
}
