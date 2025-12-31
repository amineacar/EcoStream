package ecostream;

import java.awt.Color;

public class EWaste extends Waste {
    public EWaste(int startX, int startY, int speed) {
        super(startX, startY, 78, 78, speed, WasteType.EWASTE, new Color(220,60,60));
    }
}
