/* *********** Pledge of Honor *************************** *
I hereby certify that I have completed this lab assignment on my own
without any help from anyone else. I understand that the only sources of authorized
information in this lab assignment are (1) the course textbook, (2) the
materials posted at the course website and (3) any study notes handwritten by myself.
I have not used, accessed or received any information from any other unauthorized
source in taking this lab assignment. The effort in the assignment thus belongs
completely to me.

READ AND SIGN BY WRITING YOUR NAME SURNAME AND STUDENT ID
SIGNATURE: <Nazrin Mustafazade, 85269>

**********************************************************/

package main;

import view.LoginFrame;

import javax.swing.SwingUtilities;

/**
 * The Main class serves as the entry point for the application.
 * It creates and displays the LoginFrame on the Event Dispatch Thread (EDT).
 */
public class Main {
    /**
     * The main method is the entry point for the Java application.
     * It uses SwingUtilities.invokeLater to ensure that the GUI is created on the Event Dispatch Thread (EDT).
     * This is necessary because Swing is not thread-safe, and the EDT is the only thread that should interact with the GUI.
     *
     * @param args Command-line arguments. This application does not use command-line arguments.
     */
    public static void main(String[] args) {
        // Use SwingUtilities to ensure that the GUI is created in the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
