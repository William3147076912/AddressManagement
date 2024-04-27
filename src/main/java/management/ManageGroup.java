package management;

import java.util.ArrayList;

public class ManageGroup {
    public static ArrayList<Group> groups=new ArrayList<>();
    public static boolean addgroup(Group group)
    {
        groups.add(group);
        return true;
    }
    public static boolean delgroup(String groupname)
    {
        for(Group group:groups)
        {
            if (groupname==group.getGroup().getFormattedName().getValue())
            {
                groups.remove(group);
                return true;
            }
        }
        return false;
    }
}
