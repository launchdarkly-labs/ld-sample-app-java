package dev.form;

import dev.component.TemperatureGauge;
import dev.swing.RoundPanel;
import javax.swing.*;
import java.awt.*;

public class Weather extends JPanel {
    private TemperatureGauge gauge;
    private JSlider slider;
    
    public Weather() {
        initComponents();
        setOpaque(false);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Create a round panel to hold the components
        RoundPanel mainPanel = new RoundPanel();
        mainPanel.setBackground(new Color(51, 51, 51));
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create title label
        JLabel titleLabel = new JLabel("Temperature Control");
        titleLabel.setFont(new Font("sansserif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(220, 220, 220));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Create center panel for gauge
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        centerPanel.setOpaque(false);
        
        // Create and configure the temperature gauge
        gauge = new TemperatureGauge();
        gauge.setPreferredSize(new Dimension(150, 400));
        centerPanel.add(gauge);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Create bottom panel for slider
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        
        // Create and configure the slider
        slider = new JSlider(JSlider.HORIZONTAL, 0, 120, 70);
        slider.setOpaque(false);
        slider.setForeground(new Color(220, 220, 220));
        slider.setMajorTickSpacing(20);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(e -> gauge.setTemperature(slider.getValue()));
        
        // Create slider label
        JLabel sliderLabel = new JLabel("Adjust Temperature");
        sliderLabel.setFont(new Font("sansserif", Font.BOLD, 14));
        sliderLabel.setForeground(new Color(220, 220, 220));
        sliderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        bottomPanel.add(sliderLabel, BorderLayout.NORTH);
        bottomPanel.add(slider, BorderLayout.CENTER);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Add the main panel to this panel
        add(mainPanel, BorderLayout.CENTER);
    }
}
