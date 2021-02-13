package net.dohaw.customoregen;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

@AllArgsConstructor
public class OreWorldData {

    @Getter
    private int minY, maxY;

    @Getter
    private double spawnChance;

    @Getter
    private boolean willGeneratorOre;

    @Getter
    private Material materialReplaced;

}
