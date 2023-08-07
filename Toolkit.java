// Last update: Aug 4, 2023

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.NullPointerException;

public class Toolkit {
    /**
     * 得到指定行的缩进
     *
     * @param s 指定行
     * @return 指定行的缩进
     */
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

    /**
     * 得到指定文件中所有的缩进并排序，空行单独处理
     *
     * @param filePath 指定文件路径
     * @return 包含所有缩进（不含空行）的array
     * @throws FileNotFoundException
     */
    public int[] getIndentArray(String filePath) throws FileNotFoundException {
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

        if (ia[ia.length-1] == Integer.MAX_VALUE) {return Arrays.copyOfRange(ia, 0,ia.length-2);}
        else {return ia;}
    }

    /**
     * 根据特征分析指定行类型，见注释
     *
     * @param s 指定行
     * @return 指定行类型
     */
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
        if (m.matches()) {return "classStartX";}

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
        if (m.matches()) {return "interfaceStartX";}

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
        if (m.matches()) {return "importX";}

        // 单行package(含';')
        p = Pattern.compile("^[ \\t]*package.*\\;[ \\t\\r\\n]*$");
        m = p.matcher(s);
        if (m.matches()) {return "package";}

        // 单行package(不含';')
        p = Pattern.compile("^[ \\t]*package.*");
        m = p.matcher(s);
        if (m.matches()) {return "packageX";}

        // 单行'@'
        p = Pattern.compile("^[ \t]*\\@.*");
        m = p.matcher(s);
        if (m.matches()) {return "annotation";}

        // 单行先'{'再'}'
        p = Pattern.compile("^.*\\{.*\\}[ \\t\\r\\n]*$");
        m = p.matcher(s);
        if (m.matches()) {return "curlyStartEnd";}

        // 单行先'}'再'{'
        p = Pattern.compile("^.*\\}.*\\{[ \\t\\r\\n]*$");
        m = p.matcher(s);
        if (m.matches()) {return "curlyEndStart";}

        // 单行'}'+';'，比如do-while
        p = Pattern.compile(".*\\}.*\\;[ \\t\\r\\n]*$");
        m = p.matcher(s);
        if (m.matches()) {return "curlyEndSemicolon";}

        // 单行'{'
        p = Pattern.compile("^[^\\{]*\\{");
        m = p.matcher(s);
        if (m.matches()) {return "curlyStart";}

        // 单行'}'
        p = Pattern.compile(".*\\}[ \\t\\r\\n]*$");
        m = p.matcher(s);
        if (m.matches()) {return "curlyEnd";}

        // 多行comment塞入一行
        p = Pattern.compile("^[ \\t]*\\/\\*.*\\*\\/[ \\t\\r\\n]*$");
        m = p.matcher(s);
        if (m.matches()) {return "commentPlusStartEnd";}

        // 多行comment开始
        p = Pattern.compile("^[ \\t]*\\/\\*");
        m = p.matcher(s);
        if (m.matches()) {return "commentPlusStart";}

        // 多行comment结束
        p = Pattern.compile(".*\\*\\/[ \\t\\r\\n]*$");
        m = p.matcher(s);
        if (m.matches()) {return "commentPlusEnd";}

        // 多行comment中间（'*'开头）
        p = Pattern.compile("^[ \\t]*\\*.*");
        m = p.matcher(s);
        if (m.matches()) {return "commentPlusMid";}

        // 单行';'
        p = Pattern.compile(".*\\;[ \\t\\r\\n]*$");
        m = p.matcher(s);
        if (m.matches()) {return "semicolon";}

        // 结尾无大括号或分号（中间可以有大括号或分号）
        p = Pattern.compile("[^\\;\\{\\} \\t\\r\\n]+[ \\t\\r\\n]*$");
        m = p.matcher(s);
        if (m.matches()) {return "noSuffix";}

        // 其余的单行或多行
        // TODO: 这类情况包含哪些情形？
        return "others";
    }

    /**
     * 转化成block时将开始行类型转为block类型
     *
     * @param s block开始行
     * @return block类型
     */
    public String typeConverter(String s) {
        if (s.equals("classStart") || s.equals("classStartX") || s.equals("classStartEnd")) {return "class";}
        else if (s.equals("interfaceStart") || s.equals("interfaceStartX") || s.equals("interfaceStartEnd")) {return "interface";}
        else if (s.equals("importX")) {return "import";}
        else if (s.equals("packageX")) {return "package";}
        else if (s.equals("curlyStartEnd") || s.equals("curlyStart")) {return "curly";}
        else if (s.equals("commentPlusStartEnd") || s.equals("commentPlusStart")) {return "commentPlus";}
        else {return s;}
    }

    public String modifyIndent(String s, int newIndent) {
        int pointer = 0;
        String notIndent = s;
        while (pointer < s.length()) {
            if (s.charAt(pointer) != ' ' && s.charAt(pointer) != '\t') {
                notIndent=s.substring(pointer);
                break;
            }
            pointer++;
        }

        for (int i = 0; i < newIndent; i++) {notIndent = " " + notIndent;}

        return notIndent;
    }
}
