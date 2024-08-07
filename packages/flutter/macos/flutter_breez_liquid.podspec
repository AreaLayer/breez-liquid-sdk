version = '0.2.1' # generated; do not edit
tag_name = "v#{version}"
release_tag_name = "breez_liquid-#{tag_name}"

# We cannot distribute the XCFramework alongside the library directly,
# so we have to fetch the correct version here.
framework_name = 'breez_sdk_liquid.xcframework'
remote_zip_name = "#{framework_name}.zip"
url = "https://github.com/breez/breez-sdk-liquid-flutter/releases/download/#{tag_name}/#{remote_zip_name}"
local_zip_name = "#{release_tag_name}.zip"

Pod::Spec.new do |spec|
  spec.name          = 'flutter_breez_liquid'
  spec.version       = "#{version}"
  spec.license       = { :file => '../LICENSE' }
  spec.homepage      = 'https://breez.technology'
  spec.authors       = { 'Breez' => 'contact@breez.technology' }
  spec.summary       = 'iOS/macOS Flutter bindings for Breez Liquid'

  spec.source              = { :path => '.' }
  spec.source_files        = 'Classes/**/*'
  spec.public_header_files = 'Classes/**/*.h'
  spec.vendored_frameworks = "Frameworks/#{framework_name}"

  spec.prepare_command = <<-CMD
    cd Frameworks
    rm -rf #{framework_name}

    if [ ! -f #{local_zip_name} ]
    then
      wget #{url} -O #{local_zip_name} || curl -L #{url} -o #{local_zip_name}
    fi

    unzip #{local_zip_name}
    cd -
  CMD
  
  spec.ios.deployment_target = '12.0'
  spec.osx.deployment_target = '10.11'
end
