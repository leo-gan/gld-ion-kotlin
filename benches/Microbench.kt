package ion.bench

import ion.Catalog
import ion.EncodeOptions
import ion.IonDoc
import ion.K_STRUCT
import ion.decodeBinary
import ion.decodeText
import ion.encodeBinary
import ion.encodeText
import kotlin.system.measureNanoTime

fun main(args: Array<String>) {
    val doc = sample()
    val cat = Catalog()
    val opt = EncodeOptions(binary = true, pretty = false, version = 10)
    val textOpt = EncodeOptions(binary = false, pretty = false, version = 10)
    val long = args.isNotEmpty() && args[0] == "long"
    val n = if (long) 20000 else 200
    val warm = if (long) 4000 else 20
    repeat(warm) {
        encodeBinary(doc, opt, cat)
        encodeText(doc, textOpt)
    }
    val binNs = measureNanoTime {
        repeat(n) { encodeBinary(doc, opt, cat) }
    } / n
    val textNs = measureNanoTime {
        repeat(n) { encodeText(doc, textOpt) }
    } / n
    val raw = encodeBinary(doc, opt, cat)
    val text = encodeText(doc, textOpt)
    repeat(20) {
        decodeBinary(raw, cat)
        decodeText(text, cat)
    }
    val decBin = measureNanoTime {
        repeat(n) { decodeBinary(raw, cat) }
    } / n
    val decText = measureNanoTime {
        repeat(n) { decodeText(text, cat) }
    } / n
    println("binary_encode_ns $binNs")
    println("text_encode_ns $textNs")
    println("binary_decode_ns $decBin")
    println("text_decode_ns $decText")
    println("binary_bytes ${raw.size}")
    println("text_bytes ${text.size}")
}

private fun sample(): IonDoc {
    val doc = IonDoc()
    var i = 0
    while (i < 32) {
        val st = doc.startContainer(K_STRUCT)
        val name = doc.addSymbolText("id")
        doc.addChild(st, doc.addI64(i.toLong()), doc.nodes[name].a)
        val label = doc.addSymbolText("name")
        doc.addChild(st, doc.addString("item"), doc.nodes[label].a)
        doc.addTop(st)
        i++
    }
    return doc
}
