package ecostream;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class StartPanel extends JPanel implements ActionListener, KeyListener {

    private final Timer t = new Timer(30, this);
    private double phase = 0;

    private final Runnable onStart;
    private final Runnable onExit;

    private Rectangle btnStart, btnHow, btnExit;
    private int hover = -1;

    private final Image[] binImgs = new Image[5];

    public StartPanel(Runnable onStart, Runnable onExit) {
        this.onStart = onStart;
        this.onExit = onExit;

        setFocusable(true);
        addKeyListener(this);

        loadImages();
        t.start();

        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                int h = -1;
                if (btnStart != null && btnStart.contains(p)) h = 0;
                else if (btnHow != null && btnHow.contains(p)) h = 1;
                else if (btnExit != null && btnExit.contains(p)) h = 2;
                if (hover != h) { hover = h; repaint(); }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                if (btnStart != null && btnStart.contains(p)) onStart.run();
                else if (btnHow != null && btnHow.contains(p)) showHow();
                else if (btnExit != null && btnExit.contains(p)) onExit.run();
            }
            @Override public void mouseEntered(MouseEvent e) { requestFocusInWindow(); }
        });
    }

    private void loadImages() {
        binImgs[0] = load("/Pictures/bins/paper_bin.png");
        binImgs[1] = load("/Pictures/bins/plastic_bin.png");
        binImgs[2] = load("/Pictures/bins/glass_bin.png");
        binImgs[3] = load("/Pictures/bins/metal_bin.png");
        binImgs[4] = load("/Pictures/bins/ewaste_bin.png");
    }

    private Image load(String path) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) {
            System.out.println("Menu image not found: " + path);
            return null;
        }
        return new ImageIcon(url).getImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int W = getWidth(), H = getHeight();

        // gradient background
        g2.setPaint(new GradientPaint(0, 0, new Color(24, 40, 90), 0, H, new Color(20, 120, 90)));
        g2.fillRect(0, 0, W, H);

        // title
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 70));
        drawCentered(g2, "EcoStream", W, 115);

        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        g2.setColor(new Color(235, 245, 255));
        drawCentered(g2, "Recycling Sorting Game", W, 150);

        // animated bins row
        int rowY = 195, imgW = 130, gap = 35;
        int total = 5 * imgW + 4 * gap;
        int startX = (W - total) / 2;

        for (int i = 0; i < 5; i++) {
            int x = startX + i * (imgW + gap);
            int y = (int) (rowY + Math.sin(phase + i * 0.6) * 8);

            if (binImgs[i] != null) {
                drawImageContain(g2, binImgs[i], x, y, imgW, (int) (imgW * 1.2));
            } else {
                g2.setColor(new Color(255, 255, 255, 80));
                g2.fillRoundRect(x, y, imgW, (int)(imgW*1.2), 18, 18);
            }
        }

        // buttons
        int bw = 320, bh = 58;
        int bx = (W - bw) / 2;
        int by = 380;

        btnStart = new Rectangle(bx, by, bw, bh);
        btnHow   = new Rectangle(bx, by + 78, bw, bh);
        btnExit  = new Rectangle(bx, by + 156, bw, bh);

        drawButton(g2, btnStart, "START  (Enter)", hover == 0, true);
        drawButton(g2, btnHow,   "HOW TO PLAY  (H)", hover == 1, false);
        drawButton(g2, btnExit,  "EXIT  (Esc)", hover == 2, false);

        // footer
        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        g2.setColor(new Color(255, 255, 255, 200));
        drawCentered(g2, "←/→ Select • Space Sort • P Pause • R Restart", W, H - 40);
    }

    private void drawButton(Graphics2D g2, Rectangle r, String text, boolean isHover, boolean pulsing) {
        if (isHover || pulsing) {
            float a = pulsing ? (float)(0.35 + 0.20 * Math.sin(phase * 1.2)) : 0.35f;
            g2.setColor(new Color(255, 255, 255, (int)(255 * a)));
            g2.fillRoundRect(r.x - 6, r.y - 6, r.width + 12, r.height + 12, 22, 22);
        }

        g2.setColor(isHover ? new Color(255, 255, 255, 230) : new Color(255, 255, 255, 190));
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 22, 22);

        g2.setColor(new Color(0, 0, 0, 70));
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 22, 22);

        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.setColor(new Color(20, 30, 40));
        FontMetrics fm = g2.getFontMetrics();
        int tx = r.x + (r.width - fm.stringWidth(text)) / 2;
        int ty = r.y + (r.height + fm.getAscent()) / 2 - 3;
        g2.drawString(text, tx, ty);
    }

    private void drawCentered(Graphics2D g2, String s, int W, int y) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(s, (W - fm.stringWidth(s)) / 2, y);
    }

    private void drawImageContain(Graphics2D g2, Image img, int x, int y, int w, int h) {
        int iw = img.getWidth(null), ih = img.getHeight(null);
        if (iw <= 0 || ih <= 0) return;

        double scale = Math.min(w / (double) iw, h / (double) ih);
        int dw = (int) Math.round(iw * scale);
        int dh = (int) Math.round(ih * scale);

        int dx = x + (w - dw) / 2;
        int dy = y + (h - dh) / 2;

        g2.drawImage(img, dx, dy, dw, dh, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        phase += 0.06;
        repaint();
    }

    private void showHow() {
        JOptionPane.showMessageDialog(this,
                "HOW TO PLAY:\n\n" +
                "• ← / → : Select bin\n" +
                "• Space : Sort\n" +
                "• P : Pause\n" +
                "• R : Restart\n",
                "How to Play", JOptionPane.INFORMATION_MESSAGE);
        requestFocusInWindow();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_ENTER) onStart.run();
        else if (k == KeyEvent.VK_ESCAPE) onExit.run();
        else if (k == KeyEvent.VK_H) showHow();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
