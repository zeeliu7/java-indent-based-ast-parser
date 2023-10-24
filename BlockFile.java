import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.lang.IllegalArgumentException;
import java.lang.IndexOutOfBoundsException;

public class BlockFile {
    private final String FILE_PATH;
    private LinkedList<Line> plainLines; // 直接访问所有Line
    private LinkedList<Level> levels; // 间接访问所有Line
    private Toolkit toolkit;
    private LinkedList<Integer> indents;

    public BlockFile(String filePath) throws FileNotFoundException {
        FILE_PATH = filePath;
        plainLines = new LinkedList<>();
        levels = new LinkedList<>();
        toolkit = new Toolkit();
        indents = null;
        initialization();
    }

    private void loadLines() throws FileNotFoundException {
        Scanner sc = new Scanner(new File(FILE_PATH));
        String thisLine = "";
        int counter = 0;
        while (sc.hasNextLine()) {
            thisLine = sc.nextLine();
            plainLines.add(new Line(thisLine, toolkit.getLineIndent(thisLine), toolkit.getLineIndent(thisLine),
                    toolkit.getType(thisLine), toolkit.getRandomId(), counter));
            counter++;
        }

        indents = toolkit.getIndentList(FILE_PATH);
    }

    private void parseLines (int level, int lineStart, int lineEnd) throws FileNotFoundException {

        // 空行标注
        for(Line l : plainLines) {
            if (l.getType().equals("blank")) {
                l.setBlankLine(true);
                l.setStartLine(true);
                l.setEndLine(true);
            }
        }

        // 开始结束行标注
        if (level >= indents.size()) {throw new IndexOutOfBoundsException("illegal level in parseLines()");}
        int thisIndent = indents.get(level);

        int pointer = lineStart;
        boolean openCurly = false;
        boolean marked = true;
        while (pointer <= lineEnd) {
            Line l = plainLines.get(pointer);
            if (!l.isBlankLine() && l.getIndent() == thisIndent) {
                l.setType(toolkit.getType(l.getContent()));
                String thisType = l.getType();

                // 完整单行整体
                if (thisType.equals("comment") || thisType.equals("import")
                        || thisType.equals("package") || thisType.equals("annotation")
                        || thisType.equals("classStartEnd") || thisType.equals("interfaceStartEnd")) {
                    l.setStartLine(true);
                    l.setEndLine(true);
                }

                // 缺损单行整体
                // TODO: 目前只能修复已有"import/package"没有";"的情况，
                //  并且除第一行外只能是semicolon, noSuffix和others，
                //  需要考虑别的情况吗？
                else if (thisType.equals("import+") || thisType.equals("package+")) {
                    l.setStartLine(true);
                    int p = pointer + 1;
                    marked = false;
                    int xp = pointer;
                    while (p < plainLines.size()) {
                        String pType = plainLines.get(p).getType();
                        if (!(pType.equals("semicolon") || pType.equals("noSuffix") || pType.equals("others"))) {
                            plainLines.get(p-1).setxSemi(true);
                            plainLines.get(p-1).setFauxEndLine(true);
                            int fullSplit = p - pointer; // (p-1) - pointer + 1
                            for (int i = 0; i < fullSplit; i++) {
                                plainLines.get(pointer + i).setSplit(true);
                                plainLines.get(pointer + i).setLineSplit(i);
                                plainLines.get(pointer + i).setFullSplit(fullSplit);
                            }
                            pointer = p - 1;
                            marked = true;
                            break;
                        }
                        else if (pType.equals("semicolon")) {
                            plainLines.get(p).setEndLine(true);
                            int fullSplit = p - pointer + 1;
                            for (int i = 0; i < fullSplit; i++) {
                                plainLines.get(pointer + i).setSplit(true);
                                plainLines.get(pointer + i).setLineSplit(i);
                                plainLines.get(pointer + i).setFullSplit(fullSplit);
                            }
                            pointer = p;
                            marked = true;
                            break;
                        }
                        else {p++;}
                    }

                    if (!marked) {
                        plainLines.get(plainLines.size()-1).setFauxEndLine(true);
                        plainLines.get(plainLines.size()-1).setOkay(false);
                        plainLines.get(plainLines.size()-1).setxSemi(true);
                        marked = true;
                    }
                }

                // 多行comment特殊处理
                else if (thisType.equals("longCommentStartEnd")) {
                    l.setStartLine(true);
                    l.setEndLine(true);
                }

                // TODO: 目前只能修复已有"/*"没有"*/"的情况，
                //  并且除第一行外只能是longCommentMid, longCommentEnd, noSuffix和others，
                //  需要考虑别的情况吗？
                else if (thisType.equals("longCommentStart")) {
                    l.setStartLine(true);
                    int p = pointer + 1;
                    marked = false;
                    while (p < plainLines.size()) {
                        String pType = plainLines.get(p).getType();
                        if (!(pType.equals("longCommentMid") || pType.equals("longCommentEnd")
                                || pType.equals("noSuffix") || pType.equals("others"))) {
                            plainLines.get(p-1).setxRightComment(true);
                            plainLines.get(p-1).setFauxEndLine(true);
                            pointer = p - 1;
                            marked = true;
                            break;
                        }
                        else if (plainLines.get(p).getType().equals("longCommentEnd")) {
                            plainLines.get(p).setEndLine(true);
                            pointer = p;
                            marked = true;
                            break;
                        }
                        else {p++;}
                    }
                    if (!marked) {
                        plainLines.get(plainLines.size()-1).setFauxEndLine(true);
                        plainLines.get(plainLines.size()-1).setxRightComment(true);
                    }
                }

                // 单行完整class/interface
                else if (thisType.equals("classStartEnd") || thisType.equals("interfaceStartEnd")) {
                    if (openCurly) {throw new IllegalArgumentException("missing '}' before line " + (pointer+1));}

                    l.setStartLine(true);
                    l.setEndLine(true);
                    openCurly = false;
                }

                // 单行class/interface/大括号开始
                // 注意：同level大括号必须先打开再关上，不能连续有两个左大括号或右大括号
                else if (thisType.equals("classStart") || thisType.equals("interfaceStart")
                        || thisType.equals("curlyStart") || thisType.equals("curlyStart+")) {
                    if (openCurly) {throw new IllegalArgumentException("missing '}' before line " + (pointer+1));}

                    l.setStartLine(true);
                    openCurly = true;

                    // TODO: 目前只能修复已有'{'没有'}'的情况，
                    //  并且相同缩进行中含'{'行必须紧接'}'行
                    //  需要考虑别的情况吗？
                    int p = pointer + 1;
                    marked = false;
                    while (p < plainLines.size()) {
                        if (plainLines.get(p).getIndent() <= thisIndent) {
                            String pType = plainLines.get(p).getType();
                            if ((pType.equals("curlyEnd") || pType.equals("curlyEnd+"))
                                    && (plainLines.get(p).getIndent() == thisIndent)) {
                                pointer = p;
                                marked = true;
                                openCurly = false;
                                break;
                            }
                            else {
                                plainLines.get(p-1).setxRightCurly(true);
                                plainLines.get(p-1).setFauxEndLine(true);
                                marked = true;
                                break;
                            }
                        }
                        p++;
                    }
                    if (!marked) {
                        plainLines.get(plainLines.size()-1).setxRightCurly(true);
                        plainLines.get(plainLines.size()-1).setFauxEndLine(true);
                    }
                }

                // 单行class/interface开始，但是没有大括号
                // TODO: 目前只能修复已有"class/interface"没有"{"的情况，
                //  并且除第一行外只能是curlyStart, curlyStart+, noSuffix和others，
                //  需要考虑别的情况吗？
                else if (thisType.equals("classStart+") || thisType.equals("interfaceStart+")) {
                    if (openCurly) {throw new IllegalArgumentException("missing '}' before line " + (pointer+1));}

                    l.setStartLine(true);
                    int p = pointer + 1;
                    int xp = pointer;
                    while (p < plainLines.size()) {
                        String pType = plainLines.get(p).getType();
                        if (!(pType.equals("curlyStart") || pType.equals("curlyStart+")
                                || pType.equals("noSuffix") || pType.equals("others"))) {
                            plainLines.get(p-1).setxLeftCurly(true);
                            int fullSplit = p - pointer; // (p-1) - pointer + 1
                            for (int i = 0; i < fullSplit; i++) {
                                plainLines.get(pointer + i).setSplit(true);
                                plainLines.get(pointer + i).setLineSplit(i);
                                plainLines.get(pointer + i).setFullSplit(fullSplit);
                            }
                            pointer = p - 1;
                            marked = true;
                            break;
                        }
                        else if (pType.equals("curlyStartEnd")) {
                            plainLines.get(p).setEndLine(true);
                            int fullSplit = p - pointer + 1;
                            for (int i = 0; i < fullSplit; i++) {
                                plainLines.get(pointer + i).setSplit(true);
                                plainLines.get(pointer + i).setLineSplit(i);
                                plainLines.get(pointer + i).setFullSplit(fullSplit);
                            }
                            pointer = p;
                            openCurly = false;
                            marked = true;
                            break;
                        }
                        else if (pType.equals("curlyStart") || pType.equals("curlyStart+")) {
                            int fullSplit = p - pointer + 1;
                            for (int i = 0; i < fullSplit; i++) {
                                plainLines.get(pointer + i).setSplit(true);
                                plainLines.get(pointer + i).setLineSplit(i);
                                plainLines.get(pointer + i).setFullSplit(fullSplit);
                            }
                            pointer = p;
                            openCurly = true;
                            marked = true;
                            break;
                        }
                        else {p++;}
                    }

                    if (!marked) {
                        plainLines.get(plainLines.size()-1).setxLeftCurly(true);
                        plainLines.get(plainLines.size()-1).setFauxEndLine(true);
                    }

                    // TODO: 目前只能修复已有'{'没有'}'的情况，
                    //  并且相同缩进行中含'{'行必须紧接'}'行
                    //  需要考虑别的情况吗？
                    p = pointer + 1;
                    marked = false;
                    while (p < plainLines.size()) {
                        if (plainLines.get(p).getIndent() <= thisIndent) {
                            String pType = plainLines.get(p).getType();
                            if ((pType.equals("curlyEnd") || pType.equals("curlyEnd+"))
                                    && (plainLines.get(p).getIndent() == thisIndent)) {
                                pointer = p;
                                marked = true;
                                openCurly = false;
                                break;
                            }
                            else {
                                plainLines.get(p-1).setxRightCurly(true);
                                plainLines.get(p-1).setFauxEndLine(true);
                                marked = true;
                                break;
                            }
                        }
                        p++;
                    }
                    if (!marked) {
                        plainLines.get(plainLines.size()-1).setxRightCurly(true);
                        plainLines.get(plainLines.size()-1).setFauxEndLine(true);
                    }
                }

                // curlyStartEnd（已经确保是单独一行）
                else if (thisType.equals("curlyStartEnd")) {
                    //因为不知道哪一行缺了括号只能throw exception
                    if (openCurly) {throw new IllegalArgumentException("missing '}' before line " + (pointer+1));}

                    l.setStartLine(true);
                    l.setEndLine(true);
                    openCurly = false;
                }

                // 单行大括号开始
                else if (thisType.equals("curlyStart")) {
                    l.setStartLine(true);
                    openCurly = true;
                }

                else if (thisType.equals("curlyStart+")) {
                    int breakLocation = l.getContent().indexOf("{");
                    String newLineContent = l.getContent().substring(breakLocation + 1);
                    l.setContent(l.getContent().substring(0,breakLocation+1));
                    plainLines.add(pointer+1,
                            new Line(newLineContent, thisIndent, l.getContent().length(),
                                    toolkit.getType(newLineContent), toolkit.getRandomId(), l.getUserY()));
                    plainLines.get(pointer+1).setGlue(true);
                    plainLines.get(pointer+1).setGlueOrder(l.getGlueOrder()+1);
                }

                // 单行大括号结束
                else if (thisType.equals("curlyEnd")) {
                    if (!openCurly) {throw new IllegalArgumentException("missing '{' before line " + (pointer+1));}
                    l.setEndLine(true);
                    openCurly = false;
                }

                else if (thisType.equals("curlyEnd+")) {
                    int breakLocation = l.getContent().indexOf("}");
                    String newLineContent = l.getContent().substring(breakLocation + 1);
                    l.setContent(l.getContent().substring(0,breakLocation+1));
                    plainLines.add(pointer+1,
                            new Line(newLineContent, thisIndent, l.getContent().length(),
                                    toolkit.getType(newLineContent), toolkit.getRandomId(), l.getUserY()));
                    plainLines.get(pointer+1).setGlue(true);
                    plainLines.get(pointer+1).setGlueOrder(l.getGlueOrder()+1);
                }

                // 单行分号
                else if (thisType.equals("semicolon")) {
                    l.setStartLine(true);
                    l.setEndLine(true);
                }

                else if (thisType.equals("semicolon+")) {
                    int breakLocation = l.getContent().indexOf(";");
                    String newLineContent = l.getContent().substring(breakLocation + 1);
                    l.setContent(l.getContent().substring(0,breakLocation+1));
                    plainLines.add(pointer+1,
                            new Line(newLineContent, thisIndent, l.getContent().length(),
                                    toolkit.getType(newLineContent), toolkit.getRandomId(), l.getUserY()));
                    plainLines.get(pointer+1).setGlue(true);
                    plainLines.get(pointer+1).setGlueOrder(l.getGlueOrder()+1);
                }

                // noSuffix和others
                // TODO: 有尝试分析该行的可能性吗？
                else {
                    l.setFauxStartLine(true);
                    l.setFauxEndLine(true);
                    l.setxUnknown(true);
                }
            }

            pointer++;
        }

        if (openCurly) {throw new IllegalArgumentException("missing '}' until EOF");}
        if (!marked) {throw new IllegalArgumentException("broken code not marked");}
    }

