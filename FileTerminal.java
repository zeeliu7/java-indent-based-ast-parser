// Last update: Aug 4, 2023

import java.io.*;
import java.util.LinkedList;
import java.util.Scanner;
import java.lang.IllegalArgumentException;
import java.lang.IndexOutOfBoundsException;

// 注意：除print语句外所有行数从第0行开始计算而不是第1行

public class FileTerminal {
    private final String FILE_PATH;
    private LinkedList<Line> lines;
    private LinkedList<Level> bFile; // 由block组成的File
    private Toolkit toolkit;
    private int[] indents;
    private boolean lineParsed;
    private boolean fileParsed;
    private File fixedFile;



    public FileTerminal(String filePath) throws FileNotFoundException {
        FILE_PATH = filePath;
        bFile = new LinkedList<>();
        lines = new LinkedList<>();
        toolkit = new Toolkit();
        lineParsed = false;
    }

    private void linesSetup() throws FileNotFoundException {
        lineParsed = false;

        Scanner sc = new Scanner(new File(FILE_PATH));
        String thisLine = "";
        while (sc.hasNextLine()) {
            thisLine = sc.nextLine();
            lines.add(new Line(thisLine, toolkit.getLineIndent(thisLine), toolkit.getType(thisLine)));
        }

        indents = toolkit.getIndentArray(FILE_PATH);
    }

