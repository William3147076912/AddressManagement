package utils;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.io.text.VCardWriter;
import management.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Export {
    private static List<VCard> groups = AddressBook.getGroups();//组表
    private static List<List<Data>> peopleList = AddressBook.getPeopleList();//存储所有分组的联系人数据

    public static void export(String filepath) {
        File file = new File(filepath);
        if (filepath.startsWith("/")) filepath = filepath.substring(1);
        try {
            file.createNewFile();
            Path path = Paths.get(filepath);
            VCardWriter writer = new VCardWriter(path, VCardVersion.V4_0);
            //for(Group group: Group.groups)
            //{
            //    writer.write(group.getGroup());
            //}
            for (VCard group : groups) {
                if ("All People".equals(group.getFormattedName().getValue())) continue;//跳过默认组
                writer.write(group);
                //System.out.println(group.getMembers().get(0));
            }
            writer.setTargetVersion(VCardVersion.V3_0);
            List<Data> datas = peopleList.get(0);
            for (Data data : datas) {
                writer.write(data);
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
