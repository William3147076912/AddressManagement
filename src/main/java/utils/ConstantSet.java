package utils;

/**
 * @author fcj
 * @version 1.0
 * @description: 常量集
 * @date 2024/4/25 下午11:55
 */

public final class ConstantSet {
    //groupList里存有两个label，和实际上的组下标存在偏差，应当加上本偏移量才能得到正确的按钮组件
    public static final int GROUP_LIST_OFFSET = 2;
    public static final int ALL_PEOPLE_SCENE = 1;//所有联系人组的scene
    public static final int DEFAULT_GROUP_INDEX = 0;//默认组（所有联系人组）的下标
    public static final int CONTROL_NODE_SIZE = 9;//all people界面的控制组件个数（包含padding）
    public static final int CREATE_CONTACT = 0;    //创建联系人窗口
    public static final int UPDATE_CONTACT = 1;    //修改联系人窗口
    public static final int CREATE_GROUP = 0;
    public static final int MANAGE_GROUP = 1;
}
