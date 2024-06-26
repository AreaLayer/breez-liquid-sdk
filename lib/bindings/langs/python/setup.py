#!/usr/bin/env python

from setuptools import setup

LONG_DESCRIPTION = """# Breez Liquid SDK
Python language bindings for the [Breez Liquid SDK](https://github.com/breez/breez-liquid-sdk).

## Installing

```shell
pip install breez_liquid_sdk
```
"""

setup(
    name="breez_liquid_sdk",
    version="0.2.7.dev9",
    description="Python language bindings for the Breez Liquid SDK",
    long_description=LONG_DESCRIPTION,
    long_description_content_type="text/markdown",
    packages=["breez_liquid_sdk"],
    package_dir={"breez_liquid_sdk": "./src/breez_liquid_sdk"},
    include_package_data=True,
    package_data={"breez_liquid_sdk": ["*.dylib", "*.so", "*.dll"]},
    url="https://github.com/breez/breez-liquid-sdk",
    author="Breez <contact@breez.technology>",
    license="MIT",
    has_ext_modules=lambda: True,
)
