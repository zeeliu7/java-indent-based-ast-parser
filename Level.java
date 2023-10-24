import java.util.LinkedList;

public class Level {
    private LinkedList<Block> level;
    private int levelId;
    private int levelIndent;
    private LinkedList<Line> plainLines;

    public Level(int levelId, int levelIndent, LinkedList<Line> plainLines) {
        level = new LinkedList<>();
        this.levelId = levelId;
        this.levelIndent = levelIndent;
        this.plainLines = plainLines;
    }

    public void add(Block b) {level.add(b);}

    public Block get(int index) {return level.get(index);}

    public int size() {return level.size();}

    public int getLevelId() {
        return levelId;
    }

    public void setLevelId(int levelId) {
        this.levelId = levelId;
    }


    public int getLevelIndent() {
        return levelIndent;
    }

    public void setLevelIndent(int levelIndent) {
        this.levelIndent = levelIndent;
    }

    public LinkedList<Line> getPlainLines() {
        return plainLines;
    }

    public void setPlainLines(LinkedList<Line> plainLines) {
        this.plainLines = plainLines;
    }
}
