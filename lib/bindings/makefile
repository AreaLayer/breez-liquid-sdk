CURRENT_DIR := $(shell pwd)
SOURCES=$(sort $(wildcard ./src/*.rs ./src/**/*.rs))
SHELL := /bin/bash
OS_NAME=$(shell uname | tr '[:upper:]' '[:lower:]')
TARGET ?= aarch64-unknown-linux-gnu

.PHONY: init
init:
	rustup target add aarch64-apple-ios x86_64-apple-ios
	rustup target add aarch64-apple-darwin x86_64-apple-darwin
	rustup target add aarch64-apple-ios-sim
	rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android x86_64-linux-android
	rustup target add aarch64-unknown-linux-gnu x86_64-unknown-linux-gnu
	@if [ $$(uname) == "Darwin" ] ; then cargo install cargo-lipo ; fi
	cargo install cbindgen
	cargo install cargo-ndk

.PHONY: ndk-home
ndk-home:
	@if [ ! -d "${ANDROID_NDK_HOME}" ] ; then \
		echo "Error: Please, set the ANDROID_NDK_HOME env variable to point to your NDK folder" ; \
		exit 1 ; \
	fi

.PHONY: clean
clean:
	cargo clean
	rm -rf ffi
	rm -rf kmp

.PHONY: test
test:
	cargo test

all: bindings-swift bindings-android python-darwin react-native

## Android 
.PHONY: android
android: aarch64-linux-android armv7-linux-androideabi i686-linux-android x86_64-linux-android
	cargo install gobley-uniffi-bindgen --git https://github.com/breez/gobley --rev f920c275e4f60e22080a6cd280d9562d64eb6ac9
	gobley-uniffi-bindgen -c ./uniffi.toml -o ffi/kotlin src/breez_sdk_liquid.udl

aarch64-linux-android: $(SOURCES) ndk-home
	cargo ndk -t aarch64-linux-android -o ffi/kotlin/jniLibs build --release	
	cp -a $(ANDROID_NDK_HOME)/toolchains/llvm/prebuilt/$(OS_NAME)-x86_64/sysroot/usr/lib/aarch64-linux-android/libc++_shared.so ffi/kotlin/jniLibs/arm64-v8a/

armv7-linux-androideabi: $(SOURCES) ndk-home
	cargo ndk -t armv7-linux-androideabi -o ffi/kotlin/jniLibs build --release
	cp -a $(ANDROID_NDK_HOME)/toolchains/llvm/prebuilt/$(OS_NAME)-x86_64/sysroot/usr/lib/arm-linux-androideabi/libc++_shared.so ffi/kotlin/jniLibs/armeabi-v7a/

i686-linux-android: $(SOURCES) ndk-home
	cargo ndk -t i686-linux-android -o ffi/kotlin/jniLibs build --release
	cp -a $(ANDROID_NDK_HOME)/toolchains/llvm/prebuilt/$(OS_NAME)-x86_64/sysroot/usr/lib/i686-linux-android/libc++_shared.so ffi/kotlin/jniLibs/x86/

x86_64-linux-android: $(SOURCES) ndk-home
	cargo ndk -t x86_64-linux-android -o ffi/kotlin/jniLibs build --release
	cp -a $(ANDROID_NDK_HOME)/toolchains/llvm/prebuilt/$(OS_NAME)-x86_64/sysroot/usr/lib/x86_64-linux-android/libc++_shared.so ffi/kotlin/jniLibs/x86_64/

bindings-android: android
	cp -r ffi/kotlin/jniLibs langs/android/lib/src/main
	cp -r ffi/kotlin/main/kotlin/breez_sdk_liquid langs/android/lib/src/main/kotlin/
	cd langs/android && ./gradlew assemble --debug
	mkdir -p ffi/android
	cp langs/android/lib/build/outputs/aar/lib-release.aar ffi/android

