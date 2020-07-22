package moe.msm.dmtqserver.model;

/**
 * member object store in database
 */
public class Member {
    private Integer id;
    private String nickname;
    private String guid;
    private String puid;
    private String udid;
    private String code;
    private Integer slotItem1;
    private Integer slotItem2;
    private Integer slotItem3;
    private Integer slotItem4;

    public Member() {
    }

    public Member(Integer id, String nickname, String guid, String puid, String udid, String code, Integer slotItem1, Integer slotItem2, Integer slotItem3, Integer slotItem4) {
        this.id = id;
        this.nickname = nickname;
        this.guid = guid;
        this.puid = puid;
        this.udid = udid;
        this.code = code;
        this.slotItem1 = slotItem1;
        this.slotItem2 = slotItem2;
        this.slotItem3 = slotItem3;
        this.slotItem4 = slotItem4;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getPuid() {
        return puid;
    }

    public void setPuid(String puid) {
        this.puid = puid;
    }

    public String getUdid() {
        return udid;
    }

    public void setUdid(String udid) {
        this.udid = udid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getSlotItem1() {
        return slotItem1;
    }

    public void setSlotItem1(Integer slotItem1) {
        this.slotItem1 = slotItem1;
    }

    public Integer getSlotItem2() {
        return slotItem2;
    }

    public void setSlotItem2(Integer slotItem2) {
        this.slotItem2 = slotItem2;
    }

    public Integer getSlotItem3() {
        return slotItem3;
    }

    public void setSlotItem3(Integer slotItem3) {
        this.slotItem3 = slotItem3;
    }

    public Integer getSlotItem4() {
        return slotItem4;
    }

    public void setSlotItem4(Integer slotItem4) {
        this.slotItem4 = slotItem4;
    }
}
