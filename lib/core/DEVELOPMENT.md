# Development guide - Core crate

## Prerequisites
To compile the Core crate you will first need to install:
- [Protobuf](https://protobuf.dev/installation/)
- [Emscripten](https://emscripten.org/docs/getting_started/downloads.html) for compiling to Wasm
- [Firefox](https://mozilla.org/firefox/download/) for testing Wasm

```bash
brew install protobuf emscripten
```

On first usage you will need to run:
```bash
make init
```

## Testing
To test all targets run:
```bash
make test
```
This comprises the following make tasks:
```bash
make cargo-test wasm-test
```