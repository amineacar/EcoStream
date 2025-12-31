package ecostream;

import java.awt.Color;

public class PlasticWaste extends Waste {
    public PlasticWaste(int startX, int startY, int speed) {
        super(startX, startY, 78, 78, speed, WasteType.PLASTIC, Color.ORANGE);
    }
}
