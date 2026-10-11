package ion

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IonSuiteTest {
    @Test
    fun corpus() {
        val root = File("testdata/ion-tests/iontestdata")
        val catalogPath = File("testdata/ion-tests/catalog/catalog.ion")
        val cat = loadCatalog(catalogPath.readBytes())
        val files = root.walkTopDown().filter { it.isFile && (it.name.endsWith(".ion") || it.name.endsWith(".10n")) }
            .sortedBy { it.path }
            .toList()
        assertEquals(785, files.size, "ion-tests file count from ${root.absolutePath}")
        val failures = ArrayList<String>()
        for (file in files) {
            val path = file.path.replace('\\', '/')
            val expectBad = "/bad/" in path
            val equiv = "/equivs/" in path
            val noneq = "/non-equivs/" in path
            try {
                val doc = parsePath(file, cat)
                if (expectBad) {
                    failures.add("expected failure $path")
                } else if (equiv || noneq) {
                    if (doc.top.isEmpty()) failures.add("empty group $path")
                    for (top in doc.top) {
                        if (!seqOk(doc, top, equiv, cat)) failures.add("equiv $path")
                    }
                }
            } catch (e: DecodeError) {
                if (!expectBad) failures.add("decode $path ${e.message}")
            } catch (e: Throwable) {
                failures.add("crash $path ${e.javaClass.simpleName} ${e.message}")
            }
            if (failures.size >= 20) break
        }
        if (failures.isNotEmpty()) {
            fail(failures.joinToString("\n") + "\nchecked ${files.size}")
        }
    }
}

private fun loadCatalog(raw: ByteArray): Catalog {
    val doc = decodeText(raw, Catalog())
    val cat = Catalog()
    for (id in doc.top) {
        if (ann0(doc, id) == "\$ion_shared_symbol_table" && doc.nodes[id].kind == K_STRUCT) {
            val nameId = field(doc, id, "name")
            if (nameId >= 0 && doc.nodes[nameId].kind == K_STRING) {
                var version = 1
                val ver = field(doc, id, "version")
                if (ver >= 0 && doc.nodes[ver].kind == K_INT) version = asSmall(doc, ver)
                val table = SharedTable(doc.textAt(nameId), version)
                val syms = field(doc, id, "symbols")
                if (syms >= 0 && doc.nodes[syms].kind == K_LIST) {
                    var k = 0
                    while (k < doc.nodes[syms].nchild) {
                        val el = doc.childAt(syms, k)
                        if (doc.nodes[el].kind == K_STRING) table.add(doc.textAt(el), false)
                        else table.add("", true)
                        k++
                    }
                }
                cat.add(table)
            }
        }
    }
    return cat
}

private fun ann0(doc: IonDoc, id: Int): String {
    val n = doc.nodes[id]
    if (n.annN == 0) return ""
    return doc.symText(doc.anns[n.ann])
}

private fun field(doc: IonDoc, id: Int, name: String): Int {
    var i = 0
    while (i < doc.nodes[id].nchild) {
        val sym = doc.fieldAt(id, i)
        val s = doc.syms[sym]
        if (s.text >= 0 && doc.texts[s.text] == name) return doc.childAt(id, i)
        i++
    }
    return -1
}

private fun asSmall(doc: IonDoc, id: Int): Int {
    val n = doc.nodes[id]
    if (n.kind != K_INT || n.b > 2 || n.c != 0) return 1
    var mag = 0L
    if (n.b > 0) mag = u32(doc.limbs[n.a])
    if (n.b > 1) mag = mag or (u32(doc.limbs[n.a + 1]) shl 32)
    return mag.toInt()
}

private fun parsePath(file: File, cat: Catalog): IonDoc {
    val data = file.readBytes()
    return if (file.name.endsWith(".10n")) decodeBinary(data, cat) else decodeText(data, cat)
}

private fun embedded(doc: IonDoc, id: Int): Boolean {
    val n = doc.nodes[id]
    if (n.annN == 0) return false
    return doc.symText(doc.anns[n.ann]) == "embedded_documents"
}

private fun seqOk(doc: IonDoc, id: Int, wantEq: Boolean, cat: Catalog): Boolean {
    val n = doc.nodes[id]
    if (n.kind != K_LIST && n.kind != K_SEXP) return false
    if (n.nchild < 2) return false
    if (embedded(doc, id)) {
        var first: IonDoc? = null
        var i = 0
        while (i < n.nchild) {
            val el = doc.childAt(id, i)
            if (doc.nodes[el].kind != K_STRING) return false
            val got = decodeText(doc.textAt(el).toByteArray(Charsets.UTF_8), cat)
            if (i == 0) first = got
            else {
                val eq = docsEq(first!!, got)
                if (wantEq && !eq) return false
                if (!wantEq && eq) return false
            }
            i++
        }
        return true
    }
    var i = 1
    while (i < n.nchild) {
        val eq = ionEq(doc, doc.childAt(id, 0), doc, doc.childAt(id, i))
        if (wantEq && !eq) return false
        if (!wantEq && eq) return false
        i++
    }
    if (!wantEq) {
        var a = 0
        while (a < n.nchild) {
            var b = a + 1
            while (b < n.nchild) {
                if (ionEq(doc, doc.childAt(id, a), doc, doc.childAt(id, b))) return false
                b++
            }
            a++
        }
    }
    return true
}