    /**
     * 一个level得到文件中具有某个特定缩进的行，用来推测哪些是开始行，结束行和独立行，并补全大括号和分号
     *
     * @param level 最外层是level 0，再往里是1,2,...
     * @throws FileNotFoundException
     */
    private void parseLines (int level) throws FileNotFoundException {

        // 空行标注
        for(Line l : lines) {
            if (l.getType().equals("blank")) {
                l.setIsBlankLine(true);
                l.setIsStartLine(true);
                l.setIsEndLine(true);
            }
        }

        // 开始结束行标注
        if (level >= indents.length) {throw new IndexOutOfBoundsException("illegal level in parseLines()");}
        int levelIndent = indents[level];

        int pointer = 0;
        boolean openCurly = false;
        while (pointer < lines.size()) {
            Line l = lines.get(pointer);
            if (!l.getIsBlankLine() && l.getIndent() == levelIndent) {
                l.setType(toolkit.getType(l.getContent()));
                String thisType = l.getType();

                // 完整单行整体
                if (thisType.equals("comment") || thisType.equals("import")
                        || thisType.equals("package") || thisType.equals("annotation")
                        || thisType.equals("classStartEnd") || thisType.equals("interfaceStartEnd")) {
                    l.setIsStartLine(true);
                    l.setIsEndLine(true);
                }

                // 缺损单行整体
                // TODO: 目前只能修复已有"import/package"没有";"的情况，
                //  并且除第一行外只能是semicolon, noSuffix和others，
                //  需要考虑别的情况吗？
                else if (thisType.equals("importX") || thisType.equals("packageX")) {
                    l.setIsStartLine(true);
                    int p = pointer + 1;
                    boolean fixed = false;
                    int xp = pointer;
                    while (p < lines.size()) {
                        String pType = lines.get(p).getType();
                        if (!(pType.equals("semicolon") || pType.equals("noSuffix") || pType.equals("others"))) {
                            lines.get(p-1).setContent(lines.get(p-1).getContent()+";");
                            lines.get(p-1).setType("semicolon");
                            int fullXL = p - pointer; // (p-1) - pointer + 1
                            for (int i = 0; i < fullXL; i++) {
                                lines.get(pointer + i).setIsXL(true);
                                lines.get(pointer + i).setLineXL(i);
                                lines.get(pointer + i).setFullXL(fullXL);
                                lines.get(pointer + i).setContent(
                                        toolkit.modifyIndent(lines.get(pointer+i).getContent(), levelIndent));
                                lines.get(pointer + i).setIndent(levelIndent);
                            }
                            pointer = p - 1;
                            break;
                        }
                        else if (pType.equals("semicolon")) {
                            lines.get(p).setContent(lines.get(p).getContent()+";");
                            int fullXL = p - pointer + 1;
                            for (int i = 0; i < fullXL; i++) {
                                lines.get(pointer + i).setIsXL(true);
                                lines.get(pointer + i).setLineXL(i);
                                lines.get(pointer + i).setFullXL(fullXL);
                                lines.get(pointer + i).setContent(
                                        toolkit.modifyIndent(lines.get(pointer+i).getContent(), levelIndent));
                                lines.get(pointer + i).setIndent(levelIndent);
                            }
                            pointer = p;
                            break;
                        }
                        else {p++;}
                    }

                    if (!fixed) {
                        lines.get(lines.size()-1).setContent(lines.get(lines.size()-1).getContent() + ";");
                        lines.get(lines.size()-1).setType(toolkit.getType(lines.get(lines.size()-1).getContent()));
                        for (int i = xp; i < lines.size(); i++) {
                            lines.get(i).setContent(toolkit.modifyIndent(lines.get(i).getContent(), levelIndent));
                            lines.get(i).setIndent(levelIndent);
                        }
                        pointer = lines.size();
                    }
                }

                // 多行comment特殊处理
                else if (thisType.equals("commentPlusStartEnd")) {
                    l.setIsStartLine(true);
                    l.setIsEndLine(true);
                }

                // TODO: 目前只能修复已有"/*"没有"*/"的情况，
                //  并且除第一行外只能是commentPlusMid, commentPlusEnd, noSuffix和others，
                //  需要考虑别的情况吗？
                else if (thisType.equals("commentPlusStart")) {
                    l.setIsStartLine(true);
                    int p = pointer + 1;
                    boolean fixed = false;
                    int xp = pointer;
                    while (p < lines.size()) {
                        String pType = lines.get(p).getType();
                        if (!(pType.equals("commentPlusMid") || pType.equals("commentPlusEnd")
                                || pType.equals("noSuffix") || pType.equals("others"))) {
                            lines.get(p-1).setContent(lines.get(p-1).getContent() + "*/");
                            lines.get(p-1).setIsEndLine(true);
                            lines.get(p-1).setType("commentPlusEnd");
                            for (int i = pointer; i < p; i++) {
                                lines.get(i).setContent(toolkit.modifyIndent(lines.get(i).getContent(), levelIndent));
                                lines.get(i).setIndent(levelIndent);
                            }
                            pointer = p - 1;
                            fixed = true;
                            break;
                        }
                        else if (lines.get(p).getType().equals("commentPlusEnd")) {
                            lines.get(p).setIsEndLine(true);
                            for (int i = pointer; i <= p; i++) {
                                lines.get(i).setContent(toolkit.modifyIndent(lines.get(i).getContent(), levelIndent));
                                lines.get(i).setIndent(levelIndent);
                            }
                            pointer = p;
                            fixed = true;
                            break;
                        }
                        else {p++;}
                    }
                    if (!fixed) {
                        lines.get(lines.size()-1).setContent(lines.get(lines.size()-1).getContent() + "*/");
                        lines.get(lines.size()-1).setType(toolkit.getType(lines.get(lines.size()-1).getContent()));
                        for (int i = xp; i < lines.size(); i++) {
                            lines.get(i).setContent(toolkit.modifyIndent(lines.get(i).getContent(), levelIndent));
                            lines.get(i).setIndent(levelIndent);
                        }
                        pointer = lines.size();
                    }
                }

                // 单行完整class/interface
                else if (thisType.equals("classStartEnd") || thisType.equals("interfaceStartEnd")) {
                    if (openCurly) {throw new IllegalArgumentException("missing '}' before line " + (pointer+1));}

                    l.setIsStartLine(true);
                    l.setIsEndLine(true);
                    openCurly = false;
                }

                // 单行class/interface/大括号开始
                // 注意：同level大括号必须先打开再关上，不能连续有两个左大括号或右大括号
                else if (thisType.equals("classStart") || thisType.equals("interfaceStart")
                        || thisType.equals("curlyStart")) {
                    if (openCurly) {throw new IllegalArgumentException("missing '}' before line " + (pointer+1));}

                    l.setIsStartLine(true);
                    openCurly = true;

                    // TODO: 目前只能修复已有'{'没有'}'的情况，
                    //  并且相同缩进行中含'{'行必须紧接'}'行
                    //  需要考虑别的情况吗？
                    int p = pointer + 1;
                    boolean fixed = false;
                    while (p < lines.size()) {
                        if (lines.get(p).getIndent() <= levelIndent) {
                            String pType = lines.get(p).getType();
                            if ((pType.equals("curlyEnd") || pType.equals("curlyEndSemicolon")
                                    || pType.equals("curlyEndStart")) && (lines.get(p).getIndent() == levelIndent)) {
                                pointer = p;
                                fixed = true;
                                openCurly = false;
                                break;
                            }
                            else {
                                lines.add(p,new Line(toolkit.modifyIndent("}", levelIndent), levelIndent, "curlyEnd"));
                                pointer = p - 1;
                                fixed = true;
                                break;
                            }
                        }
                        p++;
                    }
                    if (!fixed) {
                        lines.add(new Line(toolkit.modifyIndent("}", levelIndent), levelIndent, "curlyEnd"));
                        openCurly = false;
                        pointer = lines.size();
                    }
                }

                // 单行class/interface开始，但是没有大括号
                // TODO: 目前只能修复已有"class/interface"没有"{"的情况，
                //  并且除第一行外只能是curlyStart, curlyStartEnd, noSuffix和others，
                //  需要考虑别的情况吗？
                else if (thisType.equals("classStartX") || thisType.equals("interfaceStartX")) {
                    if (openCurly) {throw new IllegalArgumentException("missing '}' before line " + (pointer+1));}

                    l.setIsStartLine(true);
                    int p = pointer + 1;
                    boolean fixed = false;
                    int xp = pointer;
                    while (p < lines.size()) {
                        String pType = lines.get(p).getType();
                        if (!(pType.equals("curlyStart") || pType.equals("curlyStartEnd")
                                || pType.equals("noSuffix") || pType.equals("others"))) {
                            lines.get(p-1).setContent(lines.get(p-1).getContent()+"{");
                            lines.get(p-1).setType("curlyStart");
                            int fullXL = p - pointer; // (p-1) - pointer + 1
                            for (int i = 0; i < fullXL; i++) {
                                lines.get(pointer + i).setIsXL(true);
                                lines.get(pointer + i).setLineXL(i);
                                lines.get(pointer + i).setFullXL(fullXL);
                                lines.get(pointer + i).setContent(
                                        toolkit.modifyIndent(lines.get(pointer+i).getContent(), levelIndent));
                                lines.get(pointer + i).setIndent(levelIndent);
                            }
                            pointer = p - 1;
                            fixed = true;
                            break;
                        }
                        else if (pType.equals("curlyStartEnd")) {
                            lines.get(p).setIsEndLine(true);
                            int fullXL = p - pointer + 1;
                            for (int i = 0; i < fullXL; i++) {
                                lines.get(pointer + i).setIsXL(true);
                                lines.get(pointer + i).setLineXL(i);
                                lines.get(pointer + i).setFullXL(fullXL);
                                lines.get(pointer + i).setContent(
                                        toolkit.modifyIndent(lines.get(pointer+i).getContent(), levelIndent));
                                lines.get(pointer + i).setIndent(levelIndent);
                            }
                            pointer = p;
                            openCurly = false;
                            fixed = true;
                            break;
                        }
                        else if (pType.equals("curlyStart")) {
                            int fullXL = p - pointer + 1;
                            for (int i = 0; i < fullXL; i++) {
                                lines.get(pointer + i).setIsXL(true);
                                lines.get(pointer + i).setLineXL(i);
                                lines.get(pointer + i).setFullXL(fullXL);
                                lines.get(pointer + i).setContent(
                                        toolkit.modifyIndent(lines.get(pointer+i).getContent(), levelIndent));
                                lines.get(pointer + i).setIndent(levelIndent);
                            }
                            pointer = p;
                            openCurly = true;
                            fixed = true;
                            break;
                        }
                        else {p++;}
                    }

                    if (!fixed) {
                        lines.get(lines.size()-1).setContent(lines.get(lines.size()-1).getContent() + "{");
                        lines.get(lines.size()-1).setType(toolkit.getType(lines.get(lines.size()-1).getContent()));
                        for (int i = xp; i < lines.size(); i++) {
                            lines.get(i).setContent(toolkit.modifyIndent(lines.get(i).getContent(), levelIndent));
                            lines.get(i).setIndent(levelIndent);
                        }
                        openCurly = true;
                        pointer = lines.size();
                    }

                    // TODO: 目前只能修复已有'{'没有'}'的情况，
                    //  并且相同缩进行中含'{'行必须紧接'}'行
                    //  需要考虑别的情况吗？
                    p = pointer + 1;
                    fixed = false;
                    while (p < lines.size()) {
                        if (lines.get(p).getIndent() <= levelIndent) {
                            String pType = lines.get(p).getType();
                            if ((pType.equals("curlyEnd") || pType.equals("curlyEndSemicolon")
                                    || pType.equals("curlyEndStart")) && (lines.get(p).getIndent() == levelIndent)) {
                                pointer = p;
                                fixed = true;
                                openCurly = false;
                                break;
                            }
                            else {
                                lines.add(p,new Line(toolkit.modifyIndent("}", levelIndent), levelIndent, "curlyEnd"));
                                pointer = p - 1;
                                fixed = true;
                                break;
                            }
                        }
                        p++;
                    }
                    if (!fixed) {
                        lines.add(new Line(toolkit.modifyIndent("}", levelIndent), levelIndent, "curlyEnd"));
                        openCurly = false;
                        pointer = lines.size();
                    }
                }

                // curlyStartEnd和curlyEndStart（已经确保是单独一行）
                else if (thisType.equals("curlyStartEnd")) {
                    //因为不知道哪一行缺了括号只能throw exception
                    if (openCurly) {throw new IllegalArgumentException("missing '}' before line " + (pointer+1));}

                    l.setIsStartLine(true);
                    l.setIsEndLine(true);
                    openCurly = false;
                }

                // curlyEndSemicolon：由于包含end+start，建议拆成两部分
                else if (thisType.equals("curlyEndStart")) {
                    if (!openCurly) {throw new IllegalArgumentException("missing '{' before line " + (pointer+1));}

                    String original = l.getContent();
                    lines.add(pointer, new Line(toolkit.modifyIndent(";", levelIndent), levelIndent, "curlyEnd"));
                    pointer++;
                    l = lines.get(pointer);
                    String start = original.substring(original.indexOf("}") + 1);
                    l.setContent(start);
                    l.setType(toolkit.getType(start));
                    lines.get(pointer-1).setIsEndLine(true);
                    lines.get(pointer).setIsStartLine(true);
                    openCurly = true;
                }

                // curlyEndSemicolon：由于包含end+start+end，建议拆成两部分
                else if (thisType.equals("curlyEndSemicolon")) {
                    if (!openCurly) {throw new IllegalArgumentException("missing '{' before line " + (pointer+1));}

                    String original = l.getContent();
                    lines.add(pointer, new Line(toolkit.modifyIndent(";", levelIndent), levelIndent, "curlyEnd"));
                    pointer++;
                    l = lines.get(pointer);
                    String semi = original.substring(original.indexOf("}") + 1);
                    l.setContent(semi);
                    l.setType(toolkit.getType(semi));
                    lines.get(pointer-1).setIsEndLine(true);
                    lines.get(pointer).setIsStartLine(true);
                    lines.get(pointer).setIsEndLine(true);
                    openCurly = false;
                }

                // 单行大括号结束
                else if (thisType.equals("curlyEnd")) {
                    if (!openCurly) {throw new IllegalArgumentException("missing '{' before line " + (pointer+1));}
                    l.setIsEndLine(true);
                    openCurly = false;
                }

                // 单行分号
                else if (thisType.equals("semicolon")) {
                    l.setIsStartLine(true);
                    l.setIsEndLine(true);
                }

                // noSuffix和others
                // TODO: 有可能是开始或结束行吗？
                // TODO: 盲目测试加分号，因为没有特征可以提取,一行拆多行的情况已经考虑过了
                else {
                    l.setContent(l.getContent() + ";");
                    l.setType(toolkit.getType(l.getContent()));
                    l.setIsStartLine(true);
                    l.setIsEndLine(true);
                }
            }

            pointer++;
        }

        if (openCurly) {throw new IllegalArgumentException("missing '}' until EOF");}

        lineParsed = true;

    }

