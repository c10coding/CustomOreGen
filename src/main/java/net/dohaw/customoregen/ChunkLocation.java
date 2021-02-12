package net.dohaw.customoregen;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ChunkLocation {

    @Getter
    private int x, z;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkLocation that = (ChunkLocation) o;
        return x == that.x &&
                z == that.z;
    }

}
