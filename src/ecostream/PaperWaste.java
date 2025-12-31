package ecostream;

import java.awt.Color;

public class PaperWaste extends Waste {
    public PaperWaste(int startX, int startY, int speed) {
        super(startX, startY, 78, 78, speed, WasteType.PAPER, Color.WHITE);
    }
}
