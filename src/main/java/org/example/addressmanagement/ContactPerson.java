package org.example.addressmanagement;


import java.util.ArrayList;

public class ContactPerson {
    private String name;
    private String namePinyin;//联系人姓名的拼音
    private ArrayList<String> phoneNumber;
    private ArrayList<String> email;

    public ContactPerson(String name, ArrayList<String> phoneNumber, ArrayList<String> email) {
        this.name = name;
        this.namePinyin = Pinyin.getPinYin(name);
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    


}