package imzjm.practice.kfc;

public class ClockInfo {
    private final String AQ = "安全";

    private final String JK = "健康";

    private final int aTYPE = 1;

    private String jd;

    private String wd;

    private String sheng;

    private String shi;

    private String qu;

    private String location;

    private String filenames;

    public String getAQ() {
        return AQ;
    }

    public String getJK() {
        return JK;
    }

    public int getaTYPE() {
        return aTYPE;
    }

    public String getJd() {
        return jd;
    }

    public void setJd(String jd) {
        this.jd = jd;
    }

    public String getWd() {
        return wd;
    }

    public void setWd(String wd) {
        this.wd = wd;
    }

    public String getSheng() {
        return sheng;
    }

    public void setSheng(String sheng) {
        this.sheng = sheng;
    }

    public String getShi() {
        return shi;
    }

    public void setShi(String shi) {
        this.shi = shi;
    }

    public String getQu() {
        return qu;
    }

    public void setQu(String qu) {
        this.qu = qu;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getFilenames() {
        return filenames;
    }

    public void setFilenames(String filenames) {
        this.filenames = filenames;
    }

    @Override
    public String toString() {
        return "签到信息: {" +
                "AQ='" + AQ + '\'' +
                ", JK='" + JK + '\'' +
                ", aTYPE=" + aTYPE +
                ", jd='" + jd + '\'' +
                ", wd='" + wd + '\'' +
                ", sheng='" + sheng + '\'' +
                ", shi='" + shi + '\'' +
                ", qu='" + qu + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
