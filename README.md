# gld-ion-kotlin

A from-scratch Amazon Ion implementation for Kotlin on the JVM.
The runtime and the code generator are Kotlin. They do not wrap, link, or
vendor ion-java, ion-c, ion-rust, or any other Ion library.

Maven coordinates, when an artifact is built from this tree:

`io.github.leo-gan:gld-ion-kotlin`

The Kotlin package is `ion`. The generator binary is `gld-iongen-kotlin`.

This repository is a standalone library. It is not part of any other project.

Documentation: [Why Ion](https://leo-gan.github.io/gld-ion-kotlin/why-ion/),
[Instructions](https://leo-gan.github.io/gld-ion-kotlin/instructions/),
[Examples](https://leo-gan.github.io/gld-ion-kotlin/examples/),
[Techniques](https://leo-gan.github.io/gld-ion-kotlin/techniques/),
[Test data](https://leo-gan.github.io/gld-ion-kotlin/test-data/).

## Build

Requires JDK 21.

```bash
git clone https://github.com/leo-gan/gld-ion-kotlin.git
cd gld-ion-kotlin
./gradlew test
```

`./gradlew installDist` writes `gld-iongen-kotlin` under `build/install`.

## What it reads and writes

| Surface | Behavior |
| --- | --- |
| Ion 1.0 text and binary | Decode the vendored ion-tests corpus. Encode compact text or length-prefixed binary. |
| Ion 1.1 draft | Little-endian values, inline symbols, delimited containers, directives, and template macros. |
| Numbers | Arbitrary-precision integers and decimals. Decimal scale is kept, so `1.0` and `1.00` stay distinct. |
| Timestamps | Precision from year through fractional seconds, plus a local offset or unknown `-00:00`. |
| Schema | JSON Schema objects and Ion Schema `type` definitions. `gld-iongen-kotlin` emits classes with `encodedLength`, `encode`, and `decode`. |

The encoder drops comments and whitespace. Two encodes of the same value match
when the options match. A catalog supplies shared symbol tables. Unknown
imported symbols stay unknown and compare by import name and import id.

## Layout

`src/main/kotlin/ion` is the public package: the document arena, the text and
binary codecs, symbol tables, and the facade `decode` / `encode`.
`ion.schema` and `ion.codegen` turn a schema into Kotlin. `testdata/` holds
the vendored corpus. `benches/` times a 32-struct document.

Requires **JDK 21**. Kotlin is **2.1** on Gradle 8.12.
