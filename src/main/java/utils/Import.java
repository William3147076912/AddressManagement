package utils;

import ezvcard.VCard;
import ezvcard.io.text.VCardReader;
import ezvcard.property.Member;
import ezvcard.property.Photo;
import ezvcard.property.Uid;
import management.Data;
import management.Group;
import management.MainPane;
import management.ManageGroup;

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
        try {
            Data person;
            VCard temp;
            while (  (temp=reader.readNext()) != null) {
                if(temp.getKind().isIndividual())
                {
                    person =Data.vCardtoData(temp);
                    MainPane.addressBook.add(person);
                    if(person.getUid()==null)
                    {
                        Uid uid = new Uid(UUID.randomUUID().toString());
                        person.setUid(uid);
                    }

//                System.out.println(person.getUid().getValue());
//                Member member=new Member(person.getUid().getValue());
//                un.addMember(member);

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
                    MainPane.groups.add(temp);
                }

            }
        } finally {
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
                    if(person.getUid().getValue() == member.getValue())
                    {
                        group1.addmember(person);
                    }
                }

            }
            ManageGroup.addgroup(group1);
        }
    }
}
