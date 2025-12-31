package ecostream;

import java.awt.Color;

public class MetalWaste extends Waste {
    public MetalWaste(int startX, int startY, int speed) {
        super(startX, startY, 78, 78, speed, WasteType.METAL, new Color(160,160,160));
    }
}
