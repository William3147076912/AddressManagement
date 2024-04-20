package org.example.addressmanagement;

import ezvcard.VCard;

import java.util.ArrayList;

/**
 * 数组+链式存储的存储结构
 * 链表：单向链表
 * 数组存放每个链表的首结点
 * 每条链表存放以一个字母为标志的，姓名首字母或姓名拼音首字母的所有联系人（以联系人对象为数据元素的结点）
 * 若联系人姓名或姓名的拼音以非字母开头,则存放在以#标志的首结点元素的链表中
 */
public class AddressBook {
    private static final int SYMBOL_NUM = 27;//将通讯录分为以 # A B C...为标志的27块存放所有联系人;
    private static final AddressBookHeadNode[] addressBookHeadNodes = new AddressBookHeadNode[SYMBOL_NUM];

    static {//初始化数组
        addressBookHeadNodes[0] = new AddressBookHeadNode('#');
        for (int i = 1; i < addressBookHeadNodes.length ; i++) {
            addressBookHeadNodes[i] = new AddressBookHeadNode((char) ('A' + i));
        }
    }

    public AddressBook() {
    }

    public AddressBookHeadNode[] getAddressBookHeadNodes() {//获取所有链表的头结点
        return addressBookHeadNodes;
    }

    /**
     * 因为 为了区分链表的首结点与普通结点分别用两个类来作为链表的首结点和普通结点
     * 导致不可以将 增删改查（add remove modify get）方法的代码相同操作（例如：找到被查找联系人结点的前一个结点...）
     * “统一”到一个方法内来简化代码 后 再在增删改查（add remove modify get）方法 内分别进行他们独特的操作语句
     * 从而在这些方法中出现了大量类似操作语句
     */
    public void add(VCard person) {//将一个联系人添加到链表中
        AddressBookHeadNode headNode;
        char firstChar = Pinyin.getFisrtChar(Pinyin.getPinYin(person.getFormattedName().getValue())) ;//在contactperson中
        AddressBookNode node = new AddressBookNode(person);//以待添加联系人作为数据创建链表结点

        if (firstChar >= 'A' && firstChar <= 'Z') {
            headNode = addressBookHeadNodes[firstChar - 'A' + 1];//为联系人定位到合适的链表
        } else {
            headNode = addressBookHeadNodes[0];
        }

        AddressBookNode pointer = headNode.getFirstNode();//将联系人放到链表中的合适位置
        if (pointer == null) {//若链表除首结点外无其他结点,则将联系人作为数据创建结点插入该链表首结点后
            headNode.setFirstNode(new AddressBookNode(person));
        } else if (Pinyin.compareTo(Pinyin.getPinYin(person.getFormattedName().getValue()),Pinyin.getPinYin(pointer.getData().getFormattedName().getValue()) ) < 0) {/*若待插入联系人小于链表首结点后的结点存储的联系人，
                                                                      则将联系人作为数据创建结点插入到首结点后*/
            node.setNext(pointer);
            headNode.setFirstNode(node);
        } else {//否则通过while循环找到待添加联系人的合适位置，进行添加
            while (pointer.getNext() != null &&Pinyin.compareTo(Pinyin.getPinYin(pointer.getNext().getData().getFormattedName().getValue()),Pinyin.getPinYin(person.getFormattedName().getValue()))< 0) {
                pointer = pointer.getNext();
            }// pointer.getData().compareTo(person)
            node.setNext(pointer.getNext());
            pointer.setNext(node);
        }
        headNode.setNumberOfLinked(headNode.getNumberOfLinked() + 1);//该链表首结点记录存储联系人数目的变量加1
    }



    /**
     * 为了避免相同name存在而错误删除,另外用第一个phoneNumber作为区别
     *
     * @param name        待删除联系人的姓名
     * @param phoneNumber 待删除联系人的第一个手机号码
     */
    public void remove(String name, String phoneNumber) {//根据联系人的姓名和联系人的第一个手机号码将联系人从链表中删除
        String namePinyin = Pinyin.getPinYin(name);//处理待删除联系人的姓名获取其姓名拼音首字母以便后续定位
        char firstChar = namePinyin.toUpperCase().charAt(0);
        AddressBookHeadNode headNode;
        if (firstChar >= 'A' && firstChar <= 'Z') {
            headNode = addressBookHeadNodes[firstChar - 'A' + 1];//确定以待删除联系人为数据元素的结点属于哪个链表
        } else {
            headNode = addressBookHeadNodes[0];
        }

        AddressBookNode pointer = headNode.getFirstNode();//在链表中找到以待删除联系人为数据元素结点的位置

        if (pointer.getData().getFormattedName().getValue().equals(name) && pointer.getData().getTelephoneNumbers().get(0).getText().equals(phoneNumber)) {
            headNode.setFirstNode(pointer.getNext()); //以待删除联系人为数据元素结点为首结点的后一个结点
        } else {//使用while循环在链表中定位以待删除联系人为数据元素结点
            while (pointer.getNext() != null && (!pointer.getNext().getData().getFormattedName().getValue().equals(name) || !pointer.getNext().getData().getTelephoneNumbers().get(0).getText().equals(phoneNumber))) {
                pointer = pointer.getNext();
            }
            pointer.setNext(pointer.getNext().getNext());
        }
        headNode.setNumberOfLinked(headNode.getNumberOfLinked() - 1);//该链表首结点记录存储联系人数目的变量减1
    }

