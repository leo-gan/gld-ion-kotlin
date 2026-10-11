# Examples

The dynamic API and the generated API write the same Ion. Use the dynamic API
when the shape is not known ahead of time. Use a generated class when a schema
already names the fields.

## Dynamic document

`examples/EncodeValue.kt` decodes one struct and writes it twice.

```kotlin
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
```

Ion 1.0 text starts with `$ion_1_0`. Ion 1.1 binary starts with `E0 01 01 EA`
and stores the field name `a` as inline symbol text, so the stream has no
symbol table.

## Build a value without parsing

```kotlin
import ion.IonDoc

fun main() {
    val doc = IonDoc()
    val st = doc.startContainer(13)
    val field = doc.addSymbolText("n")
    doc.addChild(st, doc.addI64(150), doc.nodes[field].a)
    doc.addTop(st)
}
```

`13` is the struct kind. `addI64` accepts any `Long`. Wider integers go
through `addInt` and a `BigInt`. `addDecimal` keeps the coefficient and the
exponent, including a negative zero. `addTime` keeps precision and offset.

## Generated class

`testdata/schema/message.json` describes a closed object. After
`gld-iongen-kotlin`, `Message` has the fields below.

| Schema field | Kotlin field | Required |
| --- | --- | --- |
| `id` | `Long` | yes |
| `name` | `String` | yes |
| `ok` | `Boolean` | no |
| `weight` | `Double` | no |
| `tags` | `MutableList<String>` | no |
| `when` | `String` holding Ion timestamp text | no |
| `payload` | `ByteArray` | no |

```kotlin
import ion.EncodeOptions
import ion.generated.Message

fun main() {
    val msg = Message()
    msg.id = 7
    msg.name = "ada"
    msg.hasWhen = true
    msg.`when` = "2023T"
    val buf = msg.encode(EncodeOptions(binary = true, pretty = false, version = 10))
    val back = Message.decode(buf)
}
```

Optional fields use a `has` flag. A missing flag omits the field. A closed
schema raises `DecodeError.KIND_SCHEMA` when a required field is absent or an
unknown field is present.

`testdata/schema/point.ion` is Ion Schema. `label` is a symbol, so the
generated `String` holds Ion symbol text such as `foo`, not a quoted string.
`encode` with version `11` writes an Ion 1.1 binary struct.
