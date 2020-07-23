package moe.msm.dmtqserver.model;

public interface Play {
    Integer getPattern_id();

    void setPattern_id(Integer pattern_id);

    Integer getUser_id();

    void setUser_id(Integer user_id);

    Integer getScore();

    void setScore(Integer score);

    String getGrade();

    void setGrade(String grade);

    String getIsAllCombo();

    void setIsAllCombo(String isAllCombo);

    String getIsPerfectPlay();

    void setIsPerfectPlay(String isPerfectPlay);

    Integer getJudgement();

    void setJudgement(Integer judgement);
}

