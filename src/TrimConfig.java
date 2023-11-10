public class TrimConfig {
    /**
     * Determine if program is being run on Mac or Windows. 
     *  
     * Test if config file already exists at:
     *      ~/Library/Application Support/TrimConfig/config.text    (mac)
     *      %localappdata%\TrimConfig\config.text                   (win)
     *      If yes: 
     *          Read config file to load in set of default commands.
     *      Else: 
     *          Prompt user to input default commands to exclude.
     *          Write this data to the location above.
     * 
     *      Prompt user for location of file to trim.
     *      
     * Read in the specified file to trim.
     * 
     *      For each line in the file:
     *          If the current line contains 'interface':
     *              Set per-interface configuration flag to true
     *              Add the current line to the map
     *              Set the current line as the interface marker
     *              While the current line does not contain 'interface':
     *                  Advance the current line to the next line
     *                  If the current line is not in the list of commands to exclude:
     *                      Add the current line to the map at the interface marker
     *  */            
}
