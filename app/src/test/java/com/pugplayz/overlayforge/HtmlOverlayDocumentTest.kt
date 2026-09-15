package com.pugplayz.overlayforge

import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlOverlayDocumentTest {

    @Test
    fun defaultHealthOverlay_isPresentInRenderedDocument() {
        val document = buildHtmlDocument(SAMPLE_HTML, SAMPLE_CSS)

        assertTrue(document.contains("HEALTH"))
        assertTrue(document.contains("82 / 100"))
        assertTrue(document.contains("class=\"bar-fill\""))
        assertTrue(document.contains("background: linear-gradient"))
        assertTrue(document.contains("background: transparent !important"))
    }

    @Test
    fun editedHtmlAndCss_areInsertedIntoRenderedDocument() {
        val html = "<div id=\"probe\">VISIBLE</div>"
        val css = "#probe { color: rgb(1, 2, 3); }"
        val document = buildHtmlDocument(html, css)

        assertTrue(document.contains(html))
        assertTrue(document.contains(css))
    }
}
