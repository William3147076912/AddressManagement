package management;

import ezvcard.VCard;
import ezvcard.property.Member;
import utils.ConstantSet;

import java.util.ArrayList;
import java.util.List;

/**
 * 往简单说，就是一个二维数组存各个组联系人的数据，一个一维数组存各个组联系人的uid
 */
public class AddressBook {
    private static List<VCard> groups = new ArrayList<>();//包含已存在的所有组信息
    private static List<List<Data>> peopleList = new ArrayList<>();//存储所有分组的所有用户信息

    static {
        List<Data> defaultList = new ArrayList<>();
        peopleList.add(defaultList);
    }

    public AddressBook() {
    }

    //外部调用groups的唯一方法
    public static List<VCard> getGroups() {
        return groups;
    }

    //外部调用peopleList的唯一方法
    public static List<List<Data>> getPeopleList() {
        return peopleList;
    }

    /**
     * 添加数据到"all people"组
     */
    public static void add(Data person) {
        peopleList.get(0).add(person);
    }

    public static void addAll(List<Data> people) {
        peopleList.get(0).addAll(people);
    }

    /**
     * 智能删除，如果是"all people"组，则会删除该联系人在各组的数据与在组表的uid
     * 如果是在其他组，则仅仅删除该联系人在该组的数据与在该组的uid
     *
     * @param person 待删除联系人的姓名
     * @param index  对应组下标
     */
    public static void remove(Data person, int index) {
        if (index == ConstantSet.DEFAULT_GROUP_INDEX) {//如果是在默认组删除联系人
            for (List<Data> dataList : peopleList) {//删除该联系人在各组的数据
                dataList.remove(person);
            }
            for (VCard group : groups) {//删除该联系人在在组表的uid
                group.getMembers().remove(new Member(person.getUid().getValue()));
            }
        } else {//不是默认组则在该组删除即可
            if (index < peopleList.size()) {
                peopleList.get(index).remove(person);
                groups.get(index).getMembers().remove(new Member(person.getUid().getValue()));
            }
        }
    }

    /**
     * 没什么好讲的，就更新该联系人在各组的数据
     *
     * @param oldData 原先要替换的数据
     * @param newData 新的数据
     */
    public static void modify(Data oldData, Data newData) {//对指定联系人信息进行修改（全局修改）
        for (List<Data> dataList : peopleList) {
            if (dataList.contains(oldData)) {
                dataList.set(dataList.indexOf(oldData), newData);
            }
        }
    }
}


