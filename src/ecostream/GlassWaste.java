package ecostream;

import java.awt.Color;

public class GlassWaste extends Waste {
    public GlassWaste(int startX, int startY, int speed) {
        super(startX, startY, 78, 78, speed, WasteType.GLASS, Color.CYAN);
    }
}