## Kotlin
.PHONY: kotlin
kotlin: $(SOURCES)
	cargo build --release --target $(TARGET)
	cargo install gobley-uniffi-bindgen --git https://github.com/breez/gobley --rev f920c275e4f60e22080a6cd280d9562d64eb6ac9
	gobley-uniffi-bindgen -c ./uniffi.toml -o ffi/kotlin src/breez_sdk_liquid.udl

bindings-kotlin-multiplatform: ios-universal android
	cargo install gobley-uniffi-bindgen --git https://github.com/breez/gobley --rev f920c275e4f60e22080a6cd280d9562d64eb6ac9
	gobley-uniffi-bindgen -c ./uniffi.kotlin-multiplatform.toml -o ffi/kmp src/breez_sdk_liquid.udl

	mkdir -p langs/kotlin-multiplatform/breez-sdk-liquid-kmp/src/androidMain/kotlin/breez_sdk_liquid
	mkdir -p langs/kotlin-multiplatform/breez-sdk-liquid-kmp/src/commonMain/kotlin/breez_sdk_liquid
	mkdir -p langs/kotlin-multiplatform/breez-sdk-liquid-kmp/src/jvmMain/kotlin/breez_sdk_liquid
	mkdir -p langs/kotlin-multiplatform/breez-sdk-liquid-kmp/src/nativeMain/kotlin/breez_sdk_liquid
	
	cp -r ffi/kotlin/jniLibs/ langs/kotlin-multiplatform/breez-sdk-liquid-kmp/src/androidMain/jniLibs/
	cp -r ffi/kmp/nativeInterop langs/kotlin-multiplatform/breez-sdk-liquid-kmp/src/

	cp ffi/kmp/main/kotlin/breez_sdk_liquid/breez_sdk_liquid.android.kt langs/kotlin-multiplatform/breez-sdk-liquid-kmp/src/androidMain/kotlin/breez_sdk_liquid/
	cp ffi/kmp/main/kotlin/breez_sdk_liquid/breez_sdk_liquid.common.kt langs/kotlin-multiplatform/breez-sdk-liquid-kmp/src/commonMain/kotlin/breez_sdk_liquid/
	cp ffi/kmp/main/kotlin/breez_sdk_liquid/breez_sdk_liquid.jvm.kt langs/kotlin-multiplatform/breez-sdk-liquid-kmp/src/jvmMain/kotlin/breez_sdk_liquid/
	cp ffi/kmp/main/kotlin/breez_sdk_liquid/breez_sdk_liquid.native.kt langs/kotlin-multiplatform/breez-sdk-liquid-kmp/src/nativeMain/kotlin/breez_sdk_liquid/

	mkdir -p langs/kotlin-multiplatform/breez-sdk-liquid-kmp/src/lib/ios-arm64/
	mkdir -p langs/kotlin-multiplatform/breez-sdk-liquid-kmp/src/lib/ios-simulator-arm64/
	mkdir -p langs/kotlin-multiplatform/breez-sdk-liquid-kmp/src/lib/ios-simulator-x64/

	cp ../target/aarch64-apple-ios/release/libbreez_sdk_liquid_bindings.a langs/kotlin-multiplatform/breez-sdk-liquid-kmp/src/lib/ios-arm64/
	cp ../target/aarch64-apple-ios-sim/release/libbreez_sdk_liquid_bindings.a langs/kotlin-multiplatform/breez-sdk-liquid-kmp/src/lib/ios-simulator-arm64/
	cp ../target/x86_64-apple-ios/release/libbreez_sdk_liquid_bindings.a langs/kotlin-multiplatform/breez-sdk-liquid-kmp/src/lib/ios-simulator-x64/
	cd langs/kotlin-multiplatform && ./gradlew :breez-sdk-liquid-kmp:assemble

