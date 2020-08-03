package wang.rule;

public class PassStatus {

    private boolean pass;

    private String msg;

    /**
     * 通过
     * @return
     */
    public static PassStatus pass(){
        PassStatus status = new PassStatus();
        status.setPass(true);
        status.setMsg("通过");
        return status;
    }

    /**
     * 不通过
     * @param msg
     * @return
     */
    public static PassStatus reject(String msg){
        PassStatus status = new PassStatus();
        status.setPass(false);
        status.setMsg(msg);
        return status;
    }

    public boolean isPass() {
        return pass;
    }

    public void setPass(boolean pass) {
        this.pass = pass;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "PassStatus{" +
                "pass=" + pass +
                ", msg='" + msg + '\'' +
                '}';
    }
}
