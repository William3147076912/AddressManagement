package org.example.addressmanagement;

import ezvcard.VCard;
import ezvcard.property.Email;
import ezvcard.property.Telephone;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Pattern;

public class AddressBookFX extends Application {

    static final int BLOCK_NUM = 27;//链条(JavaFX界面中分块)的个数
    static Label[] anchorLabels = new Label[BLOCK_NUM];//点击导航标签
    static int activeLabelIndex = 0;//被点击的当前的活跃标签
    static AddressBook addressBook = new AddressBook();
    static AddressBookHeadNode[] addressBookHeadNodes = addressBook.getAddressBookHeadNodes();
    static ArrayList<VCard> contactPeopleArr = new ArrayList<>();//用来存放展示在FX界面的上的所有联系人
    static ObservableList<String> contactPersonOL;//用于存放主ListView中显示的数据（字符串类型的联系人姓名）
    static ListView<String> contactPersonLV;//主ListView
    static int[] blocksContactPeopleNum = new int[BLOCK_NUM];//用于存放当前块以及之前所有块所含联系人以及标签数目之和
    static Stage contactPersonInformationStage;//用于显示联系人信息的舞台


    static {
        Platform.runLater(() -> contactPersonInformationStage = new Stage());
        anchorLabels[0] = new Label("#");//初始化右侧导航栏数据
        for (int i = 0; i < anchorLabels.length - 1; i++) {
            anchorLabels[i + 1] = new Label(String.valueOf((char) ('A' + i)));
        }
        String[] name = {"*anonymous", "雷军", "扎克伯格", "贝佐斯", "比尔盖茨", "张一鸣", "丁磊", "马云", "马化腾", "马斯克", "巴菲特", "沃尔顿", "拉里佩奇", "谢尔盖布林", "李嘉诚", "何享健", "许家印", "吴清亮", "乔布斯", "李书福", "蔡崇信", "安东尼"};
        for (int i = name.length; i < name.length * 2; i++) {//初始化通讯录信息
            ArrayList<String> phoneNumber = new ArrayList<>();
            ArrayList<String> email = new ArrayList<>();
            phoneNumber.add("159989606" + i);
            email.add(i + "0117522@qq.com");
            VCard person=new VCard();
            person.setFormattedName(name[i - name.length]);
            Telephone telephone=new Telephone(phoneNumber.get(0));
            person.addTelephoneNumber(telephone);

            Email email1=new Email(email.get(0));
            person.addEmail(email1);
            addressBook.add(person);
        }

/*        for (int i = 0; i < addressBookHeadNodes.length; i++) {//初始化范围递增块包含的联系人的数目以及标签的数目之和
            for (int j = 0; j <= i; j++) {
                if (addressBookHeadNodes[j].getNumberOfLinked() != 0)
                    blocksContactPeopleNum[i]++;
                blocksContactPeopleNum[i] += addressBookHeadNodes[j].getNumberOfLinked();
            }
            blocksContactPeopleNum[i]--;//因为ListView初始下标为0，所以为了后续定位准确将每个块元素的总数-1；
        }*/

    }

    /**
     * 更新用来存放展示在FX界面上所有联系人的对象--contactPeopleArr
     * 创建新的用于存放主ListView中显示的数据的对象
     * 更新用于存放当前块以及之前所有块所含联系人以及标签数目之和的对象
     *
     * @return ObservableList<String>类型 用于存放主ListView中显示的数据的对象--blocksContactPeopleNum
     */
    public static ObservableList<String> updateContactPeopleArrAndListViewAndBlocksContactPeopleNumData() {
        contactPeopleArr.clear();//清空用于存放原来展示在FX界面上所有联系人的对象
        ObservableList<String> contactPersonOL = FXCollections.observableArrayList();//创建新的用于存放主ListView中显示的数据的对象
        for (AddressBookHeadNode addressBookHeadNode : addressBookHeadNodes) {
            if (addressBookHeadNode.getNumberOfLinked() != 0) {//若链表中含有联系人，则在JavaFX界面中展示该链表头结点存放的标志(#或大写字母)
                contactPersonOL.add(String.valueOf(addressBookHeadNode.getSymbol()));
                contactPeopleArr.add(new VCard());//ContactPerson(" ", new ArrayList<>(), new ArrayList<>())与标志相对应创建一个空对象，便于后续点击跳转事件方法的处理
                AddressBookNode pointer = addressBookHeadNode.getFirstNode();
                while (pointer != null) {//遍历将对应链条中每个联系人的姓名(字符串类型)存储在存放主ListView中显示的数据的对象中
                    contactPersonOL.add(pointer.getData().getFormattedName().getValue());
                    contactPeopleArr.add(pointer.getData());
                    pointer = pointer.getNext();
                }
            }
        }
        for (int i = 0; i < addressBookHeadNodes.length; i++) {//更新(初始化)范围递增块包含的联系人的数目以及标签的数目之和
            blocksContactPeopleNum[i] = 0;
            for (int j = 0; j <= i; j++) {//遍历每个头结点
                if (addressBookHeadNodes[j].getNumberOfLinked() != 0) {
                    blocksContactPeopleNum[i]++;//标签数目
                }
                blocksContactPeopleNum[i] += addressBookHeadNodes[j].getNumberOfLinked();//每个链表中表头结点（首结点）存放的链表中包含的联系人的数目
            }
            blocksContactPeopleNum[i]--;//因为ListView初始下标为0，所以为了后续定位准确将每个块元素的总数-1；!important
        }
        return contactPersonOL;
    }

