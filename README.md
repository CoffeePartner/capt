# capt

[ ![Download](https://api.bintray.com/packages/dieyi/maven/capt-api/images/download.svg) ](https://bintray.com/dieyi/maven/capt-api/_latestVersion) [![Build Status](https://travis-ci.org/CoffeePartner/capt.svg?branch=master)](https://travis-ci.org/CoffeePartner/capt)

Capt is short for Class Annotation Processor Tool on Android.

Like apt, capt provide some mechanism to parse annotations at compile time. 

But capt can do more stuff than apt, because capt visit every class that will packing into APK, and apt only visit java sources.

Further more, capt also provides the chance to update every original class.

For more information please see the [wiki](https://github.com/CoffeePartner/capt/wiki).

Hope you enjoy it!

## Getting Started

##### Add capt plugin to gradle script classpath:

```groovy
classpath 'coffeepartner.capt:plugin:1.0.0-RC2'
```

##### Apply capt plugin on Android application or library module:

```gradle
apply plugin: 'com.android.application' 
// or apply plugin: 'com.android.library'
apply plugin: 'capt'
```

##### Add plugins for capt

```groovy
dependencies {
    capt 'xxx:xxx:x.y.z'
    capt files('xxx')
    capt project(":xxx") // java library and android library both supported
    debugCapt 'xxx'
    androidTestCapt 'xxx'
}

capt {
    plugins {
        pluginOfCapt {
            // arguments for pluginOfCapt
        }
    }
}
```

## Documentation

* User guide: This guide contains examples on how to use capt in your gradle script.
* [Writing Capt Plugins](https://github.com/CoffeePartner/capt/wiki): This guide introduce how to develop a plugin of capt.
* Design document: This document discusses issues we faced while designing capt.

 ## Releases Plan
 
-  - **03/01/2019: Release Candidate 1: stabilized API and feature set**
-  - **17/01/2019: Release Candidate 2: addressing feedback from RC 1**
   - 31/01/2019: Stable Release: General availability

## License

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