## Apple
ios-universal: $(SOURCES)		
	mkdir -p ../target/ios-universal/release
	mkdir -p ../target/ios-universal-sim/release
	cargo build --release --target aarch64-apple-ios ;\
	cargo build --release --target x86_64-apple-ios ;\
	cargo build --release --target aarch64-apple-ios-sim ;\
	# build universal lib for arm device and x86 sim
	lipo -create -output ../target/ios-universal/release/libbreez_sdk_liquid_bindings.a ../target/aarch64-apple-ios/release/libbreez_sdk_liquid_bindings.a ../target/x86_64-apple-ios/release/libbreez_sdk_liquid_bindings.a
	# build universal lib for arm sim and x86 sim
	lipo -create -output ../target/ios-universal-sim/release/libbreez_sdk_liquid_bindings.a ../target/aarch64-apple-ios-sim/release/libbreez_sdk_liquid_bindings.a ../target/x86_64-apple-ios/release/libbreez_sdk_liquid_bindings.a

darwin-universal: $(SOURCES)
	mkdir -p ../target/darwin-universal/release
	cargo lipo --release --targets aarch64-apple-darwin
	cargo lipo --release --targets x86_64-apple-darwin
	lipo -create -output ../target/darwin-universal/release/libbreez_sdk_liquid_bindings.dylib ../target/aarch64-apple-darwin/release/libbreez_sdk_liquid_bindings.dylib ../target/x86_64-apple-darwin/release/libbreez_sdk_liquid_bindings.dylib
	lipo -create -output ../target/darwin-universal/release/libbreez_sdk_liquid_bindings.a ../target/aarch64-apple-darwin/release/libbreez_sdk_liquid_bindings.a ../target/x86_64-apple-darwin/release/libbreez_sdk_liquid_bindings.a

## Swift
swift-ios: ios-universal
	cargo run --bin uniffi-bindgen generate src/breez_sdk_liquid.udl -l swift -o ffi/swift-ios
	cp ../target/ios-universal/release/libbreez_sdk_liquid_bindings.a ffi/swift-ios
	cd ffi/swift-ios && "swiftc" "-emit-module" "-module-name" "breez_sdk_liquid_bindings" "-Xcc" "-fmodule-map-file=$(CURRENT_DIR)/ffi/swift-ios/breez_sdk_liquidFFI.modulemap" "-I" "."  "-L" "." "-lbreez_sdk_liquid_bindings" breez_sdk_liquid.swift

swift-darwin: darwin-universal
	cargo run --bin uniffi-bindgen generate src/breez_sdk_liquid.udl -l swift -o ffi/swift-darwin
	cp ../target/darwin-universal/release/libbreez_sdk_liquid_bindings.dylib ffi/swift-darwin
	cd ffi/swift-darwin && "swiftc" "-emit-module" "-module-name" "breez_sdk_liquid_bindings" "-Xcc" "-fmodule-map-file=$(CURRENT_DIR)/ffi/swift-darwin/breez_sdk_liquidFFI.modulemap" "-I" "."  "-L" "." "-lbreez_sdk_liquid_bindings" breez_sdk_liquid.swift

bindings-swift: ios-universal darwin-universal build-ios-framework