    public static void appendWarningAlert() {//追加TextField文本输入框时,提示电话号码与邮箱最多储存量的弹窗
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning Dialog");
        alert.setHeaderText("Append Warning");
        alert.setContentText("Store up to three!");
        alert.showAndWait();
    }

    public static void enterEmptyWarningAlert() {//联系人手机号码输入为空时的提示弹框
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning Dialog");
        alert.setHeaderText("Empty Warning");
        alert.setContentText("The phone number cannot be empty!");
        alert.showAndWait();
    }

    public static void enterErrorAlert() {//输入的电话号码或邮箱不符合格式时的提示弹框
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Dialog");
        alert.setHeaderText("Enter Error");
        alert.setContentText("Incorrect phone number or email address entered!");
        alert.showAndWait();
    }

    public static void searchNullAlert() {//搜索不到内容时的提示弹框
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Search Dialog");
        alert.setHeaderText("Search Null");
        alert.setContentText("Did not find the result of the query you were looking for!");
        alert.showAndWait();
    }

    public static void confirmDeleteAlert(String name, String firstPhoneNumber) {//删除联系人时，确认是否删除的确认弹框
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Confirm Delete");
        alert.setContentText("Are you sure you want to delete this contact?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {//若用户点击确认删除
            addressBook.remove(name, firstPhoneNumber);//在底层数据中删除
            contactPersonOL = updateContactPeopleArrAndListViewAndBlocksContactPeopleNumData();//更新显示在JavaFX中的数据以及辅助数据
            contactPersonLV.setItems(contactPersonOL);//更新ListView界面
            contactPersonInformationStage.hide();//隐藏查看联系人信息界面
        }
    }

