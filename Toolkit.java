// Last update: Sep 8, 2023

/////////////////////////////
// 代码尚在完善过程中且未经测试 //
/////////////////////////////

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.NullPointerException;

public class Toolkit {

    public int getLineIndent(String s) {
        if (s == null || s.isBlank()) {
            return Integer.MAX_VALUE; // 空行特别标记
        }

        int counter = 0;
        int pointer = 0;
        while (pointer < s.length()) {
            if (s.charAt(pointer) != ' ' && s.charAt(pointer) != '\t') {
                break;
            } else {
                if (s.charAt(pointer) == ' ') {counter++;}
                if (s.charAt(pointer) == '\t') {counter += 4;}
                pointer++;
            }
        }
        return counter;
    }

    public LinkedList<Integer> getIndentList(String filePath) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(filePath));
        HashSet<Integer> indents = new HashSet<>();

        while (sc.hasNextLine()) {
            String thisLine = sc.nextLine();
            indents.add(getLineIndent(thisLine));
        }

        if (indents.size() == 0) {throw new NullPointerException("getIndentArray() error");}
        int[] ia = Arrays.stream(indents.toArray(new Integer[indents.size()]))
                .mapToInt(Integer::intValue).toArray();
        Arrays.sort(ia);

        return (LinkedList) Arrays.asList(ia);
    }

    public String getType (String s) {
        if (s == null || s.isBlank()) {return "blank";}

        // 单行class（含'{'+'}'）
        Pattern p = Pattern.compile("(^[ \\t]*class\\s.*\\{.*\\}[ \\t\\r\\n]*$)|(.*[ \\t]class\\s.*\\{.*\\}[ \\t\\r\\n]*$)");
        Matcher m = p.matcher(s);
        if (m.matches()) {return "classStartEnd";}

        // 单行或多行class(含'{')
        p = Pattern.compile("(^[ \\t]*class\\s.*\\{[ \\t\\r\\n]*$)|(.*[ \\t]class\\s.*\\{[ \\t\\r\\n]*$)");
        m = p.matcher(s);
        if (m.matches()) {return "classStart";}

        // 单行或多行class(不含'{')
        p = Pattern.compile("(^[ \\t]*class\\s.*)|(.*[ \\t]class\\s.*)");
        m = p.matcher(s);
        if (m.matches()) {return "classStart+";}

        // 单行interface（含'{'+'}'）
        p = Pattern.compile("(^[ \\t]*interface\\s.*\\{.*\\}[ \\t\\r\\n]*$)|(.*[ \\t]interface\\s.*\\{.*\\}[ \\t\\r\\n]*$)");
        m = p.matcher(s);
        if (m.matches()) {return "interfaceStartEnd";}

        // 单行或多行interface(含'{')
        p = Pattern.compile("(^[ \\t]*interface\\s.*\\{[ \\t\\r\\n]*$)|(.*[ \\t]interface\\s.*\\{[ \\t\\r\\n]*$)");
        m = p.matcher(s);
        if (m.matches()) {return "interfaceStart";}

        // 单行或多行interface(不含'{')
        p = Pattern.compile("(^[ \\t]*interface\\s.*)|(.*[ \\t]interface\\s.*)");
        m = p.matcher(s);
        if (m.matches()) {return "interfaceStart+";}

        // 单行comment
        p = Pattern.compile("^[ \\t]*\\/\\/.*");
        m = p.matcher(s);
        if (m.matches()) {return "comment";}

        // 单行import(含';')
        p = Pattern.compile("^[ \\t]*import.*\\;[ \\t\\r\\n]*$");
        m = p.matcher(s);
        if (m.matches()) {return "import";}

        // 单行import(不含';')
        p = Pattern.compile("^[ \\t]*import.*");
        m = p.matcher(s);
        if (m.matches()) {return "import+";}

        // 单行package(含';')
        p = Pattern.compile("^[ \\t]*package.*\\;[ \\t\\r\\n]*$");
        m = p.matcher(s);
        if (m.matches()) {return "package";}

        // 单行package(不含';')
        p = Pattern.compile("^[ \\t]*package.*");
        m = p.matcher(s);
        if (m.matches()) {return "package+";}

        // 单行'@'
        p = Pattern.compile("^[ \t]*\\@.*");
        m = p.matcher(s);
        if (m.matches()) {return "annotation";}

        // 单行先'{'再'}'
        p = Pattern.compile("^.*\\{.*\\}[ \\t\\r\\n]*$");
        m = p.matcher(s);
        if (m.matches()) {return "curlyStartEnd";}

        // 单行'{'后无内容
        p = Pattern.compile(".*\\{[ \\t\\r\\n]*$");
        m = p.matcher(s);
        if (m.matches()) {return "curlyStart";}

        // 单行'{'后有内容
        p = Pattern.compile("^.*\\{");
        m = p.matcher(s);
        if (m.matches()) {return "curlyStart+";}

        // 单行'}'后无内容
        p = Pattern.compile(".*\\}[ \\t\\r\\n]*$");
        m = p.matcher(s);
        if (m.matches()) {return "curlyEnd";}

        // 单行'}'后有内容
        p = Pattern.compile("^.*\\}");
        m = p.matcher(s);
        if (m.matches()) {return "curlyEnd+";}

        // 单行';'后无内容
        p = Pattern.compile(".*\\;[ \\t\\r\\n]*$");
        m = p.matcher(s);
        if (m.matches()) {return "semicolon";}

        // 单行';'后有内容
        p = Pattern.compile("^.*\\;");
        m = p.matcher(s);
        if (m.matches()) {return "semicolon+";}

        // 多行comment塞入一行
        p = Pattern.compile("^[ \\t]*\\/\\*.*\\*\\/[ \\t\\r\\n]*$");
        m = p.matcher(s);
        if (m.matches()) {return "longCommentStartEnd";}

        // 多行comment开始
        p = Pattern.compile("^[ \\t]*\\/\\*");
        m = p.matcher(s);
        if (m.matches()) {return "longCommentStart";}

        // 多行comment结束
        p = Pattern.compile(".*\\*\\/[ \\t\\r\\n]*$");
        m = p.matcher(s);
        if (m.matches()) {return "longCommentEnd";}

        // 多行comment中间（'*'开头）
        p = Pattern.compile("^[ \\t]*\\*.*");
        m = p.matcher(s);
        if (m.matches()) {return "longCommentMid";}

        // 结尾无大括号或分号（中间可以有大括号或分号）
        p = Pattern.compile("[^\\;\\{\\} \\t\\r\\n]+[ \\t\\r\\n]*$");
        m = p.matcher(s);
        if (m.matches()) {return "noSuffix";}

        // 其余的单行或多行
        // TODO: 这类情况包含哪些情形？
        return "others";
    }

    public String typeConverter(String s) {
        if (s.equals("classStart") || s.equals("classStart+") || s.equals("classStartEnd")) {return "class";}
        else if (s.equals("interfaceStart") || s.equals("interfaceStart+") || s.equals("interfaceStartEnd")) {return "interface";}
        else if (s.equals("import+")) {return "import";}
        else if (s.equals("package+")) {return "package";}
        else if (s.equals("curlyStartEnd") || s.equals("curlyStart")) {return "curly";}
        else if (s.equals("commentPlusStartEnd") || s.equals("commentPlusStart")) {return "commentPlus";}
        else {return s;}
    }

    public int getRandomId() {return ThreadLocalRandom.current().nextInt();}

    public LinkedList<Integer> modifyIndentsList(LinkedList<Integer> list, int newIndent, boolean add) {
        if (add) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) == newIndent) {return list;}
            }
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) > newIndent) {list.add(i, newIndent); return list;}
            }
            list.add(newIndent);
            return list;
        } else {
            list.remove((Integer) newIndent);
            return list;
        }
    }

    public int getUserYByLine(LinkedList<Line> list, int index) {
        int counter = 0;
        for (int i = 0; i <= index; i++) {
            if (list.get(i).isGlue()) {counter++;}
        }
        return index - counter;
    }

    public Line getLineByUser(LinkedList<Line> list, int userX, int userY) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getUserY() == userY) {
                int start = i;
                while (list.get(start).getUserY() == userY) {
                    if (list.get(start).getUserXEnd() < userX) {start++;}
                }
                return list.get(start);
            }
        }
        return null;
    }

    public int getLineIndexById(LinkedList<Line> list, int id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getLineId() == id) {return i;}
        }
        return -1;
    }

    public int getIndentsIndex(LinkedList<Integer> list, int indent) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == indent) {return i;}
        }
        return -1;
    }

    public int getGlueZero(LinkedList<Line> list, int index) {
        while (index >= 0) {
            if (list.get(index).isGlue()) {return index;}
            index--;
        }
        return -1;
    }

    public String truncate(String s, int removeStart, int removeEnd) {
        String first = s.substring(0, removeStart);
        String second = s.substring(removeEnd);
        return first + second;
    }

    public LinkedList<Line> userYAdjust(LinkedList<Line> list) {
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setUserY(getUserYByLine(list, i));
        }
        return list;
    }
}