    private void formLevel(int thisIndent) {
        int levelId = toolkit.getRandomId();
        boolean formingBlock = false;
        Level thisLevel = new Level(toolkit.getRandomId(), thisIndent, plainLines);
        Block thisBlock = null;
        for (Line l : plainLines) {
            if (((l.isStartLine() || l.isFauxStartLine()) && l.getIndent() == thisIndent) || !formingBlock) {
                formingBlock = true;
                thisBlock = new Block(toolkit.getRandomId(), levelId);
                thisBlock.add(l);
                l.addLevelBlock(levelId, thisBlock.getBlockId());
                thisBlock.setType(toolkit.typeConverter(l.getType()));
            }
            else if ((l.isEndLine() || l.isFauxEndLine()) && l.getIndent() == thisIndent) {
                thisBlock.add(l);
                l.addLevelBlock(levelId, thisBlock.getBlockId());
                thisLevel.add(thisBlock);
                formingBlock = false;
            }
            else {
                thisBlock.add(l);
                l.addLevelBlock(levelId, thisBlock.getBlockId());
            }
        }
    }

    public void initialization() throws FileNotFoundException {
        loadLines();
        for (int i = 0; i < indents.size(); i++) {
            parseLines(i, 0, plainLines.size()-1);
            formLevel(indents.get(i));
        }
    }

