package ion.examples

import ion.Catalog
import ion.EncodeOptions
import ion.decode
import ion.encode

fun main() {
    val cat = Catalog()
    val doc = decode("{a:1,b:\"hi\"}\n".toByteArray(), cat)
    val text = encode(doc, EncodeOptions(binary = false, pretty = false, version = 10), cat)
    val binary = encode(doc, EncodeOptions(binary = true, pretty = false, version = 11), cat)
    println("text bytes ${text.size}")
    println("ion 1.1 binary bytes ${binary.size}")
}
