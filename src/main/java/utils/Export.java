package utils;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.io.text.VCardWriter;
import management.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Export {
    public static void export(String filename)
    {
        File file=new File(filename);
        try {
            file.createNewFile();
            Path path= Paths.get(filename);

            VCardWriter writer=new VCardWriter(path, VCardVersion.V4_0);
            for(Group group: ManageGroup.groups)
            {
                writer.write(group.getGroup());
            }
            writer.setTargetVersion(VCardVersion.V3_0);
            ArrayList<Data> datas= MainPane.addressBook.getAll();
            for (Data data:datas)
            {
                writer.write(data);
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
