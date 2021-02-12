package net.dohaw.customoregen;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class OreWorldData {

    @Getter
    private int minY, maxY;

    @Getter
    private double spawnChance;

    @Getter
    private boolean willGeneratorOre;

}
