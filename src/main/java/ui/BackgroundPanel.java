package ui;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(String imagePath) {
        loadBackgroundImage(imagePath);
    }

    private void loadBackgroundImage(String imagePath) {
        try {
            // Try loading from file system first
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                backgroundImage = ImageIO.read(imageFile);
            } else {
                // Try loading from resources folder
                InputStream imageStream = getClass().getResourceAsStream("/" + imagePath);
                if (imageStream != null) {
                    backgroundImage = ImageIO.read(imageStream);
                } else {
                    System.out.println("Background image not found: " + imagePath);
                    // Set a default color background as fallback
                    setBackground(new Color(45, 45, 45)); // Dark gray fallback
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading background image: " + e.getMessage());
            setBackground(new Color(45, 45, 45)); // Dark gray fallback
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            // Draw background image stretched to fill the entire panel
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}