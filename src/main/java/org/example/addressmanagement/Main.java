package org.example.addressmanagement;

import ezvcard.VCard;
import ezvcard.property.Address;
import ezvcard.property.Email;
import ezvcard.property.Telephone;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.security.PublicKey;
import java.util.ArrayList;

public class Main{        //....
    public static String[] name = {"*anonymous", "雷军", "扎克伯格", "贝佐斯", "比尔盖茨", "张一鸣", "丁磊", "马云", "马化腾", "马斯克", "巴菲特", "沃尔顿", "拉里佩奇", "谢尔盖布林", "李嘉诚", "何享健", "许家印", "吴清亮", "乔布斯", "李书福", "蔡崇信", "安东尼"};

    public static void main(String[] args) {



        Application.launch(AddressBookFX.class);
    }

}
