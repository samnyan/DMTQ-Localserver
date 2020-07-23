package moe.msm.dmtqserver.external;

import moe.msm.dmtqserver.model.Member;
import moe.msm.dmtqserver.model.Play;

import java.util.List;

/**
 * @author sam_nya (privateamusement@protonmail.com)
 */
public interface GameDatabaseService {
    void close();

    /**
     * Initialize database like create tables. This won't get call by the server.
     */
    void init();

    Member getUserByUdid(String udid);

    Member getUserByGuid(int guid);

    void updateUser(Member member);

    List<Play> getRecordsByGuid(int guid);

    Play getRecordByGuidAndPatternId(int guid, int patternId);

    void insertScore(Play play);

    void updateScore(Play play);
}
