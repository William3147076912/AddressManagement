package utils;

/**
 * @description: 常量集
 * @author fcj
 * @date 2024/4/25 下午11:55
 * @version 1.0
 */

public final class ConstantSet {
    //groupList里存有两个label，和实际上的组下标存在偏差，应当加上本偏移量才能得到正确的按钮组件
    public static final int GROUP_LIST_OFFSET = 2;
    public static final int ALL_PEOPLE_SCENE = 1;//所有联系人组的scene
    public static final int CREATE_CONTACT=0;    //创建联系人窗口
    public static final int UPDATE_CONTACT=1;    //修改联系人窗口
}
