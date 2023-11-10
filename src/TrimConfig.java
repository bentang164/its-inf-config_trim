public class TrimConfig {
    /**
     * Test if config file already exists at: ~/Library/Application Support/TrimConfig/config.text
     *      If yes: 
     *          Read config file to load in set of default commands and input in list.
     *          Prompt user to input building default VLAN.
     *          Add 'switchport access vlan [building VLAN]' to list of default commands.
     *      Else: 
     *          Prompt user to input default commands to exclude and store in a temporary list.
     *          If the user's input contains 'spanning-tree portfast':
     *              Add 'spanning-tree portfast' to the list
     *              Add 'spanning-tree portfast edge' to the list
     *          Write header data to the location above.
     *          Write this data to the location above.
     * 
     *      Prompt user for location of file to trim.
     *      
     * Read in the specified file to trim.
     * 
     *      While the current line does not contain 'interface vlan':
     *          If the update marker is true:
     *              Advance the current line to the next line
     *          
     *          If the current line contains 'interface':
     *              Update the interface marker to the current line
     *              While the current line does not contain 'interface':
     *                  Read in the next line
     *                  For each command in the list of commands to exclude:
     *                      If the current line matches on a command:
     *                          Update the exclude marker to true 
     *                  If the exclude marker is false:
     *                      Add the current line into a list of commands to keep
     *              
     *              Add the interface marker as the key into the map, and the list of commands as the value
     *              Set the update marker to false so the line is not advanced
     *  */  
}
