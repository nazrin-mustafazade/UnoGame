package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// This class is a custom renderer for a JList that is used to display a leaderboard.
public class CustomLeaderboardRenderer extends DefaultListCellRenderer {

    // This method is overridden to customize the appearance of each cell in the JList.
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        // Call the superclass's method to get a component that can be customized.
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        // Make the label opaque so that we can change its background.
        label.setOpaque(true);
        // Set the font of the label.
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        // Add some padding to the label.
        label.setBorder(new EmptyBorder(5, 10, 5, 10));

        // If the cell is selected, change its background and text color.
        if (isSelected) {
            // Use a blue color for the background of the selected item.
            label.setBackground(new Color(0x2196F3));
            // Use white for the text color of the selected item.
            label.setForeground(Color.WHITE);
        } else {
            // If the cell is not selected, use white for the background and black for the text.
            label.setBackground(Color.WHITE);
            label.setForeground(Color.BLACK);
        }

        // Return the customized label.
        return label;
    }
}