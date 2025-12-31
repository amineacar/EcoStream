package ecostream;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Random;
import javax.swing.*;

public class GamePanel extends JPanel implements ActionListener, KeyListener {

    
    private final ArrayList<Waste> wasteList = new ArrayList<>();
    private final Timer timer = new Timer(30, this);
    private final Random random = new Random();

    private int score = 0;

    private final int BELT_Y = 220;
    private final int SORTING_LINE_X = 300;

    private String feedbackMessage = "";
    private Color feedbackColor = Color.WHITE;
    private int feedbackTimer = 0;

    // Levels
    private int level = 1;
    private static final int LEVEL2_SCORE = 100;
    private static final int LEVEL3_SCORE = 250;

    //  Win / GameOver
    private static final int WIN_SCORE = 400;
    private static final int GAME_OVER_SCORE = -10;
    private boolean gameOver = false;
    private boolean gameWin = false;

    //  Pause
    private boolean paused = false;

    //  Level transition overlay
    private boolean inLevelTransition = false;
    private int transitionTicks = 0;
    private static final int TRANSITION_DURATION_TICKS = 60;
    private String transitionText = "";

    // flash
    private int flashTimer = 0;
    private int flashBinIndex = -1;
    private boolean flashCorrect = false;

    // layout cache
    private int lastLayoutWidth = -1;

    // ------- STATS ------
    private final EnumMap<WasteType, Integer> correctSortedCounts = new EnumMap<>(WasteType.class);
    private int totalCorrectSorted = 0;

    // --- BINS 
    private final ArrayList<Bin> bins = new ArrayList<>();
    private final WasteType[] binTypes = new WasteType[] {
            WasteType.PAPER, WasteType.PLASTIC, WasteType.GLASS, WasteType.METAL, WasteType.EWASTE
    };
    private int selectedBinIndex = 0;

    // bin geometry (for overlays/arrow/locked/flash)
    private final int[] bx = new int[5];
    private final int[] by = new int[5];
    private final int[] bw = new int[5];
    private final int[] bh = new int[5];

    public GamePanel() {
        setBackground(new Color(220, 220, 220));
        setFocusable(true);
        addKeyListener(this);

        initStats();
        initBins();

        timer.start();
    }

   
    private void initStats() {
        correctSortedCounts.put(WasteType.PAPER, 0);
        correctSortedCounts.put(WasteType.PLASTIC, 0);
        correctSortedCounts.put(WasteType.GLASS, 0);
        correctSortedCounts.put(WasteType.METAL, 0);
        correctSortedCounts.put(WasteType.EWASTE, 0);
        totalCorrectSorted = 0;
    }

    private void incrementCorrect(WasteType type) {
        if (!correctSortedCounts.containsKey(type)) return;
        correctSortedCounts.put(type, correctSortedCounts.get(type) + 1);
        totalCorrectSorted++;
    }

    private String percentText(int part, int total) {
        if (total <= 0) return "0%";
        double p = (part * 100.0) / total;
        return String.format("%.0f%%", p);
    }

    private Color typeColor(WasteType t) {
        switch (t) {
            case PAPER:   return new Color(60, 130, 255);
            case PLASTIC: return new Color(255, 160, 60);
            case GLASS:   return new Color(60, 190, 120);
            case METAL:   return new Color(160, 160, 160);
            case EWASTE:  return new Color(255, 80, 80);
            default:      return new Color(120, 120, 120);
        }
    }

    //  Win progress 
    private int winProgressValue() {
        return Math.max(0, Math.min(score, WIN_SCORE));
    }

    //------ LEVEL DIFFICULTY -----
    private int currentSpeed() {
        if (level == 1) return 4;
        if (level == 2) return 5;
        return 5;
    }

    private int currentSpawnChance() {
        if (level == 1) return 2;  // daha yavaş
        if (level == 2) return 4;
        return 5;
    }

    private int currentGap() {
        if (level == 1) return 130;
        if (level == 2) return 110;
        return 105;
    }

    private int unlockedCount() {
        if (level == 1) return 3; // paper, plastic, glass
        if (level == 2) return 4; // + metal
        return 5;                 // + ewaste
    }

    private int nextLevelScore() {
        if (level == 1) return LEVEL2_SCORE;
        if (level == 2) return LEVEL3_SCORE;
        return -1;
    }

