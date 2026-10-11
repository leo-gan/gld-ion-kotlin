package ion.generated

import ion.Catalog
import ion.DecodeError
import ion.EncodeOptions
import ion.IonDoc
import ion.K_BLOB
import ion.K_CLOB
import ion.K_LIST
import ion.K_STRUCT
import ion.asBool
import ion.asBytes
import ion.asF64
import ion.asI64
import ion.asString
import ion.decodeAny
import ion.encodeBinary
import ion.encodeText
import ion.fieldName
import ion.ionText
import ion.parseIon

class Point {
    var x: Long = 0L
    var hasY: Boolean = false
    var y: Long = 0L
    var label: String = ""

    fun encodedLength(options: EncodeOptions): Int = encodeTo(options).size

    fun encode(options: EncodeOptions): ByteArray = encodeTo(options)

    fun encodeTo(options: EncodeOptions): ByteArray {
        val doc = toDoc()
        val cat = Catalog()
        return if (options.binary) encodeBinary(doc, options, cat) else encodeText(doc, options)
    }

    fun toDoc(): IonDoc {
        val doc = IonDoc()
        val st = writeInto(doc)
        doc.addTop(st)
        return doc
    }

    fun writeInto(doc: IonDoc): Int {
        val st = doc.startContainer(K_STRUCT)
        val sym_x = doc.addSymbolText("x")
        val slot_x = doc.nodes[sym_x].a
        val val_x = doc.addI64(x)
        doc.addChild(st, val_x, slot_x)
        if (hasY) {
            val sym_y = doc.addSymbolText("y")
            val slot_y = doc.nodes[sym_y].a
            val val_y = doc.addI64(y)
            doc.addChild(st, val_y, slot_y)
        }
        val sym_label = doc.addSymbolText("label")
        val slot_label = doc.nodes[sym_label].a
        val val_label = parseIon(doc, label, 8)
        doc.addChild(st, val_label, slot_label)
        return st
    }

    companion object {
        fun decode(raw: ByteArray): Point = decodeFrom(raw)

        fun decodeFrom(raw: ByteArray): Point {
            val doc = decodeAny(raw)
            if (doc.top.size != 1) throw DecodeError(DecodeError.KIND_TYPE, 0)
            return fromDoc(doc, doc.top[0])
        }

        fun fromDoc(doc: IonDoc, id: Int): Point {
            if (doc.nodes[id].kind != K_STRUCT) throw DecodeError(DecodeError.KIND_TYPE, 0)
            val out = Point()
            var got_x = false
            var got_label = false
            var i = 0
            while (i < doc.nodes[id].nchild) {
                val fname = fieldName(doc, id, i)
                val child = doc.childAt(id, i)
                if (fname == "x") {
                    out.x = asI64(doc, child)
                    got_x = true
                }
                else if (fname == "y") {
                    out.y = asI64(doc, child)
                    out.hasY = true
                }
                else if (fname == "label") {
                    out.label = ionText(doc, child, 8)
                    got_label = true
                }
                else {
                    throw DecodeError(DecodeError.KIND_SCHEMA, 0)
                }
                i++
            }
            if (!got_x) throw DecodeError(DecodeError.KIND_SCHEMA, 0)
            if (!got_label) throw DecodeError(DecodeError.KIND_SCHEMA, 0)
            return out
        }
    }
}
