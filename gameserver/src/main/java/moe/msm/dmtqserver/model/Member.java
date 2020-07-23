package moe.msm.dmtqserver.model;

/**
 * member object store in database
 */
public interface Member {
    Integer getId();

    void setId(Integer id);

    String getNickname();

    void setNickname(String nickname);

    String getGuid();

    void setGuid(String guid);

    String getPuid();

    void setPuid(String puid);

    String getUdid();

    void setUdid(String udid);

    String getCode();

    void setCode(String code);

    Integer getSlotItem1();

    void setSlotItem1(Integer slotItem1);

    Integer getSlotItem2();

    void setSlotItem2(Integer slotItem2);

    Integer getSlotItem3();

    void setSlotItem3(Integer slotItem3);

    Integer getSlotItem4();

    void setSlotItem4(Integer slotItem4);
}
