package ion

import ion.generated.Message
import ion.generated.Point
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GeneratedTest {
    @Test
    fun messageAndPointRoundTrip() {
        val msg = Message()
        msg.id = 7
        msg.name = "ada"
        msg.hasOk = true
        msg.ok = true
        msg.hasWeight = true
        msg.weight = 1.5
        msg.hasNote = true
        msg.note = "hi"
        msg.hasTags = true
        msg.tags.add("a")
        msg.tags.add("b")
        msg.hasWhen = true
        msg.`when` = "2023T"
        msg.hasPayload = true
        msg.payload = byteArrayOf(1, 2)
        val opt = EncodeOptions(binary = true, pretty = false, version = 10)
        val buf = msg.encodeTo(opt)
        assertEquals(buf.size, msg.encodedLength(opt))
        assertEquals(buf.size, msg.encode(opt).size)
        val back = Message.decodeFrom(buf)
        assertEquals(7L, back.id)
        assertEquals("ada", back.name)
        assertTrue(back.ok)
        assertEquals(1.5, back.weight)
        assertEquals("hi", back.note)
        assertEquals(listOf("a", "b"), back.tags)
        assertEquals("2023T", back.`when`)
        assertEquals(2, back.payload.size)
        assertEquals(1, back.payload[0].toInt() and 0xFF)
        val point = Point()
        point.x = 3
        point.label = "foo"
        val opt11 = EncodeOptions(binary = true, pretty = false, version = 11)
        val got = Point.decode(point.encode(opt11))
        assertEquals(3L, got.x)
        assertFalse(got.hasY)
        assertEquals("foo", got.label)
    }
}