    private void clampSelectedIndexToUnlocked() {
        int unlocked = unlockedCount();
        if (selectedBinIndex < 0) selectedBinIndex = 0;
        if (selectedBinIndex >= unlocked) selectedBinIndex = unlocked - 1;
        syncSelectedState();
    }

    private void updateLevelByScore() {
        int old = level;

        if (score >= LEVEL3_SCORE) level = 3;
        else if (score >= LEVEL2_SCORE) level = 2;
        else level = 1;

        if (level != old) {
            clampSelectedIndexToUnlocked();
            if (level > old) startLevelTransition(level);
        }
    }

    private void startLevelTransition(int newLevel) {
        inLevelTransition = true;
        transitionTicks = TRANSITION_DURATION_TICKS;
        transitionText = "LEVEL " + newLevel + "!";
        feedbackMessage = "New bin unlocked!";
        feedbackColor = new Color(0, 120, 255);
        feedbackTimer = 25;
    }

    private void checkEndConditions() {
        if (!gameOver && !gameWin) {
            if (score <= GAME_OVER_SCORE) gameOver = true;
            else if (level >= 3 && score >= WIN_SCORE) gameWin = true;
        }
    }

    private void resetGame() {
        wasteList.clear();
        score = 0;
        level = 1;
        selectedBinIndex = 0;

        feedbackMessage = "";
        feedbackTimer = 0;

        flashTimer = 0;
        flashBinIndex = -1;
        flashCorrect = false;

        inLevelTransition = false;
        transitionTicks = 0;
        transitionText = "";

        gameOver = false;
        gameWin = false;
        paused = false;

        initStats();
        clampSelectedIndexToUnlocked();
        repaint();
    }

   
    private void initBins() {
        bins.clear();
        // Bin.java 
        bins.add(new Bin("paper", 0, 0, 10, 10));
        bins.add(new Bin("plastic", 0, 0, 10, 10));
        bins.add(new Bin("glass", 0, 0, 10, 10));
        bins.add(new Bin("metal", 0, 0, 10, 10));
        bins.add(new Bin("ewaste", 0, 0, 10, 10));

        layoutBinsIfNeeded();
        clampSelectedIndexToUnlocked();
        syncSelectedState();
    }

    private void syncSelectedState() {
        for (int i = 0; i < bins.size(); i++) {
            bins.get(i).setSelected(i == selectedBinIndex);
        }
    }

    private void layoutBinsIfNeeded() {
        int w = getWidth();
        if (w <= 0) w = 1200;

        if (w == lastLayoutWidth) return;
        lastLayoutWidth = w;

        int count = bins.size();

        int sideMargin = 160;
        int available = Math.max(700, w - sideMargin);

        int gap = Math.max(32, available / 26);
        int binW = (available - gap * (count - 1)) / count;

   
        binW = Math.max(125, Math.min(binW, 170));
        int binH = (int) (binW * 1.25); 
        binH = Math.max(170, Math.min(binH, 215));

        int totalW = count * binW + (count - 1) * gap;
        int startX = (w - totalW) / 2;

        int y = 430;

        for (int i = 0; i < bins.size(); i++) {
            bx[i] = startX + i * (binW + gap);
            by[i] = y;
            bw[i] = binW;
            bh[i] = binH;

           
            bins.get(i).setBounds(bx[i], by[i], bw[i], bh[i]);
        }
    }

    // - DRAW --
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        layoutBinsIfNeeded();

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawConveyorBelt(g2);
        drawBins(g2);

        for (Waste w : wasteList) w.draw(g2);

