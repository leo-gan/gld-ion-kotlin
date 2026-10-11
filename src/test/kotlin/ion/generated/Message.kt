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

class Message {
    var id: Long = 0L
    var name: String = ""
    var hasOk: Boolean = false
    var ok: Boolean = false
    var hasWeight: Boolean = false
    var weight: Double = 0.0
    var hasNote: Boolean = false
    var note: String = ""
    var hasTags: Boolean = false
    var tags: MutableList<String> = mutableListOf()
    var hasWhen: Boolean = false
    var `when`: String = ""
    var hasPayload: Boolean = false
    var payload: ByteArray = ByteArray(0)

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
        val sym_id = doc.addSymbolText("id")
        val slot_id = doc.nodes[sym_id].a
        val val_id = doc.addI64(id)
        doc.addChild(st, val_id, slot_id)
        val sym_name = doc.addSymbolText("name")
        val slot_name = doc.nodes[sym_name].a
        val val_name = doc.addString(name)
        doc.addChild(st, val_name, slot_name)
        if (hasOk) {
            val sym_ok = doc.addSymbolText("ok")
            val slot_ok = doc.nodes[sym_ok].a
            val val_ok = doc.addBool(ok)
            doc.addChild(st, val_ok, slot_ok)
        }
        if (hasWeight) {
            val sym_weight = doc.addSymbolText("weight")
            val slot_weight = doc.nodes[sym_weight].a
            val val_weight = doc.addFloat(8, weight.toRawBits())
            doc.addChild(st, val_weight, slot_weight)
        }
        if (hasNote) {
            val sym_note = doc.addSymbolText("note")
            val slot_note = doc.nodes[sym_note].a
            val val_note = doc.addString(note)
            doc.addChild(st, val_note, slot_note)
        }
        if (hasTags) {
            val sym_tags = doc.addSymbolText("tags")
            val slot_tags = doc.nodes[sym_tags].a
            val val_tags = doc.startContainer(K_LIST)
            var j_val_tags = 0
            while (j_val_tags < tags.size) {
                val item_val_tags = doc.addString(tags[j_val_tags])
                doc.addChild(val_tags, item_val_tags, -1)
                j_val_tags++
            }
            doc.addChild(st, val_tags, slot_tags)
        }
        if (hasWhen) {
            val sym_when = doc.addSymbolText("when")
            val slot_when = doc.nodes[sym_when].a
            val val_when = parseIon(doc, `when`, 6)
            doc.addChild(st, val_when, slot_when)
        }
        if (hasPayload) {
            val sym_payload = doc.addSymbolText("payload")
            val slot_payload = doc.nodes[sym_payload].a
            val val_payload = doc.addBytes(9, payload)
            doc.addChild(st, val_payload, slot_payload)
        }
        return st
    }

    companion object {
        fun decode(raw: ByteArray): Message = decodeFrom(raw)

        fun decodeFrom(raw: ByteArray): Message {
            val doc = decodeAny(raw)
            if (doc.top.size != 1) throw DecodeError(DecodeError.KIND_TYPE, 0)
            return fromDoc(doc, doc.top[0])
        }

        fun fromDoc(doc: IonDoc, id: Int): Message {
            if (doc.nodes[id].kind != K_STRUCT) throw DecodeError(DecodeError.KIND_TYPE, 0)
            val out = Message()
            var got_id = false
            var got_name = false
            var i = 0
            while (i < doc.nodes[id].nchild) {
                val fname = fieldName(doc, id, i)
                val child = doc.childAt(id, i)
                if (fname == "id") {
                    out.id = asI64(doc, child)
                    got_id = true
                }
                else if (fname == "name") {
                    out.name = asString(doc, child)
                    got_name = true
                }
                else if (fname == "ok") {
                    out.ok = asBool(doc, child)
                    out.hasOk = true
                }
                else if (fname == "weight") {
                    out.weight = asF64(doc, child)
                    out.hasWeight = true
                }
                else if (fname == "note") {
                    out.note = asString(doc, child)
                    out.hasNote = true
                }
                else if (fname == "tags") {
                    if (doc.nodes[child].kind != K_LIST) {
                        throw DecodeError(DecodeError.KIND_TYPE, 0)
                    }
                    var item_out_tags = ""
                    var j_out_tags = 0
                    while (j_out_tags < doc.nodes[child].nchild) {
                        item_out_tags = asString(doc, doc.childAt(child, j_out_tags))
                        out.tags.add(item_out_tags)
                        j_out_tags++
                    }
                    out.hasTags = true
                }
                else if (fname == "when") {
                    out.`when` = ionText(doc, child, 6)
                    out.hasWhen = true
                }
                else if (fname == "payload") {
                    out.payload = asBytes(doc, child, 9)
                    out.hasPayload = true
                }
                else {
                    throw DecodeError(DecodeError.KIND_SCHEMA, 0)
                }
                i++
            }
            if (!got_id) throw DecodeError(DecodeError.KIND_SCHEMA, 0)
            if (!got_name) throw DecodeError(DecodeError.KIND_SCHEMA, 0)
            return out
        }
    }
}
