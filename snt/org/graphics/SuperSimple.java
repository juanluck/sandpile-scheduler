package org.graphics;
 
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
 
/**
 * @author Steffen Gates Jan 10, 2011
 */
public class SuperSimple {
 
    public static void main(String[] args) {
        new SuperSimple();
    }
    BufferedImage[] sprites;
    JFrame window;
    MyCanvas canvas;
    int boxSize = 10,
            width = 300,
            height = 400;
 
    public SuperSimple() {
        loadSprites();
        window = new JFrame();
        canvas = new MyCanvas();
        canvas.setPreferredSize(new Dimension(width, height));
        window.getContentPane().add(canvas);
        window.pack();
        window.setVisible(true);
    }
 
    private void loadSprites() {
        try {
            sprites = new BufferedImage[2];
            sprites[0] = ImageIO.read(new File("images/pacMan.png"));
            sprites[1] = ImageIO.read(new File("images/monster.png"));
        } catch (IOException ex) {
            Logger.getLogger(SuperSimple.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
 
    private class MyCanvas extends JPanel {
 
        @Override
        public void paintComponent(Graphics g) {
        	super.paintComponent(g);
            update(g);
        }
 
        public void update(Graphics g) {
            drawGrid(g);
            drawSprites(g);
        }
 
        private void drawGrid(Graphics g) {
            g.setColor(Color.BLACK);
            
 
            for (int i = 0; i < 160; i++) {
                g.drawLine(i * boxSize, 0, i * boxSize, height);
 
            }
            for (int i = 0; i < 210; i++) {
                g.drawLine(0, i * boxSize, width, i * boxSize);
            }
        }
 
        private void drawSprites(Graphics g) {
            g.drawImage(sprites[0], boxSize * 5, boxSize * 3, null);
            g.drawImage(sprites[1], boxSize * 2, boxSize * 1, null);
            g.drawImage(sprites[1], boxSize * 7, boxSize * 9, null);
        }
    }
}
