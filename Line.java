// Last update: Sep 8, 2023

/////////////////////////////
// 代码尚在完善过程中且未经测试 //
/////////////////////////////

import java.util.HashMap;

public class Line {
    private String content;
    private boolean startLine;
    private boolean endLine;
    private boolean blankLine;
    private boolean split;
    //////////////////////////////////////////////////////
    // lineSplit和fullSplit示例：
    // 专门针对于一行拆为多行（/**/多行comment不算）
    // 只有第一行是startLine，后面两行不是
    // 
    // public class A          lineSplit=0, fullSplit=3
    //     extends B           lineSplit=1, fullSplit=3
    // throws Exception {      lineSplit=2, fullSplit=3
    //////////////////////////////////////////////////////
    private int lineSplit;
    private int fullSplit;
    private int indent;
    private String type;
    private int length;
    private int userXStart;
    private int userXEnd;
    private int userY;
    private int lineId;
    private boolean fauxStartLine;
    private boolean fauxEndLine;
    private boolean okay; // 没有问题，如果下列任何一个为true则这个为false（glue除外）
    private boolean xSemi; // 缺分号
    private boolean xLeftCurly; // 缺左大括号
    private boolean xRightCurly; // 缺右大括号
    private boolean xUnknown; // 缺未知元素
    private boolean glue;  // 和上一个黏在同一行
    // 例：int a = 1; a++;
    // 前者没有glue，后者有glue
    // 注意两者的indent相同，即使后者没有缩进
    private int glueOrder;
    // 上面两个Line的glueOrder分别为0和1
    private boolean xLeftComment; // 缺/*
    private boolean xRightComment; // 缺*/
    //////////////////////////////////////////////
    // ID关系图：(每一级都能认出自己的上级和下级)
    // 注意Line是固定的，Block和Level是可变的
    //                Level
    //               /     \
    //          Block        Block
    //         /     \      /     \
    //       Line   Line  Line   Line
    //////////////////////////////////////////////
    private HashMap<Integer, Integer> blockByLevel; // <levelId, blockId>


    public Line(String content, int indent, int userXStart, String type, int lineId, int userY) {
        this.content = content;
        this.indent = indent;
        this.type = type;
        this.userXStart = userXStart;
        this.userXEnd = userXStart + content.length() - 1; // TODO: '\n'需要在这里考虑吗？
        this.lineId = lineId;
        this.userY = userY;

        startLine = false;
        endLine = false;
        blankLine = false;
        split = false;
        lineSplit = -1;
        fullSplit = -1;
        length = content.length();
        fauxStartLine = false;
        fauxEndLine = false;
        okay = true;
        xSemi = false;
        xLeftCurly = false;
        xRightCurly = false;
        glue = false;
        xLeftComment = false;
        xRightComment = false;
        xUnknown = false;
        blockByLevel = new HashMap<>();
        glueOrder = 0;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isStartLine() {
        return startLine;
    }

    public void setStartLine(boolean startLine) {
        this.startLine = startLine;
        if (startLine) {
            fauxStartLine = false;
            endLine = false;
            fauxEndLine = false;
        }
    }

    public boolean isEndLine() {
        return endLine;
    }

    public void setEndLine(boolean endLine) {
        this.endLine = endLine;
        if (endLine) {
            startLine = false;
            fauxStartLine = false;
            fauxEndLine = false;
        }
    }

    public boolean isBlankLine() {
        return blankLine;
    }

    public void setBlankLine(boolean blankLine) {
        this.blankLine = blankLine;
    }

    public boolean isSplit() {
        return split;
    }

    public void setSplit(boolean split) {
        this.split = split;
    }

    public int getLineSplit() {
        return lineSplit;
    }

    public void setLineSplit(int lineSplit) {
        this.lineSplit = lineSplit;
    }

    public int getFullSplit() {
        return fullSplit;
    }

    public void setFullSplit(int fullSplit) {
        this.fullSplit = fullSplit;
    }

    public int getIndent() {
        return indent;
    }

    public void setIndent(int indent) {
        this.indent = indent;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getLineId() {
        return lineId;
    }

    public void setLineId(int lineId) {
        this.lineId = lineId;
    }

    public boolean isFauxStartLine() {
        return fauxStartLine;
    }

    public void setFauxStartLine(boolean fauxStartLine) {
        this.fauxStartLine = fauxStartLine;
        if (fauxStartLine) {
            startLine = false;
            endLine = false;
            fauxEndLine = false;
        }
    }

    public boolean isFauxEndLine() {
        return fauxEndLine;
    }

    public void setFauxEndLine(boolean fauxEndLine) {
        this.fauxEndLine = fauxEndLine;
        if (fauxEndLine) {
            startLine = false;
            fauxStartLine = false;
            endLine = false;
        }
    }

    public boolean isOkay() {
        return okay;
    }

    public void setOkay(boolean okay) {
        this.okay = okay;
    }

    public void updateOkay() {this.okay =
            !(isxLeftComment() || isxLeftCurly() || isxRightComment() || isxRightCurly() || isxSemi());}

    public boolean isxSemi() {
        return xSemi;
    }

    public void setxSemi(boolean xSemi) {
        this.xSemi = xSemi;
        updateOkay();
    }

    public boolean isxLeftCurly() {
        return xLeftCurly;
    }

    public void setxLeftCurly(boolean xLeftCurly) {
        this.xLeftCurly = xLeftCurly;
        updateOkay();
    }

    public boolean isxRightCurly() {
        return xRightCurly;
    }

    public void setxRightCurly(boolean xRightCurly) {
        this.xRightCurly = xRightCurly;
        updateOkay();
    }

    public boolean isGlue() {
        return glue;
    }

    public void setGlue(boolean glue) {
        this.glue = glue;
    }

    public boolean isxLeftComment() {
        return xLeftComment;
    }

    public void setxLeftComment(boolean xLeftComment) {
        this.xLeftComment = xLeftComment;
        updateOkay();
    }

    public boolean isxRightComment() {
        return xRightComment;
    }

    public void setxRightComment(boolean xRightComment) {
        this.xRightComment = xRightComment;
        updateOkay();
    }

    public HashMap<Integer, Integer> getBlockByLevel() {
        return blockByLevel;
    }

    public void setBlockByLevel(HashMap<Integer, Integer> blockByLevel) {
        this.blockByLevel = blockByLevel;
    }

    public void addLevelBlock(int levelId, int blockId) {
        blockByLevel.put(levelId, blockId);
    }

    public int getUserY() {
        return userY;
    }

    public void setUserY(int userY) {
        this.userY = userY;
    }

    public int getGlueOrder() {
        return glueOrder;
    }

    public void setGlueOrder(int glueOrder) {
        this.glueOrder = glueOrder;
    }

    public boolean isxUnknown() {
        return xUnknown;
    }

    public void setxUnknown(boolean xUnknown) {
        this.xUnknown = xUnknown;
        updateOkay();
    }

    public int getUserXStart() {
        return userXStart;
    }

    public void setUserXStart(int userXStart) {
        this.userXStart = userXStart;
    }

    public int getUserXEnd() {
        return userXEnd;
    }

    public void setUserXEnd(int userXEnd) {
        this.userXEnd = userXEnd;
    }
}