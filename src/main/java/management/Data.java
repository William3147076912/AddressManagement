package management;

import ezvcard.VCard;
import ezvcard.property.*;
import io.vproxy.vfx.ui.button.FusionButton;

public class Data extends VCard {
    public FusionButton choiceButton;
    public String in= "";//所属分组
/*    public String type;
    public int bandwidth;
    public long createTime;*/

    public Data() {
        this.choiceButton = new FusionButton() {{
            setPrefWidth(10);
            setPrefHeight(50);
            setDisableAnimation(true);//取消闪烁动画
        }};
//        choiceButton = new FusionButton() {{
//            setPrefWidth(10);
//            setPrefHeight(50);
//            //setOnlyAnimateWhenNotClicked(true);
//        }};

//        FormattedName name = new FormattedName("某某某");
//        this.addFormattedName(name);
//        Address address = new Address();
//        address.setStreetAddress("二仙桥");
//        this.addAddress(address);

//        type = ThreadLocalRandom.current().nextBoolean() ? "classic" : "new";
//        bandwidth = ThreadLocalRandom.current().nextInt(10) * 100 + 100;
//        createTime = System.currentTimeMillis();

    }

    public static Data vCardtoData(VCard vCard) {

        Data person = new Data();
/*       person.type = ThreadLocalRandom.current().nextBoolean() ? "classic" : "new";
        person. bandwidth = ThreadLocalRandom.current().nextInt(10) * 100 + 100;
        person. createTime = System.currentTimeMillis();*/
//        Uid uid = new Uid(UUID.randomUUID().toString());
//        person.setUid(uid);
        Uid uid = vCard.getUid();
        person.setUid(uid);
        for (Address address : vCard.getAddresses()) {
            person.addAddress(address);
        }
        for (Telephone telephone : vCard.getTelephoneNumbers()) {
            person.addTelephoneNumber(telephone);
        }
        for (Url url : vCard.getUrls()) {
            person.addUrl(url);
        }
        person.setBirthday(vCard.getBirthday());
        person.setFormattedName(vCard.getFormattedName());
        for (Email email : vCard.getEmails()) {
            person.addEmail(email);
        }
        for (Organization organization : vCard.getOrganizations()) {
            person.addOrganization(organization);
        }

        for (Photo photo : vCard.getPhotos()) {
            person.addPhoto(photo);
        }
        for (Note note : vCard.getNotes()) {
            person.addNote(note);
        }
        return person;
    }
}