package ecostream;

import java.awt.*;
import java.net.URL;
import javax.swing.ImageIcon;

public class Bin {
    private int x, y;
    private int width, height;
    private String type;
    private Image image;
    private boolean selected = false;

    public Bin(String type, int x, int y, int width, int height) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        String path = "/Pictures/bins/" + type.toLowerCase() + "_bin.png";
        URL url = getClass().getResource(path);
        if (url != null) {
            this.image = new ImageIcon(url).getImage();
        } else {
            System.out.println("Bin image not found: " + path);
            this.image = null;
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

    
        if (selected) {
            Stroke oldStroke = g2.getStroke();

            
            g2.setColor(new Color(255, 215, 0, 70));
            g2.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawRoundRect(x - 8, y - 8, width + 16, height + 16, 26, 26);

      
            g2.setColor(new Color(255, 215, 0, 120));
            g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawRoundRect(x - 7, y - 7, width + 14, height + 14, 24, 24);

         
            g2.setColor(new Color(255, 215, 0));
            g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawRoundRect(x - 6, y - 6, width + 12, height + 12, 22, 22);

            g2.setStroke(oldStroke);
        }

      
        if (image == null) {
            g2.setColor(new Color(200, 200, 200));
            g2.fillRoundRect(x, y, width, height, 16, 16);
            g2.setColor(Color.BLACK);
            g2.drawRoundRect(x, y, width, height, 16, 16);
            g2.drawString(type.toUpperCase(), x + 10, y + 20);
            return;
        }

    
        int iw = image.getWidth(null);
        int ih = image.getHeight(null);
        if (iw <= 0 || ih <= 0) return;

        int pad = 4;
        int boxW = width - pad * 2;
        int boxH = height - pad * 2;

        double scale = Math.min(boxW / (double) iw, boxH / (double) ih);
        int dw = (int) Math.round(iw * scale);
        int dh = (int) Math.round(ih * scale);

        int dx = x + pad + (boxW - dw) / 2;
        int dy = y + pad + (boxH - dh) / 2;

        g2.drawImage(image, dx, dy, dw, dh, null);
    }

    public String getType() { return type; }

    public void setSelected(boolean selected) { this.selected = selected; }
    public boolean isSelected() { return selected; }

    public int getX() { return x; }
    public int getY() { return y; }

 
    public void setBounds(int x, int y, int width, int height) {
        this.x = x; this.y = y;
        this.width = width; this.height = height;
    }
}
