# Description

This bundle contains a set of [Joget](https://www.joget.org) plugins to query and update a [Hyperledger Fabric](https://www.hyperledger.org/use/fabric) blockchain network. For detailed instructions and an introduction to Hyperledger Fabric, please refer to the article [Blockchain Made Simple: No-Code Apps with Hyperledger Fabric and Joget](https://blog.joget.org/2019/01/blockchain-made-simple-no-code-apps.html).


# Installation

## Hyperledger Fabric 2.0

To test and build, install Hyperledger Fabric v2.0 and the Fabcar Network Sample. 

1. Install Prerequisites (https://hyperledger-fabric.readthedocs.io/en/release-2.0/prereqs.html)
1. Install Hyperledger Fabric Samples, Binaries and Docker Images (https://hyperledger-fabric.readthedocs.io/en/release-2.0/install.html)
1. Setup Sample Fabcar Network (https://hyperledger-fabric.readthedocs.io/en/release-2.0/write_first_app.html)
1. Modify the configuration property values in the testFabricTool method in /src/java/org/joget/hyperledger/TestFabric.java.
1. Build the project and upload the plugin JAR to Joget.
1. Import the Joget Fabcar sample app and modify the fabric_ environment variables. 

## IBM Blockchain Platform

1. Setup the Fabcar Blockchain Sample on IBM Blockchain Platform https://github.com/IBM/fabcar-blockchain-sample 
1. Modify the configuration property values in the testFabricTool method in /src/java/org/joget/hyperledger/TestFabric.java.
1. Build the project and upload the plugin JAR to Joget.
1. Import the Joget Fabcar sample app and modify the fabric_ environment variables. 

# Getting Help

JogetOSS is a community-led team for open source software related to the [Joget](https://github.com/jogetworkflow/jw-community) no-code/low-code application platform.
Projects under JogetOSS are community-driven and community-supported.
To obtain support, ask questions, get answers and help others, please participate in the [Community Q&A](https://answers.joget.org/).

# Contributing

This project welcomes contributions and suggestions, please open an issue or create a pull request.

Please note that all interactions fall under our [Code of Conduct](CODE_OF_CONDUCT.md).

# Licensing

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

NOTE: This software may depend on other packages that may be licensed under different open source licenses.
