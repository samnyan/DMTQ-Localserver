package moe.msm.dmtqjavaserver.service;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import moe.msm.dmtqjavaserver.Main;
import moe.msm.dmtqjavaserver.model.MemberData;
import moe.msm.dmtqjavaserver.model.PlayData;
import moe.msm.dmtqserver.external.GameDatabaseService;
import moe.msm.dmtqserver.model.Member;
import moe.msm.dmtqserver.model.Play;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

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
