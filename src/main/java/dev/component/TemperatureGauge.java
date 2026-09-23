package dev.component;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class TemperatureGauge extends JPanel {
    private double temperature;
    private final int MIN_TEMP = 0;
    private final int MAX_TEMP = 120;
    private final Color MERCURY_COLOR = new Color(220, 20, 60);
    private final Color GLASS_COLOR = new Color(220, 220, 220, 50);
    private final Color MARKING_COLOR = new Color(220, 220, 220);

    public TemperatureGauge() {
        setPreferredSize(new Dimension(600, 1000));
        setBackground(new Color(51, 51, 51));  // Match app's dark theme
        temperature = 70; // Default temperature
    }

    public void setTemperature(double temp) {
        temperature = Math.max(MIN_TEMP, Math.min(MAX_TEMP, temp));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int bulbDiameter = 60;
        int stemWidth = 40;
        int stemHeight = h - bulbDiameter - 100;  // Adjust spacing for smaller bulb
        int x = w * 2/3;  // Move thermometer to right third
        int y = 30;  // Space at top

        // Draw temperature markings first (so they're behind the tube)
        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        int markingLength = 20;
        for (int temp = MIN_TEMP; temp <= MAX_TEMP; temp += 10) {
            int markY = getYForTemperature(temp, y, stemHeight);
            // Draw longer lines and labels for multiples of 20
            if (temp % 20 == 0) {
                g2.setStroke(new BasicStroke(3));
                String tempStr = String.valueOf(temp);
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(tempStr);
                // Draw temperature on left side, aligned with the marking
                g2.setColor(MARKING_COLOR);
                g2.drawString(tempStr + "°F", x - stemWidth/2 - markingLength - textWidth - 35, markY + 5);
                // Draw tick marks
                g2.draw(new Line2D.Double(x - stemWidth/2 - markingLength, markY, x - stemWidth/2, markY));
                g2.draw(new Line2D.Double(x + stemWidth/2, markY, x + stemWidth/2 + markingLength, markY));
            } else {
                // Shorter lines for other markings
                g2.setStroke(new BasicStroke(2));
                g2.draw(new Line2D.Double(x - stemWidth/2 - markingLength/2, markY, x - stemWidth/2, markY));
                g2.draw(new Line2D.Double(x + stemWidth/2, markY, x + stemWidth/2 + markingLength/2, markY));
            }
        }

        // Draw glass tube (stem)
        g2.setColor(GLASS_COLOR);
        g2.setStroke(new BasicStroke(4));
        g2.fill(new RoundRectangle2D.Double(x - stemWidth/2, y, stemWidth, stemHeight, 20, 20));
        g2.setColor(MARKING_COLOR);
        g2.draw(new RoundRectangle2D.Double(x - stemWidth/2, y, stemWidth, stemHeight, 20, 20));

        // Calculate mercury height
        int mercuryHeight = (int)((temperature - MIN_TEMP) * stemHeight / (MAX_TEMP - MIN_TEMP));
        int mercuryTop = y + stemHeight - mercuryHeight;

        // Draw mercury in stem
        g2.setColor(MERCURY_COLOR);
        g2.fill(new RoundRectangle2D.Double(x - stemWidth/2 + 4, mercuryTop, stemWidth - 8, mercuryHeight, 10, 10));

        // Draw bulb
        int bulbX = x - bulbDiameter/2;
        int bulbY = h - bulbDiameter - 60;
        g2.setColor(MERCURY_COLOR);
        g2.fill(new Ellipse2D.Double(bulbX, bulbY, bulbDiameter, bulbDiameter));
        g2.setColor(MARKING_COLOR);
        g2.setStroke(new BasicStroke(4));
        g2.draw(new Ellipse2D.Double(bulbX, bulbY, bulbDiameter, bulbDiameter));

        // Add highlight to bulb for 3D effect
        g2.setColor(new Color(255, 255, 255, 50));
        g2.fill(new Ellipse2D.Double(bulbX + bulbDiameter/4, bulbY + bulbDiameter/4, 
                                   bulbDiameter/4, bulbDiameter/4));

        // Draw current temperature
        g2.setColor(MARKING_COLOR);
        g2.setFont(new Font("Arial", Font.BOLD, 36));
        String tempStr = String.format("%.1f°F", temperature);
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(tempStr);
        g2.drawString(tempStr, x - textWidth/2, h - 20);
    }

    private int getYForTemperature(int temp, int topY, int height) {
        return topY + height - (int)((temp - MIN_TEMP) * height / (MAX_TEMP - MIN_TEMP));
    }
}
