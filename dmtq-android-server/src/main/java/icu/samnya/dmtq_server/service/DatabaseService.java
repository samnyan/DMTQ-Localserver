package icu.samnya.dmtq_server.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import moe.msm.dmtqserver.external.GameDatabaseService;
import moe.msm.dmtqserver.model.Member;
import moe.msm.dmtqserver.model.Play;
import moe.msm.dmtqserver.model.impl.MemberData;
import moe.msm.dmtqserver.model.impl.PlayData;
import moe.msm.dmtqserver.util.HashUtil;

public class DatabaseService implements GameDatabaseService {

    private final SQLiteDatabase db;

    private String TABLE_MEMBER = "Member";
    private String TABLE_PLAY = "Play";

    public DatabaseService(SQLiteDatabase db) {
        this.db = db;
        this.init();
    }

    public void close() {
        this.db.close();
    }

    public void init() {
        Log.i("Database", "Database initialize");
        db.execSQL("" +
                "CREATE TABLE IF NOT EXISTS Member (\n" +
                "    id         INTEGER,\n" +
                "    nickname   NVARCHAR (255),\n" +
                "    guid       VARCHAR (255),\n" +
                "    puid       VARCHAR (255),\n" +
                "    udid       TEXT,\n" +
                "    code       TEXT,\n" +
                "    slot_item1 INTEGER,\n" +
                "    slot_item2 INTEGER,\n" +
                "    slot_item3 INTEGER,\n" +
                "    slot_item4 INTEGER\n" +
                ");");
        db.execSQL("" +
                "CREATE TABLE IF NOT EXISTS Play (\n" +
                "    pattern_id    INTEGER,\n" +
                "    user_id       INTEGER,\n" +
                "    score         INTEGER,\n" +
                "    grade         CHAR (1),\n" +
                "    isAllCombo    CHAR (1),\n" +
                "    isPerfectPlay CHAR (1),\n" +
                "    judgement     INTEGER\n" +
                ");\n");
    }

    public Member getUserByUdid(String udid) {
        Cursor cursor = db.query(
                TABLE_MEMBER,
                null,
                "udid = ?",
                new String[]{udid},
                null,
                null,
                null
        );
        if (cursor.getCount() < 1) {
            // get max id
            Cursor tempId = db.rawQuery("SELECT IFNULL(MAX(id), 0) as id FROM " + TABLE_MEMBER, null);
            tempId.moveToFirst();
            int id = tempId.getInt(tempId.getColumnIndex("id")) + 1;
            String code = "CODE_" + id;
            code = HashUtil.getMd5(code.getBytes());
            code = code.substring(code.length() - 10);
            Member member = new MemberData(id, "NewUser", String.valueOf(id), String.valueOf(id), udid, code, null, null, null, null);
            ContentValues sql = memberToSql(member);
            long result = db.insert(TABLE_MEMBER, null, sql);
            if(result == -1) {
                throw new RuntimeException("SQL INSERT FAIL");
            }
            tempId.close();
            return member;
        }
        cursor.moveToFirst();
        Member member = mapToMember(cursor);
        cursor.close();
        return member;
    }

    public Member getUserByGuid(int guid) {
        Cursor cursor = db.query(
                TABLE_MEMBER,
                null,
                "guid = ?",
                new String[]{String.valueOf(guid)},
                null,
                null,
                null
        );
        cursor.moveToFirst();
        Member member = mapToMember(cursor);
        cursor.close();
        return member;
    }

    public void updateUser(Member member) {
        long result = db.update(
                TABLE_MEMBER,
                memberToSql(member),
                "id = ?",
                new String[]{String.valueOf(member.getId())}
        );
        if(result == -1) {
            throw new RuntimeException("SQL INSERT FAIL");
        }
    }

