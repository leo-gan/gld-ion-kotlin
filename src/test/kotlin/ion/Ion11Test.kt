package ion

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Ion11Test {
    private fun parse(vararg vals: Int): IonDoc {
        val raw = ByteArray(vals.size) { vals[it].toByte() }
        val doc = IonDoc()
        val tab = LocalTab()
        Ion11(raw).readTop(doc, tab, Catalog())
        return doc
    }

    @Test
    fun specExamples() {
        var doc = parse(0xE0, 0x01, 0x01, 0xEA, 0x60)
        assertEquals(K_INT, doc.nodes[doc.top[0]].kind)
        val zero = IonDoc()
        val z = zero.addI64(0)
        assertTrue(ionEq(doc, doc.top[0], zero, z))

        doc = parse(0xE0, 0x01, 0x01, 0xEA, 0x8F, 0x02)
        assertEquals(K_NULL, doc.nodes[doc.top[0]].kind)
        assertEquals(K_INT, doc.nodes[doc.top[0]].a)

        doc = parse(0xE0, 0x01, 0x01, 0xEA, 0xB6, 0x61, 0x01, 0x61, 0x02, 0x61, 0x03)
        assertEquals(K_LIST, doc.nodes[doc.top[0]].kind)
        assertEquals(3, doc.nodes[doc.top[0]].nchild)

        doc = parse(0xE0, 0x01, 0x01, 0xEA, 0xF0, 0xEF)
        assertEquals(0, doc.nodes[doc.top[0]].nchild)

        doc = parse(
            0xE0, 0x01, 0x01, 0xEA, 0xE1, 0x93, 0x66, 0x6F, 0x6F, 0x93, 0x62, 0x61, 0x72, 0xEF, 0x52, 0x03,
        )
        assertEquals("foo", doc.symText(doc.nodes[doc.top[0]].a))

        doc = parse(0xE0, 0x01, 0x01, 0xEA, 0xA3, 0x62, 0x61, 0x72)
        assertEquals("bar", doc.symText(doc.nodes[doc.top[0]].a))

        doc = parse(0xE0, 0x01, 0x01, 0xEA, 0x72, 0x07, 0x00)
        val dec = doc.nodes[doc.top[0]]
        assertEquals(K_DECIMAL, dec.kind)
        assertEquals(1, dec.c)
        assertEquals(3L, dec.exp)
    }

    @Test
    fun textMacros() {
        val doc = decodeText(
            "\$ion_1_1\n(:\$ion set_macros (point {x: (:?), y: (:? 0)}))\n(:point 2 3)\n".toByteArray(),
            Catalog(),
        )
        assertEquals(K_STRUCT, doc.nodes[doc.top[0]].kind)
        assertEquals(2, doc.nodes[doc.top[0]].nchild)
        assertEquals(2L, asI64(doc, doc.childAt(doc.top[0], 0)))
        assertEquals(3L, asI64(doc, doc.childAt(doc.top[0], 1)))
        val absent = decodeText(
            "\$ion_1_1\n(:\$ion set_macros (point {x: (:?), y: (:? 0)}))\n(:point 5 (:))\n".toByteArray(),
            Catalog(),
        )
        assertEquals(0L, asI64(absent, absent.childAt(absent.top[0], 1)))
    }

    @Test
    fun writerMatchesSpecBytes() {
        val one = IonDoc()
        one.addTop(one.addDecimal(bigFromU64(7, false), false, 0))
        val opt = EncodeOptions(binary = true, pretty = false, version = 11)
        val raw = encodeBinary(one, opt, Catalog())
        assertEquals("e00101ea720107", raw.joinToString("") { "%02x".format(it) })
        val y = IonDoc()
        val t = IonTime()
        t.year = 1947
        t.prec = PREC_YEAR
        t.unknown = true
        y.addTop(y.addTime(t))
        val hex = encodeBinary(y, opt, Catalog()).joinToString("") { "%02x".format(it) }
        assertEquals("e00101eaf7059b07", hex)
    }
}
