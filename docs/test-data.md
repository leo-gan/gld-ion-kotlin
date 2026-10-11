# Test data

The tests are split into a vendored corpus and a few local schemas. Nothing in
this tree is named a fixture.

## ion-tests

`testdata/ion-tests` is a copy of the upstream
[amazon-ion/ion-tests](https://github.com/amazon-ion/ion-tests) data files.
Those files keep their Apache-2.0 license. `testdata/ion-tests/LICENSE` and
`NOTICE` are the upstream texts. The library's own code stays MIT.

| Directory | What a passing run means |
| --- | --- |
| `iontestdata/good` | The file decodes. |
| `iontestdata/bad` | The file raises `DecodeError`. |
| `iontestdata/good/equivs` | Each top-level group contains equivalent values. `embedded_documents` groups are parsed again before the comparison. |
| `iontestdata/good/non-equivs` | Values in a group are pairwise not equivalent. |
| `catalog` | Shared symbol tables the suite loads before each file. |

`./gradlew test` runs `IonSuiteTest` on every `.ion` and `.10n` file. The
current copy has 785 files. The expected result is zero failures.

The corpus covers Ion 1.0. Ion 1.1 behavior is checked by `Ion11Test`: the
binary version marker, a delimited container, inline symbols, a negative-zero
decimal, and text macros.

## Schemas

| File | What it generates |
| --- | --- |
| `testdata/schema/message.json` | `Message`, a closed JSON Schema object with Ion extensions for timestamp and blob. |
| `testdata/schema/point.ion` | `Point`, an Ion Schema struct with an optional int and a symbol. |

`./scripts/check-generated.sh` writes those files again and diffs them against
`src/test/kotlin/ion/generated`. A drift fails the check.

`GeneratedTest` fills a `Message` and a `Point`, encodes them, and decodes
them back. The timestamp `2023T` and the symbol `foo` must come back unchanged.
