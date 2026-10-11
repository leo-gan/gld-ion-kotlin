package ion

import ion.schema.parseSchemaFile
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SchemaParseTest {
    @Test
    fun parsesBothForms() {
        val message = parseSchemaFile("testdata/schema/message.json")
        assertTrue(message.find("Message") >= 0)
        val point = parseSchemaFile("testdata/schema/point.ion")
        assertTrue(point.find("Point") >= 0)
        assertFailsWith<DecodeError> { parseSchemaFile("testdata/invalid-schema/reject.ion") }
    }
}
