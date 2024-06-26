package utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class Pinyin {
    public static String getPinYin(String src) {//通过联系人姓名汉字获取对应的拼音(小写字母形式)
        char[] t1;
        t1 = src.replaceAll("\\s", "").toCharArray();//去掉全部何空白字符
        String[] t2;
        HanyuPinyinOutputFormat t3 = new HanyuPinyinOutputFormat();
        t3.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        t3.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        t3.setVCharType(HanyuPinyinVCharType.WITH_V);
        StringBuilder t4 = new StringBuilder();
        try {
            for (char c : t1) {
                // 判断是否为汉字字符
                if (Character.toString(c).matches("[\\u4E00-\\u9FA5]+")) {
                    t2 = PinyinHelper.toHanyuPinyinStringArray(c, t3);
                    t4.append(t2[0]);
                } else {
                    t4.append(c);
                }
            }
            return t4.toString();
        } catch (BadHanyuPinyinOutputFormatCombination e1) {
            e1.printStackTrace();
        }
        return t4.toString();
    }

    public static String getInitialConsonant(String str) {//判断中文字符并返回汉字声母
        char[] t1 = str.replaceAll("\\s", "").toCharArray();
        StringBuilder result = new StringBuilder();
        for (char c : t1) {
            // 判断是否为汉字字符
            if (Character.toString(c).matches("[\\u4E00-\\u9FA5]+")) {
                result.append(getPinYin(Character.toString(c)).charAt(0));
            } else {
                return null;
            }

        }
        return result.toString().isEmpty()?"":result.toString();
    }
}