        // UI
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 26));
        g2.drawString("Score: " + score, 20, 45);

        g2.setFont(new Font("Arial", Font.BOLD, 18));
        int next = nextLevelScore();
        String prog = (next == -1) ? "MAX LEVEL" : ("Next Level: " + next);
        g2.drawString("Level: " + level + "   |   " + prog, 20, 75);

        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        g2.drawString("Controls:  \u2190/\u2192 select  |  SPACE sort  |  P pause  |  R restart", 20, 100);

        if (feedbackTimer > 0) {
            g2.setFont(new Font("Arial", Font.BOLD, 30));
            g2.setColor(feedbackColor);
            g2.drawString(feedbackMessage, getWidth() / 2 - 200, BELT_Y - 35);
        }

      
        drawStatsPanelPretty(g2);

        // overlays
        if (inLevelTransition) drawCenterOverlay(g2, transitionText, "Get ready!");
        if (paused && !gameOver && !gameWin && !inLevelTransition) drawCenterOverlay(g2, "PAUSED", "Press P to Resume");

        if (gameOver || gameWin) {
            String title = gameWin ? "YOU WIN!" : "GAME OVER!";
            drawEndOverlay(g2, title, "Final Score: " + score, "Press R to Restart", gameWin);
        }
    }

    private void drawConveyorBelt(Graphics2D g2) {
        g2.setColor(new Color(50, 50, 50));
        g2.fillRect(0, BELT_Y, getWidth(), 90);

        g2.setColor(new Color(80, 80, 80));
        for (int i = 0; i < getWidth(); i += 60) {
            g2.drawLine(i, BELT_Y, i, BELT_Y + 90);
        }

        g2.setColor(Color.RED);
        g2.fillRect(SORTING_LINE_X, BELT_Y, 7, 90);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.drawString("SORTING LINE", SORTING_LINE_X - 45, BELT_Y - 6);
    }

    private void drawBins(Graphics2D g2) {
        int panelX = 80;
        int panelY = 390;
        int panelW = getWidth() - 160;

        int binH = bh[0] > 0 ? bh[0] : 200;
        int panelH = binH + 110;

        g2.setColor(new Color(240, 240, 240, 210));
        g2.fillRoundRect(panelX, panelY, panelW, panelH, 18, 18);
        g2.setColor(Color.GRAY);
        g2.drawRoundRect(panelX, panelY, panelW, panelH, 18, 18);

        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.setColor(Color.BLACK);
        g2.drawString("RECYCLING BINS", getWidth() / 2 - 85, panelY + 30);

        int unlocked = unlockedCount();

        for (int i = 0; i < bins.size(); i++) {
            bins.get(i).draw(g2);

            // LOCKED overlay
            if (i >= unlocked) {
                g2.setColor(new Color(0, 0, 0, 120));
                g2.fillRoundRect(bx[i], by[i], bw[i], bh[i], 16, 16);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 18));
                g2.drawString("LOCKED", bx[i] + 35, by[i] + 40);
            }

            // Arrow marker on selected
            if (i == selectedBinIndex) {
                g2.setFont(new Font("Arial", Font.BOLD, 22));
                g2.setColor(new Color(255, 215, 0));
                g2.drawString("▼", bx[i] + bw[i] / 2 - 6, by[i] - 10);
            }

            // flash overlay
            if (flashTimer > 0 && flashBinIndex == i) {
                Color c = flashCorrect ? new Color(0, 255, 0, 90) : new Color(255, 0, 0, 90);
                g2.setColor(c);
                g2.fillRoundRect(bx[i], by[i], bw[i], bh[i], 16, 16);
            }
        }

        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Selected: " + binTypes[selectedBinIndex], getWidth() / 2 - 80, panelY + panelH - 20);
    }

  
    private void drawStatsPanelPretty(Graphics2D g2) {
        int panelW = 320;
        int panelH = 185;
        int x = getWidth() - panelW - 20;
        int y = 10;

        // shadow
        g2.setColor(new Color(0, 0, 0, 35));
        g2.fillRoundRect(x + 4, y + 5, panelW, panelH, 22, 22);

        // panel bg
        g2.setColor(new Color(255, 255, 255, 235));
        g2.fillRoundRect(x, y, panelW, panelH, 22, 22);
        g2.setColor(new Color(140, 140, 140));
        g2.drawRoundRect(x, y, panelW, panelH, 22, 22);

        // title
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 15));
        g2.drawString("Sorted Stats (Correct)", x + 14, y + 22);

        WasteType[] types = new WasteType[]{
                WasteType.PAPER, WasteType.PLASTIC, WasteType.GLASS, WasteType.METAL, WasteType.EWASTE
        };
        String[] labels = new String[]{"P", "Pl", "G", "M", "E"};

      
        int chartX = x + 14;
        int chartY = y + 34;
        int chartW = panelW - 28;
        int chartH = 86;

        int cols = types.length;
        int gap = 14;
        int colW = (chartW - gap * (cols + 1)) / cols;

        int baseY = chartY + chartH;
        int maxBarH = chartH - 18;

      
        g2.setColor(new Color(200, 200, 200));
        g2.drawLine(chartX, baseY, chartX + chartW, baseY);

        for (int i = 0; i < cols; i++) {
            WasteType t = types[i];
            int count = correctSortedCounts.getOrDefault(t, 0);
            double p = (totalCorrectSorted <= 0) ? 0 : (count / (double) totalCorrectSorted);

            int barH = (int) Math.round(maxBarH * p);
            int bx2 = chartX + gap + i * (colW + gap);
            int by2 = baseY - barH;

            Color c = typeColor(t);

            // bar fill only
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 220));
            g2.fillRoundRect(bx2, by2, colW, barH, 14, 14);

            // count + percent at top line
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            g2.setColor(new Color(40, 40, 40));
            String txt = count + "  " + percentText(count, totalCorrectSorted);
            int tw = g2.getFontMetrics().stringWidth(txt);
            g2.drawString(txt, bx2 + (colW - tw) / 2, chartY + 12);
        }

        // legend row: circle + letter
        int legendY = y + 132;
        int lx = x + 16;
        g2.setFont(new Font("Arial", Font.BOLD, 12));

        for (int i = 0; i < cols; i++) {
            Color c = typeColor(types[i]);

            g2.setColor(c);
            g2.fillOval(lx, legendY - 10, 10, 10);

            g2.setColor(new Color(60, 60, 60));
            g2.drawString(labels[i], lx + 14, legendY);

            lx += 52;
        }

        // total
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.drawString("Total Correct: " + totalCorrectSorted, x + 14, y + panelH - 36);

        // win progress
        int progVal = winProgressValue();
        g2.drawString("Win:", x + 14, y + panelH - 18);

        int pBarX = x + 55;
        int pBarY = y + panelH - 26;
        int pBarW = panelW - 55 - 14;
        int pBarH = 10;

        g2.setColor(new Color(210, 210, 210));
        g2.fillRoundRect(pBarX, pBarY, pBarW, pBarH, 10, 10);

        double pp = progVal / (double) WIN_SCORE;
        int pFillW = (int) Math.round(pBarW * pp);
        pFillW = Math.max(0, Math.min(pFillW, pBarW));

        g2.setColor(new Color(0, 160, 255, 220));
        g2.fillRoundRect(pBarX, pBarY, pFillW, pBarH, 10, 10);

        g2.setColor(new Color(120, 120, 120));
        g2.drawRoundRect(pBarX, pBarY, pBarW, pBarH, 10, 10);

        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        g2.setColor(new Color(50, 50, 50));
        String right = progVal + "/" + WIN_SCORE;
        int rw = g2.getFontMetrics().stringWidth(right);
        g2.drawString(right, pBarX + pBarW - rw, y + panelH - 16);
    }

    private void drawCenterOverlay(Graphics2D g2, String title, String subtitle) {
        g2.setColor(new Color(0, 0, 0, 165));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 80));
        int textW = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (getWidth() - textW) / 2, getHeight() / 2);

        g2.setFont(new Font("Arial", Font.BOLD, 22));
        int subW = g2.getFontMetrics().stringWidth(subtitle);
        g2.drawString(subtitle, (getWidth() - subW) / 2, getHeight() / 2 + 45);
    }

    private void drawEndOverlay(Graphics2D g2, String title, String line2, String line3, boolean win) {
        g2.setColor(new Color(0, 0, 0, 185));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(win ? Color.GREEN : Color.RED);
        g2.setFont(new Font("Arial", Font.BOLD, 70));
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (getWidth() - tw) / 2, getHeight() / 2 - 30);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 28));
        int l2w = g2.getFontMetrics().stringWidth(line2);
        g2.drawString(line2, (getWidth() - l2w) / 2, getHeight() / 2 + 20);

        g2.setFont(new Font("Arial", Font.BOLD, 22));
        int l3w = g2.getFontMetrics().stringWidth(line3);
        g2.drawString(line3, (getWidth() - l3w) / 2, getHeight() / 2 + 60);
    }

    // ---------------- GAME LOOP ----------------
    private boolean canSpawn() {
        if (wasteList.isEmpty()) return true;
        Waste last = wasteList.get(wasteList.size() - 1);
        return last.getX() > -currentGap();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (feedbackTimer > 0) feedbackTimer--;
        if (flashTimer > 0) flashTimer--;

        if (gameOver || gameWin) { repaint(); return; }
        if (paused) { repaint(); return; }

        if (inLevelTransition) {
            transitionTicks--;
            if (transitionTicks <= 0) inLevelTransition = false;
            repaint();
            return;
        }

        updateLevelByScore();

        if (random.nextInt(100) < currentSpawnChance() && canSpawn()) spawnWaste();

        // move + overtake 
        for (int i = 0; i < wasteList.size(); i++) {
            Waste w = wasteList.get(i);
            w.move();

            if (i > 0) {
                Waste prev = wasteList.get(i - 1);
                int maxX = prev.getX() - currentGap();
                if (w.getX() > maxX) w.setX(maxX);
            }
        }

        // missed
        Iterator<Waste> it = wasteList.iterator();
        while (it.hasNext()) {
            Waste w = it.next();
            if (w.getX() > getWidth() + 120) {
                it.remove();
                score -= 5;

                feedbackMessage = "-5 MISSED!";
                feedbackColor = Color.RED;
                feedbackTimer = 18;

                updateLevelByScore();
            }
        }

        checkEndConditions();
        repaint();
    }

    private void spawnWaste() {
        WasteType t;

        if (level == 1) {
            int r = random.nextInt(3);
            t = (r == 0) ? WasteType.PAPER : (r == 1) ? WasteType.PLASTIC : WasteType.GLASS;
        } else if (level == 2) {
            int r = random.nextInt(4);
            t = (r == 0) ? WasteType.PAPER
                    : (r == 1) ? WasteType.PLASTIC
                    : (r == 2) ? WasteType.GLASS
                    : WasteType.METAL;
        } else {
            int r = random.nextInt(5);
            t = (r == 0) ? WasteType.PAPER
                    : (r == 1) ? WasteType.PLASTIC
                    : (r == 2) ? WasteType.GLASS
                    : (r == 3) ? WasteType.METAL
                    : WasteType.EWASTE;
        }

        int startX = -90;
        int startY = BELT_Y + 6;
        int speed = currentSpeed();

        wasteList.add(createWasteByType(t, startX, startY, speed));
    }

    private Waste createWasteByType(WasteType t, int x, int y, int speed) {
        switch (t) {
            case PAPER:   return new PaperWaste(x, y, speed);
            case PLASTIC: return new PlasticWaste(x, y, speed);
            case GLASS:   return new GlassWaste(x, y, speed);
            case METAL:   return new MetalWaste(x, y, speed);
            case EWASTE:  return new EWaste(x, y, speed);
            default:      return new PaperWaste(x, y, speed);
        }
    }

    // ---------------- INPUT ----------------
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_R) { resetGame(); return; }

        if (key == KeyEvent.VK_P && !gameOver && !gameWin && !inLevelTransition) {
            paused = !paused;
            repaint();
            return;
        }

        if (gameOver || gameWin || paused) return;

        int unlocked = unlockedCount();

        if (key == KeyEvent.VK_LEFT) {
            selectedBinIndex = (selectedBinIndex - 1 + unlocked) % unlocked;
            syncSelectedState();
        } else if (key == KeyEvent.VK_RIGHT) {
            selectedBinIndex = (selectedBinIndex + 1) % unlocked;
            syncSelectedState();
        } else if (key == KeyEvent.VK_SPACE) {
            if (!inLevelTransition) attemptSort();
        }

        repaint();
    }

    private void attemptSort() {
        if (wasteList.isEmpty()) return;

        Waste front = wasteList.get(0);

        int tol = 35;
        if (front.getX() < SORTING_LINE_X - tol) {
            feedbackMessage = "Too early!";
            feedbackColor = Color.DARK_GRAY;
            feedbackTimer = 12;
            return;
        }

        WasteType selected = binTypes[selectedBinIndex];
        boolean correct = front.getType() == selected;

        if (correct) {
            score += 20;
            incrementCorrect(front.getType());

            feedbackMessage = "+20 GOOD!";
            feedbackColor = new Color(0, 150, 0);
            feedbackTimer = 18;
        } else {
            score -= 5;
            feedbackMessage = "-5 WRONG!";
            feedbackColor = Color.RED;
            feedbackTimer = 18;
        }

        updateLevelByScore();

        flashBinIndex = selectedBinIndex;
        flashCorrect = correct;
        flashTimer = 12;

        wasteList.remove(0);

        checkEndConditions();
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}
