package utils;

import ezvcard.VCard;
import ezvcard.io.text.VCardReader;
import ezvcard.property.Kind;
import ezvcard.property.Member;
import ezvcard.property.Uid;
import management.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

public class Import {
    private static List<VCard> groups = AddressBook.getGroups();//组表
    private static List<List<Data>> peopleList = AddressBook.getPeopleList();//存储所有分组的联系人数据

    public static void importVcard(String filepath) throws IOException {
        Path file = Paths.get(filepath);
        VCardReader reader = new VCardReader(file);
        boolean hasGroup = false;
        List<Data> newContacts = new ArrayList<>();//暂存此次导入的联系人
        Data person;
        VCard temp;
        while ((temp = reader.readNext()) != null) {//读取数据
            if (temp.getKind() == null || temp.getKind().isIndividual()) {//如果该VCard是无规范信息or是个人信息
                person = Data.vCardtoData(temp);
                newContacts.add(person);
                if (person.getUid() == null) {
                    //若Uid为空，则为它生成uid
                    Uid uid = new Uid(UUID.randomUUID().toString());
                    person.setUid(uid);
                }
//                List<Photo> photoList = person.getPhotos();
//                if (!photoList.isEmpty()) {//如果该联系人的图片不为空，读取并保存在本地
//                    for (Photo photo : photoList) {
//                        byte[] data = photo.getData();//转二进制
//                        String photoFilepath = "src/main/resources/vCard/" + person.getUid().getValue() + "."+photo.getContentType().getValue();
//                        File file1 = new File(photoFilepath);
//                        file1.createNewFile();
//                        FileOutputStream fos = new FileOutputStream(file1);
//                        fos.write(data);
//                        fos.close();
//                    }
//                }
            } else if (temp.getKind().isGroup()) {//如果该VCard是组织信息
                if (groups.isEmpty()) {//如果组表一开始为空，创建"all people"组
                    groups.add(new VCard() {{
                        setKind(Kind.group());
                        setFormattedName("All People");
                    }});
                }
                groups.add(temp);//将识别到的组塞入组表
                for (Member member : temp.getMembers()) {//将该组所有uid录入"all people"组
                    groups.get(0).addMember(member);
                }
                hasGroup = true;
            }
        }
        reader.close();
        AddressBook.addAll(newContacts);//将新添加的联系人塞入所有人列表
        if (!hasGroup) {
            //如果没有分组信息且本地现在没有分组，则新建一个all people分组添加进所有人
            if (groups.isEmpty()) {//应用于vCard文件为低版本没有分组概念的情况    ->应用启动时加载的vCard无分组
                VCard allPeople = new VCard();
                allPeople.setKind(Kind.group());//设置 vCard 对象的 KIND 属性为 "all people"
                allPeople.setFormattedName("All People");
                for (Data data : peopleList.get(0)) {//存此次添加的联系人的uid到该组
                    Member member = new Member(data.getUid().getValue());//记录所有人的uid作为其在该组内的标识
                    allPeople.addMember(member);
                }
                groups.add(allPeople);
            } else { //如果新导入的文件没有分组信息，则将新添加的联系人放入"all people"分组，并将新加入的联系人放入"undefined"+数字 组中
                //->按导入按钮后的vCard无分组
                boolean hasUndefinedGroup = false;//原先是否有未定义的分组
                int newIndex = -1;
                VCard newGroup = new VCard();
                newGroup.setKind(Kind.group());
                for (VCard group : groups) {//目的：找到当前要命名的组名的数字编号
                    String groupName = group.getFormattedName().getValue();//取得组名
                    if (groupName.contains("undefined")) {//如果名字包含undefined
                        hasUndefinedGroup = true;
                        String groupNameIndex = Pattern.compile("\\d+").matcher(groupName).group();//取得undefined的数字编号
                        newIndex = Integer.parseInt(groupNameIndex) + 1;//要建的分组的数字编号
                    }
                }
                if (hasUndefinedGroup) newGroup.setFormattedName("undefined" + newIndex);
                else newGroup.setFormattedName("undefined" + 0);
                for (Data data : newContacts) {//存此次添加的联系人的uid到该组
                    Member member = new Member(data.getUid().getValue());
                    newGroup.addMember(member);
                }
                groups.add(newGroup);//将该组加入组表
                peopleList.add(newContacts);
            }
        } else {//vCard有分组的情况
            //读取时已经创建"all people"组，所以这里只需要给加载进来的每个组分配其联系人的数据
            //1.根据组表大小创建分组联系人数据表
            int index = groups.size() ;
            for (int i = 1; i < index; i++) {//舍去"all people"组
                int finalI = i;
                peopleList.add(new ArrayList<>() {{
                    //2.根据组表中每组的联系人uid在"all people"组的数据表里复制一份联系人数据给本组的数据表
                    for (Member member : groups.get(finalI).getMembers()) {
                        for (Data person : peopleList.get(0)) {
                            if (member.getValue().equals(person.getUid().getValue())) {
                                //System.out.println(member.getValue());
                                add(person);
                            }
                        }
                    }
                }});
            }
        }
    }
}
