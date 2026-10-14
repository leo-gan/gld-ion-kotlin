# Instructions

These steps assume a machine with JDK 21. The Gradle project is
`gld-ion-kotlin`. The Kotlin package is `ion`.

## Install

```kotlin
dependencies {
    implementation("io.github.leo-gan:gld-ion-kotlin:0.2.0")
}
```

The Maven coordinate is `io.github.leo-gan:gld-ion-kotlin:0.2.0`.

## Build this repository

```bash
git clone https://github.com/leo-gan/gld-ion-kotlin.git
cd gld-ion-kotlin
./gradlew test
```

The wrapper uses Gradle 8.12 and Kotlin 2.1. `JAVA_HOME` must point at JDK 21.

| Task | What it runs |
| --- | --- |
| `./gradlew test` | Unit tests, then every file in the vendored ion-tests corpus. |
| `./scripts/check-generated.sh` | Regenerates schema classes and diffs them. |
| `./gradlew installDist` | Writes the `gld-iongen-kotlin` script. |
| `./scripts/run-microbench.sh` | Prints nanoseconds per encode and decode of a 32-struct document. |

## Decode and encode

```kotlin
import ion.Catalog
import ion.EncodeOptions
import ion.decode
import ion.encode

fun main() {
    val cat = Catalog()
    val doc = decode("{a:1}\n".toByteArray(), cat)
    val raw = encode(doc, EncodeOptions(binary = true, pretty = false, version = 10), cat)
    println(raw.size)
}
```

`EncodeOptions` fields:

| Field | Meaning |
| --- | --- |
| `binary` | When true, `encode` writes binary Ion. Otherwise it writes UTF-8 text. |
| `pretty` | Indent text. Binary ignores it. |
| `version` | `10` for Ion 1.0, `11` for the Ion 1.1 draft. |
| `addImport(name, version)` | Shared table the Ion 1.0 binary writer imports. |

A leading `E0 01` version marker selects the binary decoder. Any other byte
string is text. The text decoder accepts UTF-8 and transcodes UTF-16 or UTF-32
when a wide encoding is obvious from the first bytes.

## Generate Kotlin

A JSON Schema file:

```bash
gld-iongen-kotlin --schema testdata/schema/message.json --out src/test/kotlin/ion/generated
```

An Ion Schema file is Ion text:

```bash
gld-iongen-kotlin --schema testdata/schema/point.ion --out src/test/kotlin/ion/generated
```

The generator writes one `.kt` file per struct, in `package ion.generated`.
Compile generated code against this library so `ion` resolves.

## Document the package

```bash
pip install -r requirements-docs.txt
mkdocs build --strict
```

The site is Material for MkDocs with an indigo primary color and a teal accent.