    public List<Play> getRecordsByGuid(int guid) {
        Cursor cursor = db.query(
                TABLE_PLAY,
                null,
                "user_id = ?",
                new String[]{String.valueOf(guid)},
                null,
                null,
                null
        );
        List<Play> result = mapToPlays(cursor);
        cursor.close();
        return result;
    }

    public Play getRecordByGuidAndPatternId(int guid, int patternId) {
        Cursor cursor = db.query(
                TABLE_PLAY,
                null,
                "user_id = ? AND pattern_id = ?",
                new String[]{String.valueOf(guid), String.valueOf(patternId)},
                null,
                null,
                null
        );
        if(cursor.getCount() < 1) {
            cursor.close();
            return null;
        } else {
            cursor.moveToFirst();
            Play play = mapToPlay(cursor);
            cursor.close();
            return play;
        }
    }

    public void insertScore(Play play) {
        long result = db.insert(TABLE_PLAY, null, playToSql(play));
        if(result == -1) {
            throw new RuntimeException("SQL INSERT FAIL");
        }
    }

    public void updateScore(Play play) {
        long result = db.update(
                TABLE_PLAY,
                playToSql(play),
                "user_id = ? AND pattern_id = ?",
                new String[]{String.valueOf(play.getUser_id()), String.valueOf(play.getPattern_id())}
                );
        if(result == -1) {
            throw new RuntimeException("SQL INSERT FAIL");
        }
    }

    private Member mapToMember(Cursor cursor) {
        return new MemberData(
                cursor.getInt(cursor.getColumnIndex("id")),
                cursor.getString(cursor.getColumnIndex("nickname")),
                cursor.getString(cursor.getColumnIndex("guid")),
                cursor.getString(cursor.getColumnIndex("puid")),
                cursor.getString(cursor.getColumnIndex("udid")),
                cursor.getString(cursor.getColumnIndex("code")),
                cursor.getInt(cursor.getColumnIndex("slot_item1")),
                cursor.getInt(cursor.getColumnIndex("slot_item2")),
                cursor.getInt(cursor.getColumnIndex("slot_item3")),
                cursor.getInt(cursor.getColumnIndex("slot_item4"))
        );
    }

    private ContentValues memberToSql(Member member) {
        ContentValues values = new ContentValues();
        values.put("id", member.getId());
        values.put("nickname", member.getNickname());
        values.put("guid", member.getGuid());
        values.put("puid", member.getPuid());
        values.put("udid", member.getUdid());
        values.put("code", member.getCode());
        values.put("slot_item1", member.getSlotItem1());
        values.put("slot_item2", member.getSlotItem2());
        values.put("slot_item3", member.getSlotItem3());
        values.put("slot_item4", member.getSlotItem4());
        return values;
    }

    private Play mapToPlay(Cursor cursor) {
        return new PlayData(
                cursor.getInt(cursor.getColumnIndex("pattern_id")),
                cursor.getInt(cursor.getColumnIndex("user_id")),
                cursor.getInt(cursor.getColumnIndex("score")),
                cursor.getString(cursor.getColumnIndex("grade")),
                cursor.getString(cursor.getColumnIndex("isAllCombo")),
                cursor.getString(cursor.getColumnIndex("isPerfectPlay")),
                cursor.getInt(cursor.getColumnIndex("judgement"))
        );
    }

    private List<Play> mapToPlays(Cursor cursor) {
        List<Play> result = new LinkedList<>();
        while (cursor.moveToNext()) {
            result.add(mapToPlay(cursor));
        }
        return result;
    }

    private ContentValues playToSql(Play play) {
        ContentValues values = new ContentValues();
        values.put("pattern_id", play.getPattern_id());
        values.put("user_id", play.getUser_id());
        values.put("score", play.getScore());
        values.put("grade", play.getGrade());
        values.put("isAllCombo", play.getIsAllCombo());
        values.put("isPerfectPlay", play.getIsPerfectPlay());
        values.put("judgement", play.getJudgement());
        return values;
    }

}
