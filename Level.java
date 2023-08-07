// Last update: Aug 4, 2023

import java.util.LinkedList;

public class Level {
    // 如果是第i个block，则开始行是blockStart.get(i)，结束行和type类似
    private LinkedList<Integer> blockStart;
    private LinkedList<Integer> blockEnd;
    private LinkedList<String> blockTypes;

    public Level() {
        this.blockStart = new LinkedList<>();
        this.blockEnd = new LinkedList<>();
        this.blockTypes = new LinkedList<>();
    }

    public void addBlock(int start, int end, String type) {
        blockStart.add(start);
        blockEnd.add(end);
        blockTypes.add(type);
    }

    public int getStart(int index) {return (int) blockStart.get(index);}

    public int getEnd(int index) {return (int) blockEnd.get(index);}

    public String getType(int index) {return blockTypes.get(index);}

    public int getSize() {return blockStart.size();}
}