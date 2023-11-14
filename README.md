# Cisco Catalyst Configuration Trim Helper

## Context
This program will extract non-standard configuration commands from the configuration of a Cisco Catalyst switch running IOS or IOS-XE ("trimming" the original file). This is useful in scenarios when you are setting up a new switch stack with a standardized/common port configuration and only want to apply the non-standard configuration commands from the old stack onto the new. 

For example, if you have the following standard port configuration on the new switch:
```
interface TenGigabitEthernet1/0/47
 switchport access vlan 123
 switchport mode access
 switchport nonegotiate
 switchport voice vlan 1234
 spanning-tree portfast
 spanning-tree bpduguard enable
!
end
```

and this configuration on the old switch:
```
interface GigabitEthernet1/0/47
 description Printer RM 123
 switchport access vlan 100
 switchport mode access
 switchport nonegotiate
 switchport voice vlan 1234
 power inline never
!
end
```

Then this program will extract the following from the old switch's configuration:
```
interface GigabitEthernet1/0/47
 description Printer RM 123
 switchport access vlan 100
 power inline never
!
end
```

## Installation and Use
This program is compatible with macOS (10.15 or higher) devices that have version 17+ of the OpenJDK Runtime Environment installed. This can be downloaded and installed from [here](https://adoptium.net). This program is NOT compatible with Windows systems. 

To run the program, download TrimConfig.jar from the Releases section. Then, run the following in Terminal or the Command Prompt:
1. ```cd path/to/folder/with/TrimConfig.jar```
2. ```java -jar TrimConfig.jar [--edit-config] [--delete-config] [--show-config] [--help]```

When prompted for the path to the input file, it is highly recommended to drag and drop the input file into the Terminal window instead of typing it in manually.

### Arguments
- The ```--edit-config``` argument allows you to edit the list of commands to exclude before continuing.
- The ```--delete-config``` argument will delete the file containing list of commands to exclude from your computer. On next run, you will be prompted to recreate the file.
- The ```--show-config``` argument will display the list of commands to exclude.

## "Could not find or load main class"
On certain computer configurations, you may receive this error when trying to run the program. To fix this, you will need to build the program manually. To do so:
1. Download the **source code**.
2. Copy ```*.java``` from ```its-inf-config_trim-main/src``` into another folder.
3. In Terminal, run the following:
    ```
    cd path/to/folder/with/*.java
    
    # Compile and generate the TrimConfig.class file
    javac *.java
    
    # Create a new JAR file containing TrimConfig.class/.java with main class name TrimConfig
    jar cvfe TrimConfig.jar Runner *.class *.java
    ```
8. The program should now be able to be run as described in the Installation and Use section. 

## Disclaimer
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
