package ecostream;

import java.awt.*;
import java.net.URL;
import java.util.Locale;
import java.util.Random;
import javax.swing.ImageIcon;

public abstract class Waste {

    protected int x, y;
    protected int width, height;
    protected int speed;
    protected WasteType type;
    protected Color color;

    protected Image img;

    protected int variant;
    protected String imageName;

    private static final Random RNG = new Random();

    public Waste(int x, int y, int width, int height, int speed, WasteType type, Color color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.type = type;
        this.color = color;

        int maxVariant = getMaxVariant(type);
        this.variant = 1 + RNG.nextInt(maxVariant);

       
        this.imageName = type.name().toLowerCase(Locale.ROOT) + "_" + variant + ".png";
    }

    private int getMaxVariant(WasteType t) {
        switch (t) {
            case PAPER:   return 3;
            case PLASTIC: return 3;
            case GLASS:   return 2; 
            case METAL:   return 3;
            case EWASTE:  return 3;
            default:      return 1;
        }
    }

    public void move() { x += speed; }

    public void draw(Graphics g) {
        if (img == null) {
            String path = "/Pictures/wastes/" + imageName;
            try {
                URL url = getClass().getResource(path);
                if (url != null) img = new ImageIcon(url).getImage();
                else System.out.println("Resim bulunamadı: " + imageName + " | path=" + path);
            } catch (Exception e) {
                System.out.println("Resim yükleme hatası: " + imageName + " | " + e.getMessage());
            }
        }

        if (img != null) {
            g.drawImage(img, x, y, width, height, null);
        } else {
            g.setColor(color);
            g.fillRoundRect(x, y, width, height, 12, 12);
            g.setColor(Color.BLACK);
            g.drawRoundRect(x, y, width, height, 12, 12);
            g.drawString(type.toString().substring(0, 1), x + width/2 - 4, y + height/2 + 5);
        }
    }

    public WasteType getType() { return type; }
    public int getX() { return x; }
    public void setX(int newX) {
    this.x = newX;
}

}
