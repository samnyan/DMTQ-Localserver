package moe.msm.dmtqjavaserver.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import moe.msm.dmtqserver.model.Member;

/**
 * member object store in database
 */
@DatabaseTable(tableName = "Member")
public class MemberData implements Member {
    @DatabaseField(id = true)
    private Integer id;

    @DatabaseField(columnName = "nickname")
    private String nickname;

    @DatabaseField(columnName = "guid")
    private String guid;

    @DatabaseField(columnName = "puid")
    private String puid;

    @DatabaseField(columnName = "udid", dataType = DataType.LONG_STRING)
    private String udid;

    @DatabaseField(columnName = "code", dataType = DataType.LONG_STRING)
    private String code;

    @DatabaseField(columnName = "slot_item1")
    private Integer slotItem1;

    @DatabaseField(columnName = "slot_item2")
    private Integer slotItem2;

    @DatabaseField(columnName = "slot_item3")
    private Integer slotItem3;

    @DatabaseField(columnName = "slot_item4")
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
