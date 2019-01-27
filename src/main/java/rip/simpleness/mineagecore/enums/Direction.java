package rip.simpleness.mineagecore.enums;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public enum Direction {

    UP, DOWN, HORIZONTAL;

    public BlockFace toBlockFace() {
        return this == UP ? BlockFace.UP : this == DOWN ? BlockFace.DOWN : null;
    }

    public BlockFace toBlockFace(Block blockA, Block blockB) {
        return blockA.getFace(blockB);
    }
}
