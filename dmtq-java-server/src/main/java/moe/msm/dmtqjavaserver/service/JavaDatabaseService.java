package moe.msm.dmtqjavaserver.service;

import moe.msm.dmtqserver.external.GameDatabaseService;
import moe.msm.dmtqserver.model.Member;
import moe.msm.dmtqserver.model.Play;

import java.util.List;

/**
 * @author sam_nya (privateamusement@protonmail.com)
 */
public class JavaDatabaseService implements GameDatabaseService {
    @Override
    public void close() {

    }

    @Override
    public void init() {

    }

    @Override
    public Member getUserByUdid(String udid) {
        return null;
    }

    @Override
    public Member getUserByGuid(int guid) {
        return null;
    }

    @Override
    public void updateUser(Member member) {

    }

    @Override
    public List<Play> getRecordsByGuid(int guid) {
        return null;
    }

    @Override
    public Play getRecordByGuidAndPatternId(int guid, int patternId) {
        return null;
    }

    @Override
    public void insertScore(Play play) {

    }

    @Override
    public void updateScore(Play play) {

    }
}
