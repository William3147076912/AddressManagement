package management;

import ezvcard.VCard;
import ezvcard.property.Kind;
import ezvcard.property.Member;

import java.util.ArrayList;

public class Group {
    private VCard group;
    private ArrayList<Data> groupMember;

    public VCard getGroup() {
        return group;
    }

    public ArrayList<Data> getGroupMember() {
        return groupMember;
    }

    public Group(String groupName)
    {
        this.groupMember = new ArrayList<>();
        this.group=new VCard();
        Kind kind=Kind.group();
        this.group.setKind(kind);
        this.group.setFormattedName(groupName);
    }
    public void addmember(Data data)
    {
        Member member;
//        Telephone telephone=data.getTelephoneNumbers().get(0);
        member = new Member(data.getUid().toString());
        this.group.addMember(member);
        this.groupMember.add(data);
    }
    public void delmember(String uid)
    {
        for(Member member:group.getMembers())
        {
            if (member.getValue() == uid)
            {
                group.removeProperty(member);
                for(Data data: groupMember)
                {
                    if(data.getUid().toString()==uid)
                    {
                        groupMember.remove(data);
                    }
                }
            }
        }
    }
}
 