    /**
     * 将修改联系人信息界面和新建联系人界面的创建联系人对象的代码进行封装复用
     *
     * @param nameTf         姓名输入框
     * @param phoneNumberTfs 一个或多个电话号码输入框
     * @param emailTfs       一个或多个邮箱输入框输入框
     * @return ContactPerson类型的联系人对象
     */
    public static VCard createContactPerson(TextField nameTf, ArrayList<TextField> phoneNumberTfs, ArrayList<TextField> emailTfs) {//使用修改联系人信息界面和新建联系人界面的信息创建联系人对象并返回
        String name = nameTf.getText();
        String phoneNumber;
        ArrayList<String> phoneNumberArr = new ArrayList<>();
        String email;
        ArrayList<String> emailArr = new ArrayList<>();
        for (TextField phoneNumberTf : phoneNumberTfs) {
            phoneNumber = phoneNumberTf.getText();
            if (phoneNumber.isEmpty()) {
                continue;//电话号码输入为空这直接跳过
            }
            if (!Pattern.matches("[0-9]{11}", phoneNumber)) {//用正则表达式判断用户输入的电话号码是否规范
                enterErrorAlert();//若不规范，提示用户输入错误并且终止联系人对象的创建，直接返回
                return null;
            }
            if (!phoneNumberArr.contains(phoneNumber))//若用户输入重复的电话号码,则只保留重复电话号码中的第一个
            {
                phoneNumberArr.add(phoneNumber);
            }
        }
        for (TextField emailTf : emailTfs) {
            email = emailTf.getText();
            if (email.isEmpty()) {
                continue;//邮箱输入为空这直接跳过
            }
            if (!Pattern.matches("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$", email)) {//用正则表达式判断用户输入的邮箱是否规范
                enterErrorAlert();
                return null;
            }
            if (!emailArr.contains(email)) {
                emailArr.add(email);
            }
        }
        if (phoneNumberArr.isEmpty()) {//通讯录内联系人电话号码不能为空。若用户填写为空，则弹框提示并且终止联系人对象的创建，直接返回
            enterEmptyWarningAlert();
            return null;
        }
//        VCard person=new VCard();
//        person.setFormattedName(name[i - name.length]);
//        Telephone telephone=new Telephone(phoneNumber.get(i- name.length));
//        person.addTelephoneNumber(telephone);
//
//        Email email1=new Email(email.get(i-name.length));
//        person.addEmail(email1);
//
        VCard person=new VCard();
        person.setFormattedName(name);
        Telephone telephone=null;
        for(String phonenumber:phoneNumberArr)
        {
            telephone=new Telephone(phonenumber);
            person.addTelephoneNumber(telephone);
        }
        Email email1=null;
        for(String emai:emailArr)
        {
            email1=new Email(emai);
            person.addEmail(email1);
        }
        return person;
    }


    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {//初始主界面
        TextField searchTf = new TextField();
        Button searchBt = new Button("搜索");
        Button addContactPersonBt = new Button("添加联系人");
        HBox searchOrAddPane = new HBox(searchTf, searchBt, addContactPersonBt);
        searchOrAddPane.setSpacing(10);//设置结点间水平间隔
        searchOrAddPane.setPadding(new Insets(16, 16, 6, 16));//设置内边距

/*        ObservableList<String> contactPersonOL = FXCollections.observableArrayList();
        for (AddressBookHeadNode addressBookHeadNode : addressBookHeadNodes) {
            if (addressBookHeadNode.getNumberOfLinked() != 0) {
                contactPersonOL.add(String.valueOf(addressBookHeadNode.getSymbol()));
                contactPeopleArr.add(new ContactPerson("anonymous", new ArrayList<>(), new ArrayList<>()));
                AddressBookNode pointer = addressBookHeadNode.getFirstNode();
                while (pointer != null) {
                    contactPersonOL.add(pointer.getData().getName());
                    contactPeopleArr.add(pointer.getData());
                    pointer = pointer.getNext();
                }
            }
        }*/
        contactPersonOL = updateContactPeopleArrAndListViewAndBlocksContactPeopleNumData();//初始化原始填充界面的数据信息
        contactPersonLV = new ListView<>(contactPersonOL);
        contactPersonLV.setPrefWidth(360);//设置ListView的水平宽度

        VBox anchorPane = new VBox(2);//垂直导航条
        anchorPane.setPadding(new Insets(30, 10, 0, 10));

        for (Label anchorLabel : anchorLabels) {
            anchorPane.getChildren().add(anchorLabel);//用(#和字母标签)填充垂直导航条面板
        }

        HBox contactPersonPane = new HBox(contactPersonLV, anchorPane);//包含联系人和垂直导航条的面板
        contactPersonPane.setPadding(new Insets(8, 0, 16, 16));

        BorderPane addressBookPane = new BorderPane(contactPersonPane);//主面板
        addressBookPane.setTop(searchOrAddPane);

        Scene scene = new Scene(addressBookPane, 414, 736);

        for (int i = 0; i < anchorLabels.length; i++) {
            int finalI = i;//这里涉及变量作用域问题
            int finalI1 = i;
            anchorLabels[i].setOnMouseClicked(e -> {//为导航栏的各个标签设置点击事件
                anchorLabels[activeLabelIndex].setStyle("-fx-font-weight: normal");//标签前端样式改变
                anchorLabels[activeLabelIndex].setTextFill(Color.BLACK);
                anchorLabels[finalI].setTextFill(Color.LIGHTSKYBLUE);
                anchorLabels[finalI].setStyle("-fx-font-weight: bold");
                activeLabelIndex = finalI;//设置活动标签下标为此时点击的标签下标
                /*int blockIndex = 0;//确定联系人列表中应该处于被选中状态的条目的下标
                for (int j = 0; j < finalI1; j++) {
                    if (addressBookHeadNodes[j].getNumberOfLinked() != 0) {
                        blockIndex++;//刚刚好的“错位”
                        blockIndex += addressBookHeadNodes[j].getNumberOfLinked();
                    }
                }*/
                int blockIndex;
                if (finalI1 == 0) {
                    blockIndex = 0;
                } else {
                    blockIndex = blocksContactPeopleNum[finalI1 - 1] + 1;
                }
                contactPersonLV.getSelectionModel().selectIndices(blockIndex);//使应该被选中的条目被选中
                contactPersonLV.scrollTo(blockIndex);//使列表滚动到被选中条目的位置
            });
        }

        contactPersonLV.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            int index = 0;//点击ListVie内的选项时，右边的导航栏响应，被点击的选项对应的块的标识部分的字母会变为浅蓝色并加粗显示
            for (; index < blocksContactPeopleNum.length; index++)//确定被点击条目所在块(以及其所在块对应的垂直导航对应标签在存放其数组中)的下标
            {
                if ((Integer) newValue <= blocksContactPeopleNum[index]) {
                    break;
                }
            }
            anchorLabels[activeLabelIndex].setStyle("-fx-font-weight: normal");//修改标签的前端样式
            anchorLabels[activeLabelIndex].setTextFill(Color.BLACK);//将原来被选中的标签恢复为正常的样式
            anchorLabels[index].setStyle("-fx-font-weight: bold");
            anchorLabels[index].setTextFill(Color.LIGHTSKYBLUE);
            activeLabelIndex = index;//将指示活跃标签的下标更新为此时选中标签的下标


            if ((Integer) newValue == 0 || index != 0 && (Integer) newValue == blocksContactPeopleNum[index - 1] + 1) {
                return;
            }
            //使用户点击ListView中的A B C D...等选项时，无弹框信息响应,巧妙的运用了"||"短路的特点避免了数组越界异常抛出
            //&&的优先级大于||
            VCard person;
            try {//避免删除ListView中联系人后，由于事件监听而自动抛出数组越界异常;
                person = contactPeopleArr.get((Integer) newValue);
            } catch (Exception e) {
                return;
            }
            GridPane cPInformGridPane = new GridPane();//查看联系人信息界面
            cPInformGridPane.setHgap(5);
            cPInformGridPane.setVgap(12);

            int curRowIndex = 1;//这个变量很重要,记录当前要向GridPane界面中填充TextFiled文本输入框结点的行标
            TextField nameTf = new TextField(person.getFormattedName().getValue());
            nameTf.setEditable(false);/*用户不可以修改姓名(根据普遍情况人们也不会轻易修改联系人姓名，而多是修改联系人更换的电话号码)
                                       若想要修改姓名需要删除联系人后，新建联系人，将姓名和电话号码均修改*/
            Button addPhoneNumberBt = new Button("+");
            ArrayList<TextField> phoneNumberTfs = new ArrayList<>();
            for (int i = 0; i < person.getTelephoneNumbers().size(); i++) {
                phoneNumberTfs.add(new TextField(person.getTelephoneNumbers().get(i).getText()));
            }

            Label emailLabel = new Label("邮箱:");
            Button addEmailBt = new Button("+");
            ArrayList<TextField> emailTfs = new ArrayList<>();
            for (int i = 0; i < person.getEmails().size(); i++) {
                emailTfs.add(new TextField(person.getEmails().get(i).getValue()));
            }
            cPInformGridPane.add(new Label("姓名:"), 0, 0);
            cPInformGridPane.add(nameTf, 1, 0);
            cPInformGridPane.add(new Label("电话:"), 0, 1);
            cPInformGridPane.add(addPhoneNumberBt, 2, 1);
            for (TextField phoneNumberTf : phoneNumberTfs) {//利用GridPane和for循环使FX界面可以根据存储的单个联系人的电话号码以及邮箱的的多少显示相应多个
                cPInformGridPane.add(phoneNumberTf, 1, curRowIndex++);
            }
            cPInformGridPane.add(emailLabel, 0, curRowIndex);
            cPInformGridPane.add(addEmailBt, 2, curRowIndex);
            for (TextField emailTf : emailTfs) {
                cPInformGridPane.add(emailTf, 1, curRowIndex++);
            }

            Button saveBt = new Button("保存");//控制按钮界面  将两个按钮独立放到一个界面内而不是放到上面的GridPane内，使后续操作更加方便
            Button removeBt = new Button("删除");//不需要考虑两者的删除和再添加问题
            HBox cPInformButtonPane = new HBox(25);
            cPInformButtonPane.setAlignment(Pos.TOP_RIGHT);//设置面板内结点的位置
            cPInformButtonPane.setPadding(new Insets(10, 0, 100, 0));
            cPInformButtonPane.getChildren().addAll(saveBt, removeBt);

            BorderPane cPInformPane = new BorderPane(cPInformGridPane);//查看联系人信息的主界面
            cPInformPane.setBottom(cPInformButtonPane);
            cPInformPane.setPadding(new Insets(30));

            Scene cPInformScene = new Scene(cPInformPane, 350, 360);

            addPhoneNumberBt.setOnAction(e -> {//添加输入联系人电话号码的TestField输入框
                if (phoneNumberTfs.size() >= 3) {//若点击添加按钮前输入框已经等于3，提示用户不可再添加，并直接返回
                    appendWarningAlert();
                    return;
                }
                phoneNumberTfs.add(new TextField());//巧妙利用phoneNumberTfs.size()作为添加的文本输入框的行标
                cPInformGridPane.add(phoneNumberTfs.get(phoneNumberTfs.size() - 1), 1, phoneNumberTfs.size());
                cPInformGridPane.getChildren().remove(emailLabel);//移出原本显示“邮箱”的标签，添加邮箱的按钮以及所有邮箱的文本输入框
                cPInformGridPane.getChildren().remove(addEmailBt);
                for (TextField tf : emailTfs) {
                    cPInformGridPane.getChildren().remove(tf);
                }

                int curRowIndex1 = phoneNumberTfs.size() + 1;//这个变量也很重要，指示当前要重新添加到GridPane内的邮箱的文本输入框的行标
                cPInformGridPane.add(emailLabel, 0, curRowIndex1);
                cPInformGridPane.add(addEmailBt, 2, curRowIndex1);
                for (TextField emailTf : emailTfs) {
                    cPInformGridPane.add(emailTf, 1, curRowIndex1++);
                }

            });

            addEmailBt.setOnAction(e -> {//添加输入联系人邮箱的TestField输入框
                if (emailTfs.size() >= 3) {
                    appendWarningAlert();
                    return;
                }
                emailTfs.add(new TextField());
                cPInformGridPane.add(emailTfs.get(emailTfs.size() - 1), 1, phoneNumberTfs.size() + emailTfs.size() + 1);
                //巧妙的利用了phoneNumberTfs.size()+emailTfs.size()作为添加邮箱文本输入框的行标
            });

            /**
             * 修改时,用于展示的contactPeopleArr内的数据以及AddressBook内相应链表内的数据都要更新
             * 缺点：没有判断用户是否修改了联系人信息，每次点击都会执行相应方法，删除联系人，在将由当前联系人信息界面信息获取创建联系人，重新添加到相应位置
             */
            saveBt.setOnAction(e -> {//修改当前被选中的查看联系人信息界面的联系人的信息
                VCard contactPerson = createContactPerson(nameTf, phoneNumberTfs, emailTfs);
                if (contactPerson == null) {
                    return;
                }
                int nodeIndexInLinked;//存储待修改对象在当前链表（块）中的“下标”
                if (activeLabelIndex == 0) {
                    nodeIndexInLinked = (int) newValue;//待修改联系人存储在一条链表的情况
                } else {
                    nodeIndexInLinked = (int) newValue - blocksContactPeopleNum[activeLabelIndex - 1] - 1;//待修改联系人存储在其他链表的情况
                }
                addressBook.modify(contactPerson, nodeIndexInLinked);//底层数据修改
                contactPeopleArr.remove((int) newValue);//前台点击显示数据修改
                contactPeopleArr.add((int) newValue, contactPerson);
                contactPersonInformationStage.hide();//隐藏查看联系人信息界面
            });

            /**
             * 限制：可以在用户不修改联系人第一个手机号码的情况下完成底层联系人数据删除
             */
            removeBt.setOnAction(e -> {//删除当前被选中的查看联系人信息界面的联系人的信息
                String name = nameTf.getText();
                String phoneNumber = phoneNumberTfs.get(0).getText();
                confirmDeleteAlert(name, phoneNumber);
/*                String name = nameTf.getText();
                String phoneNumber = phoneNumberTfs.get(0).getText();
                addressBook.remove(name,phoneNumber);//在底层数据中删除
*//*                contactPeopleArr.remove((int) newValue);//前台点击显示数据中删除
                contactPersonOL.remove((int) newValue);//在显示的列表中删除*//*//这种情况会在删除只含有一个联系人信息的block后出现"数据紊乱";
                contactPersonOL = updateContactPeopleArrAndListViewAndBlocksContactPeopleNumData();
                contactPersonLV.setItems(contactPersonOL);
                contactPersonInformationStage.hide();*/
            });

            contactPersonInformationStage.setScene(cPInformScene);
            contactPersonInformationStage.setTitle("ContactPerson");
            contactPersonInformationStage.setResizable(false);
            contactPersonInformationStage.show();
        });
        /*new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            }*/

        /**
         * 点击添加联系人按钮触发的事件，代码查看联系人信息界面类似
         */
        addContactPersonBt.setOnAction(event -> {//显示添加联系人界面
            Stage createContactPersonStage = new Stage();

            GridPane createContactPersonGridPane = new GridPane();
            createContactPersonGridPane.setHgap(5);
            createContactPersonGridPane.setVgap(12);

            int curRowIndex = 1;
            TextField nameTf = new TextField();

            Button addPhoneNumberBt = new Button("+");
            ArrayList<TextField> phoneNumberTfs = new ArrayList<>();
            phoneNumberTfs.add(new TextField());

            Label emailLabel = new Label("邮箱:");
            Button addEmailBt = new Button("+");
            ArrayList<TextField> emailTfs = new ArrayList<>();
            emailTfs.add(new TextField());

            createContactPersonGridPane.add(new Label("姓名:"), 0, 0);
            createContactPersonGridPane.add(nameTf, 1, 0);
            createContactPersonGridPane.add(new Label("电话:"), 0, 1);
            createContactPersonGridPane.add(addPhoneNumberBt, 2, 1);
            for (TextField phoneNumberTf : phoneNumberTfs) {
                createContactPersonGridPane.add(phoneNumberTf, 1, curRowIndex++);
            }

            createContactPersonGridPane.add(emailLabel, 0, curRowIndex);
            createContactPersonGridPane.add(addEmailBt, 2, curRowIndex);
            for (TextField emailTf : emailTfs) {
                createContactPersonGridPane.add(emailTf, 1, curRowIndex++);
            }
            Button saveBt = new Button("保存");
            Button cancelBt = new Button("取消");
            HBox createContactPersonButtonPane = new HBox(25);
            createContactPersonButtonPane.setAlignment(Pos.TOP_RIGHT);
            createContactPersonButtonPane.setPadding(new Insets(10, 0, 100, 0));
            createContactPersonButtonPane.getChildren().addAll(saveBt, cancelBt);

            BorderPane createContactPersonPane = new BorderPane(createContactPersonGridPane);
            createContactPersonPane.setBottom(createContactPersonButtonPane);
            createContactPersonPane.setPadding(new Insets(30));


            addPhoneNumberBt.setOnAction(e -> {
                if (phoneNumberTfs.size() >= 3) {
                    appendWarningAlert();
                    return;
                }
                phoneNumberTfs.add(new TextField());
                createContactPersonGridPane.add(phoneNumberTfs.get(phoneNumberTfs.size() - 1), 1, phoneNumberTfs.size());
                createContactPersonGridPane.getChildren().remove(emailLabel);
                createContactPersonGridPane.getChildren().remove(addEmailBt);
                for (TextField tf : emailTfs) {
                    createContactPersonGridPane.getChildren().remove(tf);
                }

                int curRowIndex1 = phoneNumberTfs.size() + 1;
                createContactPersonGridPane.add(emailLabel, 0, curRowIndex1);
                createContactPersonGridPane.add(addEmailBt, 2, curRowIndex1);
                for (TextField emailTf : emailTfs) {
                    createContactPersonGridPane.add(emailTf, 1, curRowIndex1++);
                }
            });

            addEmailBt.setOnAction(e -> {
                if (emailTfs.size() >= 3) {
                    appendWarningAlert();
                    return;
                }
                emailTfs.add(new TextField());
                createContactPersonGridPane.add(emailTfs.get(emailTfs.size() - 1), 1, phoneNumberTfs.size() + emailTfs.size() + 1);
                //巧妙的利用了phoneNumberTfs.size()+emailTfs.size()
            });


            /**
             * 修改时,用于展示的contactPeopleArr内的数据以及AddressBook内相应链表内的数据都要更新
             */
            saveBt.setOnAction(e -> {//使用添加联系人界面的信息创建联系人并添加该联系人
                VCard contactPerson = createContactPerson(nameTf, phoneNumberTfs, emailTfs);
                if (contactPerson == null) {
                    return;
                }
                addressBook.add(contactPerson);
                contactPersonOL = updateContactPeopleArrAndListViewAndBlocksContactPeopleNumData();
                contactPersonLV.setItems(contactPersonOL);//更新界面显示，修改ListView界面展示所有联系人的信息
                createContactPersonStage.hide();//隐藏添加联系人界面
            });

            cancelBt.setOnAction(e -> createContactPersonStage.hide());//隐藏添加联系人界面

            Scene createContactPersonScene = new Scene(createContactPersonPane, 350, 370);
            createContactPersonStage.setScene(createContactPersonScene);
            createContactPersonStage.setTitle("Create Contact Person");
            createContactPersonStage.setResizable(false);//设置用户不可以拖动边框修改创建联系人界面的Stage的大小
            createContactPersonStage.show();
        });

        /**
         *模糊查询(搜索)需要遍历所有联系人的所有信息,若查找到则返回显示该联系人
         * 该方法支持：姓名（或姓名子集）、姓名拼音大小写（或姓名拼音大小写子集）、电话号码（或电话号码子集）、邮箱（或邮箱子集）查询
         *
         */
        searchBt.setOnAction(event -> {//搜索按钮点击触发的事件
            String searchContent = searchTf.getText();//获取搜索框内容
            if (Pattern.matches("^\\s*$", searchContent)) {
                return;//搜索框内容为空或任何空白字符，结束搜索直接返回
            }
            ObservableList<String> searchContactOL = FXCollections.observableArrayList(); //用于存放搜索到的联系人的数据
            for (VCard contactPerson : contactPeopleArr) {
                boolean isSearched = false;//作为是否在单个联系人信息中查到搜索内容的标志
                StringBuilder content = new StringBuilder();//用于存储查询到的单个联系人的姓名/电话号码/邮箱等信息
                String name = contactPerson.getFormattedName().getValue();
                String namePinYin =Pinyin.getPinYin(name);
                ArrayList<String> phoneNumbers = null;
                for(Telephone telephone:contactPerson.getTelephoneNumbers())
                {
                    phoneNumbers.add(telephone.getText());
                }
                ArrayList<String> emails = null;
                for(Email email:contactPerson.getEmails())
                {
                    emails.add( email.getValue());
                }
                if (name.contains(searchContent) || namePinYin.contains(searchContent) || namePinYin.toUpperCase().contains(searchContent)) {
                    isSearched = true;
                }
                for (String phoneNumber : phoneNumbers) {
                    if (phoneNumber.contains(searchContent)) {
                        isSearched = true;
                        break;
                    }
                }
                for (String email : emails) {
                    if (email.contains(searchContent)) {
                        isSearched = true;
                        break;
                    }
                }
                if (isSearched) {
                    content.append(name).append("\t");
                    for (String phone : phoneNumbers) {
                        content.append(phone).append("\t");
                    }
                    for (String email : emails) {
                        content.append(email).append("\t");
                    }
                    searchContactOL.add(content.toString());
                }
            }

            if (searchContactOL.isEmpty()) {//若遍历所有联系人的所有信息均未查找到搜索内容，则弹框提示未搜索到内容
                searchNullAlert();
                return;
            }

            ListView<String> searchContactLV = new ListView<>(searchContactOL);//搜索结果ListView
            BorderPane searchContactPane = new BorderPane(searchContactLV);
            searchContactPane.setPadding(new Insets(20));
            Scene searchResultScene = new Scene(searchContactPane, 400, 400);
            Stage searchResultStage = new Stage();
            searchResultStage.setScene(searchResultScene);
            searchResultStage.setTitle("Search Result");
            searchResultStage.show();
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("AddressBook");
        primaryStage.setResizable(false);
        primaryStage.show();

    }
}