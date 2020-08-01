package wang.excel.normal.parse.model;

import org.springframework.util.Assert;
import wang.util.ReflectUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 保存 嵌套表的参数信息
 *
 * @author Administrator
 *
 */
public class NestField {
    /**
     * 嵌套实体在主实体中的属性
     */
    private Field fieldInParent;
    private Class realType;//真实类型
    private Class<? extends List> initType;// 如果是集合对象,初始化时用的类型 ArrayList


    public NestField(Field fieldInParent) throws IllegalArgumentException{
        this.fieldInParent = fieldInParent;
        this.realType = deduceRealType(fieldInParent);
        if (isList()) {
            this.initType = deduceListInitType(fieldInParent);
        }
    }


    /**
     * 此属性是否是集合
     *
     * @return
     */
    public boolean isList() {
        return List.class.isAssignableFrom(fieldInParent.getType());
    }

    /**
     * 获取嵌套列在主实体中的属性名
     * @return
     * @throws IllegalStateException
     */
    public String getFieldNameInParent() throws IllegalStateException{
        return fieldInParent.getName();
    }

    /**
     * 判断集合类型对象的实例化类
     * @param field
     * @return
     */
    public static Class<? extends List> deduceListInitType(Field field) throws IllegalArgumentException{
        Assert.notNull(field,"推断字段不可为空");
        Class<?> type = field.getType();
        if(!List.class.isAssignableFrom(type)){
            throw new IllegalArgumentException("仅支持集合类型属性");
        }
        if(ReflectUtil.canInstance(type)){
            return (Class<? extends List>) type;
        }else if(type.isAssignableFrom(ArrayList.class)){
            return ArrayList.class;
        }else if(type.isAssignableFrom(LinkedList.class)){
            return LinkedList.class;
        }
        throw new IllegalArgumentException("无法判断合适集合实例化类型"+field);

    }

    /**
     * 判断集合类型对象的实例化类
     * @param field
     * @return
     */
    public static Class deduceRealType(Field field)throws IllegalArgumentException{
        Assert.notNull(field,"推断字段不可为空");
        Class type = field.getType();
        if(List.class.isAssignableFrom(type)){
            Class[] actualType = ReflectUtil.getFieldActualType(field);
            if(actualType.length!=1){
                throw new IllegalArgumentException("请指定集合泛型"+ field);
            }
            type = actualType[0];
        }
        if(!ReflectUtil.canInstance(type)){
            throw new IllegalArgumentException("推断的类型不可实例化"+field);
        }
        return type;
    }


    public Class<? extends List> getInitType() {
        return initType;
    }

    public void setInitType(Class<? extends List> initType) {
        this.initType = initType;
    }

    public Field getFieldInParent() {
        return fieldInParent;
    }

    public void setFieldInParent(Field fieldInParent) {
        this.fieldInParent = fieldInParent;
    }

    public Class getRealType() {
        return realType;
    }

    public void setRealType(Class realType) {
        this.realType = realType;
    }


    @Override
    public String toString() {
        return "NestField{" +
                "fieldInParent=" + fieldInParent +
                ", realType=" + realType +
                ", initType=" + initType +
                '}';
    }
}
