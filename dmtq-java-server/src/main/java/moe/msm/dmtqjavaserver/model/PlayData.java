package moe.msm.dmtqjavaserver.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import moe.msm.dmtqserver.model.Play;

@DatabaseTable(tableName = "Play")
public class PlayData implements Play {

    // Added for ormlite
    @DatabaseField(id = true)
    private Integer id;

    @DatabaseField(columnName = "pattern_id")
    private Integer pattern_id;

    @DatabaseField(columnName = "user_id")
    private Integer user_id;

    @DatabaseField(columnName = "score", defaultValue = "0")
    private Integer score = 0;

    @DatabaseField(columnName = "grade", dataType = DataType.STRING, width = 1, defaultValue = "F")
    private String grade = "F";

    @DatabaseField(columnName = "isAllCombo", dataType = DataType.STRING, width = 1, defaultValue = "N")
    private String isAllCombo = "N";

    @DatabaseField(columnName = "isPerfectPlay", dataType = DataType.STRING, width = 1, defaultValue = "N")
    private String isPerfectPlay = "N";

    @DatabaseField(columnName = "judgement", defaultValue = "0")
    private Integer judgement = 0;

    public PlayData() {
    }

    public PlayData(Integer pattern_id, Integer user_id) {
        this.pattern_id = pattern_id;
        this.user_id = user_id;
    }

    public PlayData(Integer pattern_id, Integer user_id, Integer score, String grade, String isAllCombo, String isPerfectPlay, Integer judgement) {
        this.pattern_id = pattern_id;
        this.user_id = user_id;
        this.score = score;
        this.grade = grade;
        this.isAllCombo = isAllCombo;
        this.isPerfectPlay = isPerfectPlay;
        this.judgement = judgement;
    }

    public PlayData(Integer id, Integer pattern_id, Integer user_id, Integer score, String grade, String isAllCombo, String isPerfectPlay, Integer judgement) {
        this.id = id;
        this.pattern_id = pattern_id;
        this.user_id = user_id;
        this.score = score;
        this.grade = grade;
        this.isAllCombo = isAllCombo;
        this.isPerfectPlay = isPerfectPlay;
        this.judgement = judgement;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getPattern_id() {
        return pattern_id;
    }

    @Override
    public void setPattern_id(Integer pattern_id) {
        this.pattern_id = pattern_id;
    }

    @Override
    public Integer getUser_id() {
        return user_id;
    }

    @Override
    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    @Override
    public Integer getScore() {
        return score;
    }

    @Override
    public void setScore(Integer score) {
        this.score = score;
    }

    @Override
    public String getGrade() {
        return grade;
    }

    @Override
    public void setGrade(String grade) {
        this.grade = grade;
    }

    @Override
    public String getIsAllCombo() {
        return isAllCombo;
    }

    @Override
    public void setIsAllCombo(String isAllCombo) {
        this.isAllCombo = isAllCombo;
    }

    @Override
    public String getIsPerfectPlay() {
        return isPerfectPlay;
    }

    @Override
    public void setIsPerfectPlay(String isPerfectPlay) {
        this.isPerfectPlay = isPerfectPlay;
    }

    @Override
    public Integer getJudgement() {
        return judgement;
    }

    @Override
    public void setJudgement(Integer judgement) {
        this.judgement = judgement;
    }
}