    /**
     * 根据开始结束行来确定一个level中的blocks
     *
     * @param level 最外层是level 0，再往里是1,2,...
     * @return 包含所有block的level
     * @throws FileNotFoundException
     * @throws NullPointerException
     */
    private Level parseLevel(int level) throws FileNotFoundException, NullPointerException {
        parseLines(level);
        if (!lineParsed) {throw new NullPointerException("parseLines() error");}

        int pointer = 0;
        Level thisLevel = new Level();
        while (pointer < lines.size()) {
            Line l = lines.get(pointer);
            if (l.getIsBlankLine()) {thisLevel.addBlock(pointer, pointer, "blank");}
            if (l.getIsStartLine() && l.getIsEndLine()) {
                thisLevel.addBlock(pointer, pointer, toolkit.typeConverter(l.getType()));
            }
            if (l.getIsStartLine()) {
                int p = pointer + 1;
                while (p < lines.size()) {
                    if (lines.get(p).getIsEndLine()) {
                        thisLevel.addBlock(pointer, p, toolkit.typeConverter(l.getType()));
                        break;
                    }

                    p++;
                }
            }

            pointer++;
        }

        return thisLevel;
    }

    /**
     * 生成由不同level不同block组成的BFile
     *
     * @throws FileNotFoundException
     */
    private void parseBFile() throws FileNotFoundException {
        indents = toolkit.getIndentArray(FILE_PATH);
        for (int i = 0; i < indents.length; i++) {
            bFile.add(parseLevel(i));
        }
        fileParsed = true;
    }

    /**
     * 生成代码补全后的文件
     *
     * @param filePath 新文件的名称
     * @throws IOException
     * @throws NullPointerException
     */
    public void fixFile(String filePath) throws IOException, NullPointerException {
        linesSetup();
        parseBFile();
        if (!fileParsed) {throw new NullPointerException("parseBFile() error");}

        fixedFile = new File(filePath);
        FileWriter writer = new FileWriter(fixedFile);
        for (int i = 0; i < lines.size(); i++) {writer.write(lines.get(i).getContent() + "\n");}
        writer.flush();
        writer.close();
    }

    /**
     * 返回该文件的BFile，包含所有的level和每个level对应的blocks
     *
     * @return 该文件的BFile
     * @throws NullPointerException
     */
    public LinkedList<Level> getBFile() throws NullPointerException {
        if (bFile.isEmpty()) {throw new NullPointerException("call getFixedFile() first");}
        return bFile;
    }
}
