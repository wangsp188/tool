package wang.excel.normal.produce.iwf.impl;


import wang.excel.common.model.BaseProduceParam;
import wang.excel.common.model.CellData;
import wang.excel.normal.produce.iwf.O2CellMiddleware;

/**
 * 原列的基础上包装出序号列
 */
public class WrapIndex4Middleware<T>  implements O2CellMiddleware<T> {

    private O2CellMiddleware ware;

    private static final String index_key = "00";

    public WrapIndex4Middleware(O2CellMiddleware ware) {
        this.ware = ware;
    }

    @Override
    public String[] keys() {
        String[] keys = ware.keys();
        if(keys==null){
            return new String[]{index_key};
        }
        String[] ns = new String[keys.length+1];
        System.arraycopy(keys,0,ns,1,keys.length);
        ns[0] = index_key;
        return ns;
    }

    @Override
    public BaseProduceParam param(String key) {
        if(index_key.equals(key)){
            return  new BaseProduceParam();
        }
        return ware.param(key);
    }

    @Override
    public String title(String key) {
        if(index_key.equals(key)){
            return  "序号";
        }
        return ware.title(key);
    }

    @Override
    public CellData data(T t, String key, Integer index) {
        if(index_key.equals(key)){
            return new CellData(index + 1);
        }
        return ware.data(t,key,index);
    }
}
