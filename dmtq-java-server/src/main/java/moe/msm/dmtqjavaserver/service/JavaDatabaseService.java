package moe.msm.dmtqjavaserver.service;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import moe.msm.dmtqjavaserver.model.MemberData;
import moe.msm.dmtqjavaserver.model.PlayData;
import moe.msm.dmtqserver.external.GameDatabaseService;
import moe.msm.dmtqserver.model.Member;
import moe.msm.dmtqserver.model.Play;
import moe.msm.dmtqserver.util.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author sam_nya (privateamusement@protonmail.com)
 */
public class JavaDatabaseService implements GameDatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(JavaDatabaseService.class);

    private final ConnectionSource connectionSource;
    private final Dao<MemberData, Integer> memberDao;
    private final Dao<PlayData, Integer> playDao;

    public JavaDatabaseService(String url) throws SQLException {
        logger.info("Connect to database with url: {}", url);
        connectionSource = new JdbcConnectionSource(url);
        logger.info("Creating database table");
        TableUtils.createTableIfNotExists(connectionSource, MemberData.class);
        TableUtils.createTableIfNotExists(connectionSource, PlayData.class);
        memberDao = DaoManager.createDao(connectionSource, MemberData.class);
        playDao = DaoManager.createDao(connectionSource, PlayData.class);
    }

    @Override
    public void close() {
        try {
            connectionSource.close();
        } catch (Exception ignored) {}
    }

    @Override
    public void init() {

    }

    @Override
    public Member getUserByUdid(String udid) {
        try {
            List<MemberData> members = memberDao.queryForEq("udid", udid);
            if (members.isEmpty()) {
                long max = memberDao.queryRawValue("SELECT IFNULL(MAX(id), 0) as id FROM Member");
                long id = max + 1;
                String code = "CODE_" + id;
                code = HashUtil.getMd5(code.getBytes());
                code = code.substring(code.length() - 10);
                MemberData member = new MemberData(Math.toIntExact(id), "NewUser", String.valueOf(id), String.valueOf(id), udid, code, null, null, null, null);
                memberDao.create(member);
                return member;
            } else {
                return members.get(0);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    @Override
    public Member getUserByGuid(int guid) {
        try {
            List<MemberData> result = memberDao.queryForEq("guid", guid);
            if (result.isEmpty()) {
                return null;
            }
            return result.get(0);
        } catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    @Override
    public void updateUser(Member member) {
        try {
            if (member instanceof MemberData) {
                memberDao.update((MemberData) member);
            } else {
                List<MemberData> result = memberDao.queryForEq("guid", member.getGuid());
                if (result.isEmpty()) {
                    memberDao.create(new MemberData(
                            null,
                            member.getNickname(),
                            member.getGuid(),
                            member.getPuid(),
                            member.getUdid(),
                            member.getCode(),
                            member.getSlotItem1(),
                            member.getSlotItem2(),
                            member.getSlotItem3(),
                            member.getSlotItem4()
                    ));
                } else {
                    MemberData old = result.get(0);
                    old.setNickname(member.getNickname());
                    old.setSlotItem1(member.getSlotItem1());
                    old.setSlotItem2(member.getSlotItem2());
                    old.setSlotItem3(member.getSlotItem3());
                    old.setSlotItem4(member.getSlotItem4());
                    memberDao.update(old);
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public List<PlayData> getRecordsByGuid(int guid) {
        try {
            return playDao.queryForEq("user_id", guid);
        } catch (SQLException exception) {
            exception.printStackTrace();
            return List.of();
        }
    }

    @Override
    public Play getRecordByGuidAndPatternId(int guid, int patternId) {
        try {
            List<PlayData> result = playDao.queryForFieldValuesArgs(Map.of("user_id", guid, "pattern_id", patternId));
            if (result.isEmpty()) {
                return null;
            }
            return result.get(0);
        } catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    @Override
    public void insertScore(Play play) {
        try {
            playDao.create(new PlayData(
                    play.getPattern_id(),
                    play.getUser_id(),
                    play.getScore(),
                    play.getGrade(),
                    play.getIsAllCombo(),
                    play.getIsPerfectPlay(),
                    play.getJudgement()
            ));
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void updateScore(Play play) {
        try {
            List<PlayData> result = playDao.queryForFieldValuesArgs(Map.of("user_id", play.getUser_id(), "pattern_id", play.getPattern_id()));
            if (result.isEmpty()) {
                this.insertScore(play);
            } else {
                PlayData old = result.get(0);
                old.setScore(play.getScore());
                old.setGrade(play.getGrade());
                old.setIsAllCombo(play.getIsAllCombo());
                old.setIsPerfectPlay(play.getIsPerfectPlay());
                old.setJudgement(play.getJudgement());
                playDao.update(old);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
