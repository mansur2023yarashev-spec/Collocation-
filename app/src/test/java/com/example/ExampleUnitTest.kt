package com.example

import com.example.data.model.CollocationItem
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testResolveVerb_nounBeforeVerb() {
        val item = CollocationItem(
            noun = "report",
            collocation = "report acknowledge sth"
        )
        val verb = item.resolveVerb("report")
        assertEquals("acknowledge", verb)
    }

    @Test
    fun testResolveVerb_verbBeforeNoun() {
        val item = CollocationItem(
            noun = "decision",
            collocation = "make a decision"
        )
        val verb = item.resolveVerb("decision")
        assertEquals("make", verb)
    }

    @Test
    fun testResolveAdjective_adjBeforeNoun() {
        val item = CollocationItem(
            noun = "rain",
            collocation = "heavy rain"
        )
        val adj = item.resolveAdjective("heavy")
        assertEquals("heavy", adj)
    }
}


