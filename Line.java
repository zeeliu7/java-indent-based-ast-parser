// Last update: Aug 4, 2023

public class Line {
    private String content;
    private boolean isStartLine;
    private boolean isEndLine;
    private boolean isBlankLine;
    private boolean isXL;
    ///////////////////////////////////////////////
    // lineXL和fullXL示例：
    // 专门针对于一行拆为多行（/**/多行comment不算）
    // 只有第一行是startLine，后面两行不是
    // 
    // public class A          lineXL=0, fullXL=3
    //     extends B           lineXL=1, fullXL=3
    // throws Exception {      lineXL=2, fullXL=3
    ///////////////////////////////////////////////
    private int lineXL;
    private int fullXL;
    private int indent;
    private String type;

    public Line(String content, int indent, String type) {
        this.content = content;
        this.indent = indent;
        this.type = type;

        isStartLine = false;
        isEndLine = false;
        isBlankLine = false;
        isXL = false;
        lineXL = -1;
        fullXL = -1;
    }


    public String getContent() {return content;}

    public void setContent(String content) {this.content = content;}

    public boolean getIsStartLine() {return isStartLine;}

    public void setIsStartLine(boolean isStartLine) {this.isStartLine = isStartLine;}


    public boolean getIsEndLine() {return isEndLine;}

    public void setIsEndLine(boolean isEndLine) {this.isEndLine = isEndLine;}

    public boolean getIsBlankLine() {return isBlankLine;}

    public void setIsBlankLine(boolean isBlankLine) {this.isBlankLine = isBlankLine;}

    public boolean getIsXL() {return isXL;}

    public void setIsXL(boolean isXL) {this.isXL = isXL;}

    public int getLineXL() {return lineXL;}

    public void setLineXL(int lineXL) {this.lineXL = lineXL;}

    public int getFullXL() {return fullXL;}

    public void setFullXL(int fullXL) {this.fullXL = fullXL;}

    public int getIndent() {return indent;}

    public void setIndent(int indent) {this.indent = indent;}

    public String getType() {return type;}

    public void setType(String type) {this.type = type;}

}