# Cisco Catalyst Configuration Trim Helper

## About
This program will extract the configuration of non-standard GigabitEthernet interfaces from the running configuration of a Cisco Catalyst switch and write this configuration to a new file. Trunk configurations are extracted separately and written to a separate file.

## Installation and Use
This program is compatible with macOS (10.15 or higher) and Windows (8.1 or newer, including Windows Server) devices that have version 17+ of the OpenJDK Runtime Environment installed. This can be downloaded and installed from [here](https://adoptium.net).

To run the program, download TrimConfig.jar from the Releases section. Then, run the following in Terminal or the Command Prompt:
1. ```cd path/to/folder/with/TrimConfig.jar```
2. ```java -jar TrimConfig.jar [path to input file] [path to output file] [building default VLAN]```

For example: ```java -jar TrimConfig.jar ~/Downloads/input.txt ~/Downloads/output.txt 1234```

## "Could not find or load main class" Fix
On certain computer configurations, you may receive this error when trying to run the program. To fix this, you will need to build the program manually. To do so:
1. Download the **source code**.
2. Copy ```TrimConfig.java``` from ```its-inf-config_trim-main/src``` into another folder.
3. In Terminal, run the following:
    ```
    cd path/to/folder/with/TrimConfig.java
    
    # Compile and generate the TrimConfig.class file
    javac TrimConfig.java
    
    # Create a new JAR file containing TrimConfig.class/.java with main class name TrimConfig
    jar cvfe TrimConfig.jar TrimConfig *.class *.java
    ```
8. Run the program by running ```java -jar TrimConfig.jar --version```. This should output ```TrimConfig 1.1.3 built 06/23/2023```.

## Disclaimer
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
