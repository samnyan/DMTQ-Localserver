package moe.msm.dmtqserver.model;

public class Play {
    private Integer pattern_id;
    private Integer user_id;
    private Integer score = 0;
    private String grade = "F";
    private String isAllCombo = "N";
    private String isPerfectPlay = "N";
    private Integer judgement = 0;

    public Play() {
    }

    public Play(Integer pattern_id, Integer user_id) {
        this.pattern_id = pattern_id;
        this.user_id = user_id;
    }

    public Play(Integer pattern_id, Integer user_id, Integer score, String grade, String isAllCombo, String isPerfectPlay, Integer judgement) {
        this.pattern_id = pattern_id;
        this.user_id = user_id;
        this.score = score;
        this.grade = grade;
        this.isAllCombo = isAllCombo;
        this.isPerfectPlay = isPerfectPlay;
        this.judgement = judgement;
    }

    public Integer getPattern_id() {
        return pattern_id;
    }

    public void setPattern_id(Integer pattern_id) {
        this.pattern_id = pattern_id;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getIsAllCombo() {
        return isAllCombo;
    }

    public void setIsAllCombo(String isAllCombo) {
        this.isAllCombo = isAllCombo;
    }

    public String getIsPerfectPlay() {
        return isPerfectPlay;
    }

    public void setIsPerfectPlay(String isPerfectPlay) {
        this.isPerfectPlay = isPerfectPlay;
    }

    public Integer getJudgement() {
        return judgement;
    }

    public void setJudgement(Integer judgement) {
        this.judgement = judgement;
    }
}