    /**
     * 这里默认用户在不修改联系人姓名的情况下对联系人信息进行修改
     * 该方法直接在链表中删除联系人对应的结点，重新添加到被删除结点的位置
     * 至于为什么不在这个方法中调用 add 方法，是考虑到效率因素
     * <p>
     * <p>
     * 为了避免相同name修改phoneNumber后，找不到准确的要修改的对象，
     * 利用ListView方法获取列表中的位置，结合name定位block，进而准确修改。
     * 链表头结点下标默认为0，默认用户不会修改姓名(在JavaFX中姓名对应的TextFiled属性设为不可编辑)。
     *
     * @param person 包含修改("新增")的联系人信息
     * @param index         修改的联系人的结点在对应链表中的位置
     */
    public void modify(VCard person, int index) {//对链表中指定联系人信息进行修改
        AddressBookNode node = new AddressBookNode(person);
        String namePinyin =Pinyin.getPinYin(person.getFormattedName().getValue()) ;
        char firstChar = namePinyin.toUpperCase().charAt(0);
        AddressBookHeadNode headNode;
        if (firstChar >= 'A' && firstChar <= 'Z') {
            headNode = addressBookHeadNodes[firstChar - 'A' + 1];
        } else {
            headNode = addressBookHeadNodes[0];
        }
        AddressBookNode pointer = headNode.getFirstNode();
        if (index == 1) {
            headNode.setFirstNode(node);
            node.setNext(pointer.getNext());
        } else {
            int i = 1;
            while (pointer.getNext() != null && i < index - 1) {//pointer.getNext() != null && 来避免空指针异常，其实可以不写
                pointer = pointer.getNext();
                i++;
            }
            node.setNext(pointer.getNext().getNext());
            pointer.setNext(node);
        }
    }

    /**
     * 为了避免因存在相同姓名的联系人而导致的查询信息错误
     * 利用ListView方法获取列表中的位置，结合name定位block，进而准确获得联系人信息
     * 链表头结点下标默认为0
     *
     * @param name  联系人姓名
     * @param index 联系人的结点在对应链表中的位置
     * @return 返回查找到的联系人
     */
    public VCard get(String name, int index) {//根据联系人姓名和联系人的结点在对应链表中的位置获取联系人
        String namePinyin =Pinyin.getPinYin(name);
        char firstChar = namePinyin.toUpperCase().charAt(0);
        AddressBookHeadNode headNode;
        if (firstChar >= 'A' && firstChar <= 'Z') {
            headNode = addressBookHeadNodes[firstChar - 'A' + 1];
        } else {
            headNode = addressBookHeadNodes[0];
        }
        AddressBookNode pointer = headNode.getFirstNode();
        int i = 1;
        while (pointer.getNext() != null && i < index) {
            pointer = pointer.getNext();
            i++;
        }
        return pointer.getData();
    }


    /**
     * 这个方法可以省略
     *
     * @return ArrayList<ContactPerson> 类型对象存储的 按顺序存放的所有链表中的所有联系人
     */
    public ArrayList<VCard> getAll() {//获取按顺序存放的所有链表中的所有联系人
        ArrayList<VCard> contactPersonList = new ArrayList<>();
        AddressBookNode pointer;
        for (AddressBookHeadNode addressBookHeadNode : addressBookHeadNodes) {
            pointer = addressBookHeadNode.getFirstNode();
            while (pointer != null) {
                contactPersonList.add(pointer.getData());
                pointer = pointer.getNext();
            }
        }
        return contactPersonList;
    }

}

class AddressBookHeadNode {//链表首结点
    private final char symbol;//标志以该结点为首结点的链表存储的是以什么字母为姓名拼音的首字母的联系人
    private int numberOfLinked;//链表中结点的个数（联系人的个数）
    private AddressBookNode firstNode;

    public AddressBookHeadNode(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }

    public int getNumberOfLinked() {
        return numberOfLinked;
    }

    public void setNumberOfLinked(int numberOfLinked) {
        this.numberOfLinked = numberOfLinked;
    }

    public AddressBookNode getFirstNode() {
        return firstNode;
    }

    public void setFirstNode(AddressBookNode firstNode) {
        this.firstNode = firstNode;
    }


}

class AddressBookNode {
    private VCard person;//存储联系人对象
    private AddressBookNode next;

    public AddressBookNode(VCard person) {
        this.person = person;
    }

    public VCard getData() {
        return person;
    }

    public void setData(VCard person) {
        this.person = person;
    }

    public AddressBookNode getNext() {
        return next;
    }

    public void setNext(AddressBookNode next) {
        this.next = next;
    }
}