    ///////////////////////////////////////////
    // 注意：在IDE中可能用户输入'\n'就自动加上缩进
    // insert的content包含IDE自动补上的缩进
    // delete中删掉的缩进也是按空格逐个删除
    ///////////////////////////////////////////
    public void insert(int userX, int userY, String content) throws NullPointerException, FileNotFoundException {
        try {
            Line pointLine = toolkit.getLineByUser(plainLines, userX, userY);
            int pointLineXStart = pointLine.getUserXStart();
            int pointLineIndex = toolkit.getLineIndexById(plainLines, pointLine.getLineId());
            Line pointLineNext = plainLines.get(toolkit.getLineIndexById(plainLines, pointLine.getLineId()) + 1);

            LinkedList<String> inserted = new LinkedList<>();
            HashSet<Integer> insertIndents = new HashSet<>();
            int contentPtr = 0;
            int startIndex = 0;
            while (contentPtr < content.length()) {
                if (content.charAt(contentPtr) == '\n') {
                    String oneLine = content.substring(startIndex, contentPtr+1);
                    inserted.add(oneLine);
                    startIndex = contentPtr+1;
                }
                contentPtr++;
            }
            if (startIndex < content.length()) {
                String lastLine = content.substring(startIndex);
                inserted.add(lastLine);
            }

            if (!inserted.get(0).isBlank()) {
                insertIndents.add(pointLine.getIndent());
                for (int i = 1; i < inserted.size(); i++) {
                    insertIndents.add(toolkit.getLineIndent(inserted.get(i)));
                }
            } else {
                for (String s : inserted) {
                    insertIndents.add(toolkit.getLineIndent(s));
                }
            }

            for (Integer i : insertIndents) {
                int l = (int) i;
                indents = toolkit.modifyIndentsList(indents, l, true);
            }

            int reparseIndentMin = Collections.min(indents);
            int reparseLevelIndex = 0;
            Level reparseLevel = null;
            Block reparseBlock = null;

            for (int i = 0; i < indents.size(); i++) {
                if (reparseIndentMin == indents.get(i)) {
                    reparseLevelIndex = i;
                    reparseLevel = levels.get(i);
                    break;
                }
            }

            int reparseBlockId = pointLine.getBlockByLevel().get(reparseLevel.getLevelId());
            for (int i = 0; i < reparseLevel.size(); i++) {
                if (reparseLevel.get(i).getBlockId() == reparseBlockId) {
                    reparseBlock = reparseLevel.get(i);
                    break;
                }
            }

            int reparseStartLineId = reparseBlock.getStartId();
            int reparseEndLineId = reparseBlock.getEndId();
            boolean blankFirstLine = inserted.get(0).isBlank();
            boolean brokenPointLine = false;
            String pointLineFirstHalf = pointLine.getContent().substring(0, userX - pointLine.getUserXStart());
            String pointLineSecondHalf = pointLine.getContent().substring(userX - pointLine.getUserXStart());

            String firstLine = inserted.get(0);
            if (userX == pointLineXStart) {
                if (blankFirstLine) {
                    plainLines.add(pointLineIndex + 1, new Line(firstLine, Integer.MAX_VALUE,
                            Integer.MAX_VALUE, "blank", toolkit.getRandomId(), userY + 1));
                } else {
                    plainLines.add(pointLineIndex + 1, new Line(firstLine,
                            toolkit.getLineIndent(pointLine.getContent()),
                            toolkit.getLineIndent(pointLine.getContent()),
                            "blank", toolkit.getRandomId(), userY + 1));
                }
            } else {
                brokenPointLine = true;
                pointLine.setContent(pointLineFirstHalf + inserted.get(0));
            }

            int nextUserY;
            int nextLineIndex;
            if (!brokenPointLine) {
                nextUserY = userY + 2;
                nextLineIndex = pointLineIndex + 2;
            } else {
                nextUserY = userY + 1;
                nextLineIndex = pointLineIndex + 1;
            }
            for (int i = 1; i < inserted.size()-1; i++) {
                String toInsert = inserted.get(i);
                plainLines.add(nextLineIndex, new Line(toInsert, toolkit.getLineIndent(toInsert),
                        toolkit.getLineIndent(toInsert), toolkit.getType(toInsert),
                        toolkit.getRandomId(), nextUserY));
                nextUserY++;
            }

            if (!brokenPointLine) {
                String lastLine = inserted.get(inserted.size()-1);
                plainLines.add(nextLineIndex, new Line(lastLine, toolkit.getLineIndent(lastLine),
                        toolkit.getLineIndent(lastLine), toolkit.getType(lastLine),
                        toolkit.getRandomId(), nextUserY));
            } else {
                String newContent = pointLineSecondHalf + pointLineNext.getContent().trim();
                for (int i = 0; i < pointLineNext.getIndent(); i++) {
                    newContent = " " + newContent;
                }
                pointLineNext.setContent(newContent);
            }

            for (int i = reparseLevelIndex; i < levels.size(); i++) {
                parseLines(i, toolkit.getLineIndexById(plainLines, reparseStartLineId),
                        toolkit.getLineIndexById(plainLines, reparseEndLineId));
            }

            plainLines = toolkit.userYAdjust(plainLines);


        } catch (NullPointerException e1) {throw new NullPointerException("add content error");}
          catch (FileNotFoundException e2) {throw new FileNotFoundException("add content error");}

    }

