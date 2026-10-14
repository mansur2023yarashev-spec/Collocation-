package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.parser.NounJsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Alpha", appName)
    }

    @Test
    fun `test NounJsonParser map format`() {
        val json = """
            {
                "decision": ["make a decision", "reach a decision"],
                "ability": ["demonstrate an ability"]
            }
        """.trimIndent()
        val parsed = NounJsonParser.parse(json)
        assertEquals(2, parsed.size)

        val decisionGroup = parsed.firstOrNull { it.noun == "decision" }
        assertTrue(decisionGroup != null)
        assertEquals(2, decisionGroup?.items?.size)
        assertEquals("make a decision", decisionGroup?.items?.get(0)?.collocation)
    }

    @Test
    fun `test NounJsonParser wrapped array format`() {
        val json = """
            {
                "nouns": [
                    {
                        "noun": "decision",
                        "collocations": ["make a decision", "reverse a decision"]
                    }
                ]
            }
        """.trimIndent()
        val parsed = NounJsonParser.parse(json)
        assertEquals(1, parsed.size)
        assertEquals("decision", parsed[0].noun)
        assertEquals(2, parsed[0].items.size)
    }

    @Test
    fun `test NounJsonParser empty and invalid`() {
        assertTrue(NounJsonParser.parse("").isEmpty())
        assertTrue(NounJsonParser.parse("invalid json text").isEmpty())
    }

    @Test
    fun `test all realistic Noun JSON formats`() {
        // Format A1: Categorized map with verbs and adjectives
        val formatA1 = """
            {
              "decision": {
                "verbs": ["make a decision", "reach a decision"],
                "adjectives": ["tough decision", "hard decision"]
              }
            }
        """.trimIndent()
        val parsedA1 = NounJsonParser.parse(formatA1)
        assertEquals(1, parsedA1.size)
        assertEquals(4, parsedA1[0].items.size)

        // Format A2: Categorized map with short words
        val formatA2 = """
            {
              "decision": {
                "verbs": ["make", "reach"],
                "adjectives": ["tough", "hard"]
              }
            }
        """.trimIndent()
        val parsedA2 = NounJsonParser.parse(formatA2)
        assertEquals(1, parsedA2.size)
        assertEquals(4, parsedA2[0].items.size)

        // Format B: Array of objects with various keys
        val formatB = """
            [
              {
                "term": "decision",
                "collocations": ["make a decision", "reach a decision"]
              }
            ]
        """.trimIndent()
        val parsedB = NounJsonParser.parse(formatB)
        assertEquals(1, parsedB.size)
        assertEquals("decision", parsedB[0].noun)
        assertEquals(2, parsedB[0].items.size)

        // Format C: Wrapper with 'entries'
        val formatC = """
            {
              "entries": [
                {
                  "noun": "decision",
                  "collocations": ["make a decision"]
                }
              ]
            }
        """.trimIndent()
        val parsedC = NounJsonParser.parse(formatC)
        assertEquals(1, parsedC.size)
        assertEquals("decision", parsedC[0].noun)

        // Format D: Flat array of collocations
        val formatD = """
            [
              { "noun": "decision", "collocation": "make a decision" },
              { "noun": "decision", "collocation": "reach a decision" },
              { "noun": "effort", "collocation": "make an effort" }
            ]
        """.trimIndent()
        val parsedD = NounJsonParser.parse(formatD)
        assertEquals(2, parsedD.size)
        val decisionGroup = parsedD.first { it.noun == "decision" }
        assertEquals(2, decisionGroup.items.size)

        // Format E: Array of pairs
        val formatE = """
            [
              ["decision", "make a decision"],
              ["decision", "reach a decision"]
            ]
        """.trimIndent()
        val parsedE = NounJsonParser.parse(formatE)
        assertEquals(1, parsedE.size)
        assertEquals(2, parsedE[0].items.size)

        // Format F: BOM
        val formatF = "\uFEFF{\n  \"decision\": [\"make a decision\"]\n}"
        val parsedF = NounJsonParser.parse(formatF)
        assertEquals(1, parsedF.size)
        assertEquals("decision", parsedF[0].noun)

        // Format G: Map of phrases to meanings
        val formatG = """
            {
              "decision": {
                "make a decision": "qaror qilmoq",
                "reach a decision": "qarorga kelmoq"
              }
            }
        """.trimIndent()
        val parsedG = NounJsonParser.parse(formatG)
        assertEquals(1, parsedG.size)
        assertEquals("decision", parsedG[0].noun)
        assertEquals("make a decision", parsedG[0].items[0].collocation)
        assertEquals("qaror qilmoq", parsedG[0].items[0].meaning)

        // Format H1: List of verbs under noun
        val formatH1 = """
            {
              "decision": ["make", "reach", "take"],
              "effort": ["make", "put in"]
            }
        """.trimIndent()
        val parsedH1 = NounJsonParser.parse(formatH1)
        assertEquals(2, parsedH1.size)
        val decisionGroupH1 = parsedH1.first { it.noun == "decision" }
        assertEquals(3, decisionGroupH1.items.size)
        assertEquals("make", decisionGroupH1.items[0].resolveVerb("decision"))
        assertEquals("make a decision", decisionGroupH1.items[0].displayCollocation("decision"))

        // Format H2: Verb to Uzbek meaning map
        val formatH2 = """
            {
              "decision": {
                "make": "qaror qilmoq",
                "reach": "qarorga kelmoq"
              }
            }
        """.trimIndent()
        val parsedH2 = NounJsonParser.parse(formatH2)
        assertEquals(1, parsedH2.size)
        assertEquals("make", parsedH2[0].items[0].resolveVerb("decision"))
        assertEquals("qaror qilmoq", parsedH2[0].items[0].meaning)
        assertEquals("make a decision", parsedH2[0].items[0].displayCollocation("decision"))

        // Format H3: Array of objects with verbs
        val formatH3 = """
            [
              {
                "noun": "decision",
                "verbs": [
                  { "verb": "make", "meaning": "qaror qilmoq" }
                ]
              }
            ]
        """.trimIndent()
        val parsedH3 = NounJsonParser.parse(formatH3)
        assertEquals(1, parsedH3.size)
        assertEquals("make", parsedH3[0].items[0].resolveVerb("decision"))
        assertEquals("qaror qilmoq", parsedH3[0].items[0].meaning)
        assertEquals("make a decision", parsedH3[0].items[0].displayCollocation("decision"))
    }

    @Test
    fun `test report acknowledge sth parsing and verb resolution`() {
        val json = """
            {
              "report": [
                "report acknowledge sth"
              ]
            }
        """.trimIndent()
        val parsed = NounJsonParser.parse(json)
        assertEquals(1, parsed.size)
        val item = parsed[0].items[0]
        assertEquals("report acknowledge sth", item.collocation)
        assertEquals("acknowledge", item.resolveVerb("report"))
        assertEquals("report acknowledge sth", item.displayCollocation("report"))
    }
}
