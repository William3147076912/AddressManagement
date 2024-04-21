package management;

import ezvcard.VCard;
import ezvcard.property.Address;
import ezvcard.property.FormattedName;
import ezvcard.property.Uid;
import io.vproxy.vfx.ui.button.FusionButton;
import utils.TUtils;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
public  class Data extends VCard {
    public FusionButton choiceButton;
    public String type;
    public int bandwidth;
    public long createTime;

    public Data() {
        choiceButton = new FusionButton() {{
            setPrefWidth(10);
            setPrefHeight(50);
            //setOnlyAnimateWhenNotClicked(true);
        }};
        Uid uid = new Uid("2022253104xx");
        this.setUid(uid);
        FormattedName name = new FormattedName("某某某");
        this.addFormattedName(name);
        Address address = new Address();
        address.setStreetAddress("二仙桥");
        this.addAddress(address);

        type = ThreadLocalRandom.current().nextBoolean() ? "classic" : "new";
        bandwidth = ThreadLocalRandom.current().nextInt(10) * 100 + 100;
        createTime = System.currentTimeMillis();
    }
}