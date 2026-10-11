package ion

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RoundTripTest {
    private fun text(src: String): IonDoc = decodeText(src.toByteArray(Charsets.UTF_8), Catalog())

    private fun roundText(src: String) {
        val cat = Catalog()
        val doc = decodeText(src.toByteArray(Charsets.UTF_8), cat)
        val encoded = encodeText(doc, EncodeOptions(binary = false, pretty = false, version = 10))
        val again = decodeText(encoded, cat)
        assertTrue(docsEq(doc, again), "text round " + src + " -> " + encoded.toString(Charsets.UTF_8))
    }

    private fun roundBin(src: String, version: Int = 10) {
        val cat = Catalog()
        val doc = decodeText(src.toByteArray(Charsets.UTF_8), cat)
        val raw = encodeBinary(doc, EncodeOptions(binary = true, pretty = false, version = version), cat)
        val back = decodeBinary(raw, cat)
        assertTrue(docsEq(doc, back), "bin$version " + src + " -> " + raw.joinToString("") { "%02x".format(it) })
    }

    @Test
    fun scalars() {
        roundText("null\n")
        roundText("null.int\n")
        roundText("true\n")
        roundText("false\n")
        roundText("0\n")
        roundText("-42\n")
        roundText("9223372036854775807\n")
        roundText("-9223372036854775808\n")
        roundText("1.0\n")
        roundText("-0.00\n")
        roundText("1d2\n")
        roundText("1e0\n")
        roundText("-0e0\n")
        roundText("nan\n")
        roundText("+inf\n")
        roundText("\"hi\"\n")
        roundText("foo\n")
        roundText("{{aa==}}\n")
        roundText("{{ \"ab\" }}\n")
        roundText("[true, \"hi\"]\n")
        roundText("(a + 1)\n")
        roundText("{a:1, b:[true, \"hi\"]}\n")
        roundText("a::1\n")
        roundText("2007-02-23T12:14:33.079-08:00\n")
        roundText("2023T\n")
        roundText("2007-02-23\n")
        roundBin("{a:1, b:\"hi\"}\n")
        roundBin("null.int\n")
        roundBin("-0.00\n")
        roundBin("2007-02-23T12:14:33.079-08:00\n")
        roundBin("{{aa==}}\n")
        roundBin("{{ \"ab\" }}\n")
        roundBin("(a + 1)\n")
        roundBin("a::b::1\n")
        roundBin("128d0\n")
        roundBin("-128d0\n")
        roundBin("200d0\n")
        roundBin("-0e0\n")
        roundBin("2000-01-01T00:30+01:00\n")
        roundText("2007-02-23T12:14:33.128Z\n")
        roundBin("2007-02-23T12:14:33.128Z\n")
        roundText("{id:0,name:\"item\"}\n")
        roundBin("{id:0,name:\"item\"}\n")
        roundBin("foo\n", 11)
        roundBin("{a:1, b:\"hi\"}\n", 11)
        roundBin("[1, 2, 3]\n", 11)
    }

    @Test
    fun typedNullAndDecimalScale() {
        val n = text("null.int")
        assertEquals(K_NULL, n.nodes[n.top[0]].kind)
        assertEquals(K_INT, n.nodes[n.top[0]].a)
        val dec = text("-0.00")
        val dn = dec.nodes[dec.top[0]]
        assertEquals(K_DECIMAL, dn.kind)
        assertEquals(1, dn.c)
        assertEquals(-2L, dn.exp)
        val a = text("1.0")
        val b = text("1.00")
        assertTrue(!ionEq(a, a.top[0], b, b.top[0]))
        val s1 = text("{b:1, a:2}")
        val s2 = text("{a:2, b:1}")
        assertTrue(ionEq(s1, s1.top[0], s2, s2.top[0]))
    }

    @Test
    fun symbolTable() {
        val doc = text("\$ion_1_0\n\$ion_symbol_table::{symbols:[\"foo\"]}\n\$10\n")
        assertEquals(1, doc.top.size)
        assertEquals("foo", doc.symText(doc.nodes[doc.top[0]].a))
    }

    @Test
    fun ion11MarkerAndDelimited() {
        val raw = byteArrayOf(0xE0.toByte(), 0x01, 0x01, 0xEA.toByte(), 0xF0.toByte(), 0x61, 0x07, 0xEF.toByte())
        val doc = decode(raw, Catalog())
        assertEquals(1, doc.top.size)
        assertEquals(K_LIST, doc.nodes[doc.top[0]].kind)
        assertEquals(1, doc.nodes[doc.top[0]].nchild)
        assertEquals(7L, asI64(doc, doc.childAt(doc.top[0], 0)))
        val empty = decode(byteArrayOf(0xE0.toByte(), 0x01, 0x01, 0xEA.toByte(), 0xF0.toByte(), 0xEF.toByte()), Catalog())
        assertEquals(0, empty.nodes[empty.top[0]].nchild)
    }

    @Test
    fun bigInt() {
        val doc = text("100000000000000000000")
        val n = doc.nodes[doc.top[0]]
        val mag = BigInt()
        var i = 0
        while (i < n.b) {
            mag.limbs.add(doc.limbs[n.a + i])
            i++
        }
        assertEquals("100000000000000000000", mag.magDecimal())
    }

    @Test
    fun annotationDoesNotLeak() {
        val doc = text("a::foo foo")
        assertEquals(1, doc.nodes[doc.top[0]].annN)
        assertEquals(0, doc.nodes[doc.top[1]].annN)
    }

    @Test
    fun rejectsBad() {
        assertFailsWith<DecodeError> { text("+1") }
        assertFailsWith<DecodeError> { text("01") }
        assertFailsWith<DecodeError> { decode(byteArrayOf(0xE0.toByte(), 0x01, 0x00, 0xEA.toByte(), 0x30), Catalog()) }
    }

    @Test
    fun blobRoundBytes() {
        val doc = text("{{aa==}}")
        val bytes = doc.blobBytes(doc.nodes[doc.top[0]].a)
        assertEquals(1, bytes.size)
        assertEquals(0x69, bytes[0].toInt() and 0xFF)
    }
}
