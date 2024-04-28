package utils;

import ezvcard.VCard;
import ezvcard.io.text.VCardReader;
import ezvcard.property.Kind;
import ezvcard.property.Member;
import ezvcard.property.Photo;
import ezvcard.property.Uid;
import management.*;
import org.apache.commons.math3.stat.inference.GTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Import {
    public static void importVcard(String filepath) throws IOException {
        Path file =Paths.get(filepath);
        VCardReader reader=new VCardReader(file);
        boolean hasGroup=false;
        ArrayList<Data> peopleToAdd=new ArrayList<>();
        try {
            Data person;
            VCard temp;
            while (  (temp=reader.readNext()) != null) {
                if(temp.getKind()==null||temp.getKind().isIndividual())
                {
                    person =Data.vCardtoData(temp);
                    MainPane.addressBook.add(person);
                    peopleToAdd.add(person);
                    if(person.getUid()==null)
                    {
                        //若Uid为空，则为它生成uid
                        Uid uid = new Uid(UUID.randomUUID().toString());
                        person.setUid(uid);
                    }
                    List<Photo> photoList = person.getPhotos();
                    if (!photoList.isEmpty()) {
                        Photo photo;
                        for (int i = 0; i < photoList.size(); i++) {
                            photo = photoList.get(i);
                            byte[] data = photo.getData();
                            String photoFilepath = "src/main/resources/vCard/" + person.getFormattedName().getValue() + i + ".jpg";
                            File file1 = new File(photoFilepath);
                            file1.createNewFile();
                            FileOutputStream fos = new FileOutputStream(file1);
                            fos.write(data);
                            fos.close();
                        }

                    }
                }
                else if(temp.getKind().isGroup())
                {
//                    VCard group=new VCard();
//                    Kind kind=Kind.group();
//                    group.setKind(kind);
                    String group;
                    group= temp.getMembers().get(0).getValue();
//                    VCard newd=group.getAgent().getVCard();
                    MainPane.groups.add(temp);
                    hasGroup=true;
                }
            }
        } finally {
            //如果没有分组信息，则新建一个ungroup分组添加进所有人
            if(!hasGroup&&MainPane.groups.isEmpty())
            {
                VCard un=new VCard();
                Kind kind= Kind.group();
                un.setKind(kind);
                un.setFormattedName("ungroup");
//                Group group=new Group(un.getFormattedName().getValue());
                //System.out.println(person.getUid().getValue());
                for (Data person: MainPane.addressBook.getAll())
                {
                    Member member=new Member(person.getUid().getValue());
                    un.addMember(member);
//                    group.addmember(person);
                }
                MainPane.groups.add(un);
//                ManageGroup.addgroup(group);
            }//如果新导入的文件没有分组信息，则将新添加的联系人放入"ungroup"分组
            else if(!hasGroup)
            {
                boolean hasUngroup=false;
                VCard agroup = null;
                for(int i=0;i<MainPane.groups.size();i++)
                {
                    agroup =MainPane.groups.get(i);
                    if(agroup.getFormattedName().getValue().equals("ungroup") || agroup.getFormattedName().getValue().equals("未分组"))
                    {
                        hasUngroup=true;
                        break;
                    }
                }

                VCard un;
                if(!hasUngroup)
                {
                   un=new VCard();
                    Kind kind= Kind.group();
                    un.setKind(kind);
                    un.setFormattedName("ungroup");
                }
                else
                {
                    un=agroup;
                }

//                Group group=new Group(un.getFormattedName().getValue());
                //System.out.println(person.getUid().getValue());
                for (Data person: peopleToAdd)
                {
                    Member member=new Member(person.getUid().getValue());
                    un.addMember(member);
//                    group.addmember(person);
                }
                MainPane.groups.add(un);
//                ManageGroup.addgroup(group);
            }
            reader.close();
        }
    ArrayList<Data> allperson=MainPane.addressBook.getAll();
        for(VCard onegroup: MainPane.groups)
    {
        List<Member> members=onegroup.getMembers();
        Group group1=new Group(onegroup.getFormattedName().getValue());
        for (Member member:members)
        {
            for(Data person:allperson)
            {
                System.out.println(person.getUid().getValue());
                System.out.println(member.getValue());
                if(person.getUid().getValue().equals(member.getValue()) )
                {
                    group1.addmember(person);
                }
            }

        }
        ManageGroup.addgroup(group1);
    }
    }
}
