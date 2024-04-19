package org.example.addressmanagement;

import ezvcard.parameter.AddressType;
import ezvcard.parameter.ImageType;
import ezvcard.property.*;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.ArrayList;

public class ContactPerson implements Comparable<ContactPerson> {
    private String name;
    private char firstChar;//联系人姓名拼音的首字母
    private String namePinyin;//联系人姓名的拼音
    private ArrayList<String> phoneNumber;

    private ArrayList<String> email;//两种email产生了冲突
//    private static Email email=new Email("234232@qq.com");//vCard 下的email
    Url url=new Url("www.baidu.com");
    Birthday birthday=new Birthday("2023/04/01");
    byte[] photodata;
    Photo photo=new Photo(photodata, ImageType.JPEG);
    Organization organization=new Organization();

    Address adr=new Address();



    private ArrayList<Impp> impps;


    public ContactPerson(String name, ArrayList<String> phoneNumber, ArrayList<String> email) {
        organization.getValues().add("224");
        adr.setStreetAddress("123 Main St.");
        adr.setLocality("Austin");
        adr.setRegion("TX");
        adr.setPostalCode("12345");
        adr.setCountry("USA");
        adr.getTypes().add(AddressType.WORK);

        this.name = name;
        this.namePinyin = getPinYin(name);
        this.firstChar = namePinyin.toUpperCase().charAt(0);
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public char getFirstChar() {
        return firstChar;
    }

    public void setFirstChar(char firstChar) {
        this.firstChar = firstChar;
    }

    public String getNamePinyin() {
        return namePinyin;
    }

    public void setNamePinyin(String namePinyin) {
        this.namePinyin = namePinyin;
    }

    public ArrayList<String> getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(ArrayList<String> phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public ArrayList<String> getEmail() {
        return email;
    }

    public void setEmail(ArrayList<String> email) {
        this.email = email;
    }

    @Override
    public int compareTo(ContactPerson o) {//用联系人的姓名拼音作为比较的主键
        return Integer.compare(this.namePinyin.compareTo(o.namePinyin), 0);
    }

    private String getPinYin(String src) {//通过联系人姓名汉字获取对应的拼音(小写字母形式)
        char[] t1;
        t1 = src.toCharArray();
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

}