build-ios-framework:
	mkdir -p langs/swift/Sources/BreezSDKLiquid
	cargo run --bin uniffi-bindgen generate src/breez_sdk_liquid.udl --no-format --language swift -o langs/swift/Sources/BreezSDKLiquid
	mv langs/swift/Sources/BreezSDKLiquid/breez_sdk_liquid.swift langs/swift/Sources/BreezSDKLiquid/BreezSDKLiquid.swift
	cp langs/swift/Sources/BreezSDKLiquid/breez_sdk_liquidFFI.h langs/swift/breez_sdk_liquidFFI.xcframework/ios-arm64/breez_sdk_liquidFFI.framework/Headers
	cp langs/swift/Sources/BreezSDKLiquid/breez_sdk_liquidFFI.h langs/swift/breez_sdk_liquidFFI.xcframework/ios-arm64_x86_64-simulator/breez_sdk_liquidFFI.framework/Headers
	cp langs/swift/Sources/BreezSDKLiquid/breez_sdk_liquidFFI.h langs/swift/breez_sdk_liquidFFI.xcframework/macos-arm64_x86_64/breez_sdk_liquidFFI.framework/Headers
	cp ../target/aarch64-apple-ios/release/libbreez_sdk_liquid_bindings.a langs/swift/breez_sdk_liquidFFI.xcframework/ios-arm64/breez_sdk_liquidFFI.framework/breez_sdk_liquidFFI
	cp ../target/ios-universal-sim/release/libbreez_sdk_liquid_bindings.a langs/swift/breez_sdk_liquidFFI.xcframework/ios-arm64_x86_64-simulator/breez_sdk_liquidFFI.framework/breez_sdk_liquidFFI
	cp ../target/darwin-universal/release/libbreez_sdk_liquid_bindings.a langs/swift/breez_sdk_liquidFFI.xcframework/macos-arm64_x86_64/breez_sdk_liquidFFI.framework/breez_sdk_liquidFFI
	cp langs/swift/Sources/BreezSDKLiquid/breez_sdk_liquidFFI.h langs/flutter/breez_sdk_liquidFFI/include/
	rm langs/swift/Sources/BreezSDKLiquid/breez_sdk_liquidFFI.h
	rm langs/swift/Sources/BreezSDKLiquid/breez_sdk_liquidFFI.modulemap

csharp-darwin: darwin-universal
	cargo install uniffi-bindgen-cs --git https://github.com/NordSecurity/uniffi-bindgen-cs --tag v0.9.1+v0.28.3
	uniffi-bindgen-cs src/breez_sdk_liquid.udl -o ffi/csharp -c ./uniffi.toml
	cp ../target/darwin-universal/release/libbreez_sdk_liquid_bindings.dylib ffi/csharp

csharp-linux: $(SOURCES)
	cargo install uniffi-bindgen-cs --git https://github.com/NordSecurity/uniffi-bindgen-cs --tag v0.9.1+v0.28.3
	cargo build --release --target $(TARGET)
	uniffi-bindgen-cs src/breez_sdk_liquid.udl -o ffi/csharp -c ./uniffi.toml
	cp ../target/$(TARGET)/release/libbreez_sdk_liquid_bindings.so ffi/csharp

## Go
golang-darwin: darwin-universal
	cargo install uniffi-bindgen-go --git https://github.com/NordSecurity/uniffi-bindgen-go --tag v0.3.0+v0.28.3
	uniffi-bindgen-go src/breez_sdk_liquid.udl -o ffi/golang -c ./uniffi.toml
	cp ../target/darwin-universal/release/libbreez_sdk_liquid_bindings.dylib ffi/golang
	cp -r ffi/golang/breez_sdk_liquid tests/bindings/golang/

golang-linux: $(SOURCES)
	cargo install uniffi-bindgen-go --git https://github.com/NordSecurity/uniffi-bindgen-go --tag v0.3.0+v0.28.3
	cargo build --release --target $(TARGET)
	uniffi-bindgen-go src/breez_sdk_liquid.udl -o ffi/golang -c ./uniffi.toml
	cp ../target/$(TARGET)/release/libbreez_sdk_liquid_bindings.so ffi/golang
	cp -r ffi/golang/breez_sdk_liquid tests/bindings/golang/

## Python
python-linux: $(SOURCES)
	cargo build --release --target $(TARGET)
	cargo run --bin uniffi-bindgen generate src/breez_sdk_liquid.udl --no-format --language python -o ffi/python
	cp ../target/$(TARGET)/release/libbreez_sdk_liquid_bindings.so ffi/python

python-darwin: darwin-universal
	cargo run --bin uniffi-bindgen generate src/breez_sdk_liquid.udl --no-format --language python -o ffi/python
	cp ../target/darwin-universal/release/libbreez_sdk_liquid_bindings.dylib ffi/python

## React Native
.PHONY: react-native
react-native:
	make -C langs/react-native codegen