    public void backspace(int endX, int endY, int charDeleted) throws FileNotFoundException {
        Line endLine = toolkit.getLineByUser(plainLines, endX, endY);
        int endIndex = toolkit.getLineIndexById(plainLines, endLine.getLineId());
        int endStartX = endLine.getUserXStart();
        int endEndX = endLine.getUserXEnd();

        int reparseStartLine = 0;
        int reparseEndLine = plainLines.size()-1;

        boolean endSplit = false;
        boolean fromEndEnd = (endX == (endEndX + 1));
        if (endX - charDeleted > endStartX) {
            endLine.setContent(toolkit.truncate(endLine.getContent(), endX - charDeleted, endX));

            if (endLine.isGlue()) {
                reparseStartLine = toolkit.getGlueZero(plainLines, endIndex);
                reparseEndLine = toolkit.getLineIndexById(plainLines, endLine.getLineId());
                int levelIndex =
                        Math.min(toolkit.getIndentsIndex(indents, plainLines.get(reparseStartLine).getIndent()),
                                toolkit.getIndentsIndex(indents, plainLines.get(reparseEndLine).getIndent()));

                for (int i = levelIndex; i < indents.size(); i++) {
                    parseLines(i, reparseStartLine, reparseEndLine);
                }

                plainLines = toolkit.userYAdjust(plainLines);
            } else {
                reparseStartLine = toolkit.getLineIndexById(plainLines, endLine.getLineId());
                reparseEndLine = toolkit.getLineIndexById(plainLines, endLine.getLineId());
                int levelIndex = toolkit.getIndentsIndex(indents, endLine.getIndent());

                for (int i = levelIndex; i < indents.size(); i++) {
                    parseLines(i, reparseStartLine, reparseEndLine);
                }

                plainLines = toolkit.userYAdjust(plainLines);
            }
        } else if (endX - charDeleted >= 0) {
            if (endLine.isGlue()) {backspace(endX - charDeleted, endY, charDeleted - endLine.getLength());}
            else {
                reparseStartLine = toolkit.getLineIndexById(plainLines, endLine.getLineId());
                reparseEndLine = toolkit.getLineIndexById(plainLines, endLine.getLineId());
                int levelIndex = toolkit.getIndentsIndex(indents, endLine.getIndent());

                for (int i = levelIndex; i < indents.size(); i++) {
                    parseLines(i, reparseStartLine, reparseEndLine);
                }

                plainLines = toolkit.userYAdjust(plainLines);
            }
        } else {
            endLine.setContent(toolkit.truncate(endLine.getContent(), 0, endX));
            reparseEndLine = toolkit.getLineIndexById(plainLines, endLine.getLineId());

            int currY = endY;
            int currIndex = endIndex;
            int currLength = plainLines.get(currIndex).getLength();
            while (currY >= 0 && charDeleted > currLength) {
                currIndex--;
                charDeleted -= plainLines.get(currIndex).getLength();
            }
            int newX = plainLines.get(currIndex).getLength() - charDeleted;
            backspace(newX, currY, charDeleted);
        }
    }

    public BlockFile export() {return this;}
}
