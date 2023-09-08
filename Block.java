// Last update: Sep 8, 2023

/////////////////////////////
// 代码尚在完善过程中且未经测试 //
/////////////////////////////

import java.util.LinkedList;

public class Block {
    private LinkedList<Line> block;
    private int blockId;
    private int levelId;
    private boolean edited;
    private String type;

    public Block(int blockId, int levelId) {
        block = new LinkedList<>();
        this.blockId = blockId;
        this.levelId = levelId;
        edited = false;
        type = "";
    }

    public void add(Line l) {block.add(l);}

    public Line get(int index) {return block.get(index);}

    public LinkedList<Line> getBlock() {
        return block;
    }

    public void setBlock(LinkedList<Line> block) {
        this.block = block;
    }

    public int lineInBlock(int lineId) {
        for (int i = 0; i < block.size(); i++) {
            if (block.get(i).getLineId() == lineId) {return i;}
        }
        return -1;
    }

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    public int getLevelId() {
        return levelId;
    }

    public void setLevelId(int levelId) {
        this.levelId = levelId;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public int getStartId() {return block.get(0).getLineId();}

    public int getEndId() {return block.get(blockSize()-1).getLineId();}

    public int blockSize() {return block.size();}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
