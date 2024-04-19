package org.example.addressmanagement;
import ezvcard.VCard;
import ezvcard.io.text.VCardReader;
import ezvcard.parameter.ImageType;
import ezvcard.property.*;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.IOException;

public class vCardToObject {
    public static void vCardfileToObject() throws IOException {
        Path file = Paths.get("D:\\desktop\\00001.vcf");
        VCardReader reader = new VCardReader(file);
        try {
            VCard vcard;
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            while ((vcard = reader.readNext()) != null) {
                FormattedName fn = vcard.getFormattedName();
                String name = (fn == null) ? null : fn.getValue();

                if (name != null && name.equals("女博士")) {
                    System.out.println("yes");
                    vcard.addExtendedProperty("身高", "矮");

                }
                Birthday bday = vcard.getBirthday();
                Date date = (bday == null) ? null : (Date) bday.getDate();
                String birthday = (date == null) ? null : df.format(date);

                List<Telephone> telephone=vcard.getTelephoneNumbers();

                System.out.println(name + " " );
                for (Telephone num : telephone) {
                    System.out.println(num.getText());
                }
                List<Photo> photos=vcard.getPhotos();
                for(Photo photo:photos)
                {
                    byte[] data=photo.getData();
                    System.out.println(Base64.getEncoder().encodeToString(data));
                }

                List<RawProperty> shuxin=vcard.getExtendedProperties();
                for(RawProperty shu:shuxin)
                {
                    System.out.println(shu.getPropertyName()+shu.getValue());
                }
            }
        } finally {
            reader.close();
        }
    }
}
