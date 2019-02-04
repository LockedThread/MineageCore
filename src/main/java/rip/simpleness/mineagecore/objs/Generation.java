package rip.simpleness.mineagecore.objs;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import me.lucko.helper.Schedulers;
import me.lucko.helper.promise.Promise;
import me.lucko.helper.serialize.BlockPosition;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlock;

import java.util.concurrent.ExecutionException;

public final class Generation {

    private GenBlock genBlock;
    private BlockPosition initialBlockPosition, currentBlockPosition;
    private BlockFace blockFace;
    private int length;

    public Generation(GenBlock genBlock, BlockPosition initialBlockPosition, BlockFace blockFace) {
        this.genBlock = genBlock;
        this.initialBlockPosition = initialBlockPosition;
        this.currentBlockPosition = initialBlockPosition;
        this.blockFace = blockFace;
        this.length = 1;
    }

    public boolean generate() {
        try {
            return Promise.start()
                    .thenRunSync(() -> {
                        final Chunk chunk = currentBlockPosition.toChunk().toChunk();
                        if (!chunk.isLoaded()) {
                            chunk.load();
                        }
                    }).thenApplyAsync(aVoid -> {
                        if ((initialBlockPosition.toBlock().getType() != genBlock.getMaterial() || currentBlockPosition.toBlock().getType() != genBlock.getMaterial()) && (!genBlock.isPatch())) {
                            return false;
                        }
                        final Block relative = currentBlockPosition.toBlock().getRelative(blockFace);
                        if (genBlock.isPatch() && (relative.getType() == Material.AIR || (relative.getType() == Material.STATIONARY_WATER || relative.getType() == Material.WATER))) {
                            if (length >= 500) {
                                return false;
                            }
                            Schedulers.sync().run(() -> ((CraftBlock) relative).setTypeIdAndData(genBlock.getMaterial().getId(), (byte) 0, relative.getType() == Material.WATER));
                            currentBlockPosition = BlockPosition.of(relative);
                            length++;
                            return true;
                        }
                        if (relative.getType() == Material.AIR) {
                            if ((blockFace == BlockFace.NORTH ||
                                    blockFace == BlockFace.SOUTH ||
                                    blockFace == BlockFace.EAST ||
                                    blockFace == BlockFace.WEST) &&
                                    (!Board.getInstance().getFactionAt(new FLocation(initialBlockPosition.toLocation())).getTag().equals(Board.getInstance().getFactionAt(new FLocation(currentBlockPosition.toLocation())).getTag())) ||
                                    length == 32) {
                                return false;
                            }
                            Schedulers.sync().run(() -> ((CraftBlock) relative).setTypeIdAndData(genBlock.getMaterial().getId(), (byte) 0, false));
                            currentBlockPosition = BlockPosition.of(relative);
                            length++;
                            return true;
                        }
                        return false;
                    }).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    public GenBlock getGenBlock() {
        return genBlock;
    }

    public void setGenBlock(GenBlock genBlock) {
        this.genBlock = genBlock;
    }

    public BlockPosition getInitialBlockPosition() {
        return initialBlockPosition;
    }

    public void setInitialBlockPosition(BlockPosition initialBlockPosition) {
        this.initialBlockPosition = initialBlockPosition;
    }

    public BlockPosition getCurrentBlockPosition() {
        return currentBlockPosition;
    }

    public void setCurrentBlockPosition(BlockPosition currentBlockPosition) {
        this.currentBlockPosition = currentBlockPosition;
    }

    public BlockFace getBlockFace() {
        return blockFace;
    }

    public int getLength() {
        return length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Generation that = (Generation) o;

        return genBlock.equals(that.genBlock) && initialBlockPosition.equals(that.initialBlockPosition) && currentBlockPosition.equals(that.currentBlockPosition);
    }

    @Override
    public int hashCode() {
        int result = genBlock.hashCode();
        result = 31 * result + initialBlockPosition.hashCode();
        result = 31 * result + currentBlockPosition.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Generation{" +
                "genBlock=" + genBlock +
                ", initialBlockPosition=" + initialBlockPosition +
                ", currentBlockPosition=" + currentBlockPosition +
                '}';
    }
}
