package moe.msm.dmtqserver.model.impl;

import moe.msm.dmtqserver.model.Member;

/**
 * member object store in database
 */
public class MemberData implements Member {
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

    public MemberData() {
    }

    public MemberData(Integer id, String nickname, String guid, String puid, String udid, String code, Integer slotItem1, Integer slotItem2, Integer slotItem3, Integer slotItem4) {
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

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public void setGuid(String guid) {
        this.guid = guid;
    }

    @Override
    public String getPuid() {
        return puid;
    }

    @Override
    public void setPuid(String puid) {
        this.puid = puid;
    }

    @Override
    public String getUdid() {
        return udid;
    }

    @Override
    public void setUdid(String udid) {
        this.udid = udid;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public Integer getSlotItem1() {
        return slotItem1;
    }

    @Override
    public void setSlotItem1(Integer slotItem1) {
        this.slotItem1 = slotItem1;
    }

    @Override
    public Integer getSlotItem2() {
        return slotItem2;
    }

    @Override
    public void setSlotItem2(Integer slotItem2) {
        this.slotItem2 = slotItem2;
    }

    @Override
    public Integer getSlotItem3() {
        return slotItem3;
    }

    @Override
    public void setSlotItem3(Integer slotItem3) {
        this.slotItem3 = slotItem3;
    }

    @Override
    public Integer getSlotItem4() {
        return slotItem4;
    }

    @Override
    public void setSlotItem4(Integer slotItem4) {
        this.slotItem4 = slotItem4;
    }
}
