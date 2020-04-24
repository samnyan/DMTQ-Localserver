package icu.samnya.dmtq_server.server.handler;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import icu.samnya.dmtq_server.server.BaseHandler;
import icu.samnya.dmtq_server.server.DatabaseService;
import icu.samnya.dmtq_server.server.exception.MethodNotSupportException;
import icu.samnya.dmtq_server.server.model.Member;
import icu.samnya.dmtq_server.server.model.Play;
import icu.samnya.dmtq_server.server.util.HashUtil;
import icu.samnya.dmtq_server.server.util.JsonUtil;
import icu.samnya.dmtq_server.server.util.StreamUtil;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class DMQHandler implements BaseHandler {

    private final Context ctx;

    private final DatabaseService dbService;

    public DMQHandler(Context ctx, DatabaseService dbService) {
        this.ctx = ctx;
        this.dbService = dbService;
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session, NanoHTTPD.Method httpMethod, String uri, Map<String, String> headers, Map<String, List<String>> parms, Map<String, String> body) {
        if (uri.startsWith("/DMQ/rpc")) {
            String reqJson = body.get("postData");
            try {
                JSONArray requests = new JSONArray(reqJson);

                JSONArray response = new JSONArray();
                for(int i = 0; i < requests.length(); i ++) {
                    Object x = requests.get(i);
                    if (x instanceof JSONObject) {
                        JSONObject request = (JSONObject) x;
                        int id = request.getInt("id");
                        String[] m = request.getString("method").split("\\.");
                        String module = m[0];
                        String method = m[1];
                        JSONArray params = request.getJSONArray("params");

                        JSONObject result = null;
                        try {
                            switch (module) {
                                case "service":
                                    result = serviceHandler(method, params);
                                    break;
                                case "board":
                                    result = boardHandler(method, params);
                                    break;
                                case "game":
                                    result = gameHandler(method, params);
                                    break;
                                case "memo":
                                    result = memoHandler(method, params);
                                    break;
                                case "shop":
                                    result = shopHandler(method, params);
                                    break;
                                case "user":
                                    result = userHandler(method, params);
                                    break;
                            }
                        } catch (MethodNotSupportException e) {
                            System.out.println("Unsupported module: " + module + "." + method);
                        }
                        if (result != null) {
                            response.put(result.put("id", id));
                        }
                    }
                }
                String respJson = response.toString();
                System.out.println("Response: " + respJson);
                // Watch here, escape the slash
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", respJson.replaceAll("/", "\\/"));
            } catch (JSONException e) {
                throw new RuntimeException("JSON ERROR");
            }
        }

        return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "text/html", "NOT_FOUND");
    }


    /**
     * Handle all request go to <code>service</code>
     * @param method The sub method of this request
     * @param params The request body
     * @return The json object of result
     */
    private JSONObject serviceHandler(String method, JSONArray params) throws JSONException  {
        switch (method) {
            case "getInfo": return serviceGetInfoHandler(params);
        }
        throw new MethodNotSupportException();
    }

    /**
     * Handle request go to <code>service.getInfo</code>
     * @param params The request body
     * @return The json object of result
     */
    private JSONObject serviceGetInfoHandler(JSONArray params) throws JSONException {
        String version = params.getString(0);
        String clientOS = params.getString(1);
        SharedPreferences sharedPref = ctx.getSharedPreferences("SERVER_PREFERENCES", Context.MODE_PRIVATE);
        String HOST = sharedPref.getString("HOST_ADDRESS", "localhost:3456");

        return new JSONObject()
                .put("result", new JSONObject()
                        .put("api_url", "http://" + HOST + "/DMQ/rpc")
                        .put("file_server_url", "http://" + HOST + "/patch")
                        .put("file_manage_ver", "1.003.005")
                        .put("service_type", "LIVE")
                        .put("coupon_yn", "Y")
                        .put("terms_yn", "Y")
                        .put("pp_live_status", "Y")
                        .put("inspection_notice_yn", "N")
                        .put("event_url", "http://" + HOST + "/score"))
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle all request go to <code>board</code>
     * @param method The sub method of this request
     * @param params The request body
     * @return The json object of result
     */
    private JSONObject boardHandler(String method, JSONArray params) throws JSONException {
        switch (method) {
            case "getNotice": return boardGetNoticeListHandler();
        }
        throw new MethodNotSupportException();
    }

    /**
     * Handle request go to <code>board.getNoticeList</code>
     * @return The json object of result
     */
    private JSONObject boardGetNoticeListHandler() throws JSONException {

        SharedPreferences sharedPref = ctx.getSharedPreferences("SERVER_PREFERENCES", Context.MODE_PRIVATE);
        String HOST = sharedPref.getString("HOST_ADDRESS", "localhost:3456");

        JSONArray result = new JSONArray();
        result.put(new JSONObject()
                .put("news_id", 4)
                .put("link_url", "http://"+ HOST)
                .put("image_url", "http://" + HOST + "/viewImg/news/banner_musicpack_28.png")
                .put("content", "20028")
                .put("news_type", "N")
        );
        result.put(new JSONObject()
                .put("news_id", 3)
                .put("link_url", "http://"+ HOST)
                .put("image_url", "http://" + HOST + "/viewImg/news/banner_liar.png")
                .put("content", "104")
                .put("news_type", "N")
        );
        result.put(new JSONObject()
                .put("news_id", 2)
                .put("link_url", "http://"+ HOST)
                .put("image_url", "http://" + HOST + "/viewImg/news/phone2.png")
                .put("content", "52")
                .put("news_type", "N")
        );
        result.put(new JSONObject()
                .put("news_id", 1)
                .put("link_url", "http://"+ HOST)
                .put("image_url", "http://" + HOST + "/viewImg/news/phone1.png")
                .put("content", "54")
                .put("news_type", "N")
        );
        return new JSONObject()
                .put("result",result)
                .put("error",JSONObject.NULL);
    }

    /**
     * Handle all request go to <code>game</code>
     * @param method The sub method of this request
     * @param params The request body
     * @return The json object of result
     */
    private JSONObject gameHandler(String method, JSONArray params) throws JSONException  {
        switch (method) {
            case "checkKakao2Global": return gameReturnFalseHandler();
            case "checkPmang2Global": return gameReturnFalseHandler();
            case "checkOld2Global": return gameReturnFalseHandler();
            case "getAchievementCount": return gameGetAchievementCount();
            case "getAdSongList": return gameGetAdSongList();
            case "getAdTicketChecked": return gameGetAdTicketChecked(params);
            case "getAdTicketRequest": return gameGetAdTicketRequest(params);
            case "getAdTicketReceived": return gameGetAdTicketReceived(params);
            case "getAdTicketUsed": return gameGetAdTicketUsed(params);
            case "getDefaultSetting": return gameGetDefaultSetting(params);
            case "getFirstResourceSongList": return gameGetFirstResourceSongList(params);
            case "getGameAssetByPuid": return gameGetGameAssetByPuid(params);
            case "getGameAssetForMigByToken": return gameGetGameAssetForMigByToken();
            case "getGameSettingInfo": return gameGetGameSettingInfo();
            case "getLineScoreMyRank": return gameGetLineScoreMyRank();
            case "getLineScoreRange": return gameGetLineScoreRange();
            case "getLineScoreRangeWithLevel": return gameGetLineScoreRangeWithLevel();
            case "getLineTopRankWithLevel": return gameGetLineTopRankWithLevel(params);
            case "getLineScoreMyRankWithNext": return gameGetLineScoreMyRankWithNext();
            case "getPatternUsePointData": return gameGetPatternUsePointData();
            case "getLineScoreMyRankWithLevel": return gameGetLineScoreMyRankWithLevel();
            case "getOwnAchievementList": return gameGetOwnAchievementList();
            case "getOwnItemList": return gameGetOwnItemList();
            case "getOwnPatternScore": return gameGetOwnPatternScore(params);
            case "getOwnQuestList": return gameGetOwnQuestList();
            case "getOwnSongList": return gameGetOwnSongList();
            case "getPatternUrl": return gameGetPatternUrl(params);
            case "getPreviewPlayInfo": return gameGetPreviewPlayInfo(params);
            case "getSongList": return gameGetSongList();
            case "getSongUrl": return gameGetSongUrl(params);
            case "getUserAsset": return gameGetUserAsset(params);
            case "getUserFreePass": return gameGetUserFreePass();
            case "savePlayResult": return gameSavePlayResult(params);
            case "savePlayResultFailLog": return gameSavePlayResultFailLog(params);
            case "setSlotItem": return gameSetSlotItem(params);
        }
        throw new MethodNotSupportException();
    }

    /**
     * Handle request go to
     * <code>game.checkKakao2Global</code>
     * <code>game.checkPmang2Global</code>
     * <code>game.checkOld2Global</code>
     * @return The json object of result
     */
    private JSONObject gameReturnFalseHandler() throws JSONException {
        return new JSONObject()
                .put("result", false)
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>game.getAchievementCount</code>
     * @return The json object of result
     */
    private JSONObject gameGetAchievementCount() throws JSONException {
        return new JSONObject()
                .put("result", new JSONObject()
                        .put("total_achievement_count", 999)
                        .put("total_achievement_point_sum", 99999))
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>game.getAdSongList</code>
     * @return The json object of result
     */
    private JSONObject gameGetAdSongList() throws JSONException {
        return new JSONObject()
                .put("result", new JSONObject()
                        .put("status", 200)
                        .put("message", "Ad song list")
                        .put("info", new JSONObject().put("songList", new JSONArray()))
                        .put("error", JSONObject.NULL));
    }

    /**
     * Handle request go to <code>game.getAdTicketChecked</code>
     * @return The json object of result
     */
    private JSONObject gameGetAdTicketChecked(JSONArray params) throws JSONException {
        int guid = params.getInt(0);
        return new JSONObject()
                .put("result", new JSONObject()
                        .put("status", 200)
                        .put("message", "Check Ad ticket info")
                        .put("info", new JSONObject()
                                .put("ticket", new JSONObject()
                                        .put("guid", guid)
                                        .put("song_id", 0)
                                        .put("has_ticket", "N")
                                        .put("reg_date", "20170101000000")
                                )
                        )
                        .put("error", JSONObject.NULL));
    }

    /**
     * Handle request go to <code>game.getAdTicketRequest</code>
     * @return The json object of result
     */
    private JSONObject gameGetAdTicketRequest(JSONArray params) throws JSONException {
        return new JSONObject()
                .put("result", new JSONObject()
                        .put("status", 200)
                        .put("message", "I am watching ad movie")
                        .put("info", new JSONArray()
                                .put(new JSONObject()
                                        .put("has_ticket", true)
                                        .put("state", "S")
                                )
                        )
                        .put("error", JSONObject.NULL));
    }

    /**
     * Handle request go to <code>game.getAdTicketReceived</code>
     * @return The json object of result
     */
    private JSONObject gameGetAdTicketReceived(JSONArray params) throws JSONException {
        return new JSONObject()
                .put("result", new JSONObject()
                        .put("status", 200)
                        .put("message", "I got a ticket to play")
                        .put("info", new JSONArray()
                                .put(new JSONObject()
                                        .put("has_ticket", true)
                                        .put("state", "Y")
                                )
                        )
                        .put("error", JSONObject.NULL));
    }

    /**
     * Handle request go to <code>game.getAdTicketUsed</code>
     * @return The json object of result
     */
    private JSONObject gameGetAdTicketUsed(JSONArray params) throws JSONException {
        return new JSONObject()
                .put("result", new JSONObject()
                        .put("status", 200)
                        .put("message", "I used to play")
                        .put("info", new JSONArray()
                                .put(new JSONObject()
                                        .put("has_ticket", true)
                                        .put("state", "N")
                                )
                        )
                        .put("error", JSONObject.NULL));
    }


    /**
     * Handle request go to <code>game.getDefaultSetting</code>
     * @return The json object of result
     */
    private JSONObject gameGetDefaultSetting(JSONArray params) throws JSONException {

        SharedPreferences sharedPref = ctx.getSharedPreferences("SERVER_PREFERENCES", Context.MODE_PRIVATE);
        String HOST = sharedPref.getString("HOST_ADDRESS", "localhost:3456");

        return new JSONObject()
                .put("result", new JSONObject()
                        .put("defaultKeyValue", new JSONArray()
                                .put(new JSONObject().put("key", "1").put("value", "1"))
                                .put(new JSONObject().put("key", "gamesuggest_popup").put("value", "N"))
                                .put(new JSONObject().put("key", "howtoplay_page1").put("value", "http://"+HOST+"/static/dmq/banner/180802_tuto2_package/howtoplay_play.png"))
                                .put(new JSONObject().put("key", "howtoplay_page2").put("value", "http://"+HOST+"/static/dmq/banner/180802_tuto2_package/howtoplay_note.png"))
                                .put(new JSONObject().put("key", "howtoplay_popup").put("value", "N"))
                                .put(new JSONObject().put("key", "tstop_url").put("value", "http://"+HOST+"/static/dmq/banner/180716_mucasummer/180716_tstop_summer.png"))
                                .put(new JSONObject().put("key", "twc_url").put("value", "http://"+HOST+"/static/dmq/banner/180716_mucasummer/180716_twc_summer.png"))
                        )
                        .put("error", JSONObject.NULL));
    }

    /**
     * Handle request go to <code>game.getFirstResourceSongList</code>
     * @return The json object of result
     */
    private JSONObject gameGetFirstResourceSongList(JSONArray params) throws JSONException {
        return new JSONObject()
                .put("result", new JSONObject()
                        .put("first_resource_songs", new JSONArray()
                                .put(new JSONObject().put("song_id", 1).put("name", "oblivion"))
                        )
                        .put("error", JSONObject.NULL));
    }

    /**
     * Handle request go to <code>game.getGameAssetByPuid</code>
     * @param params The request body
     * @return The json object of result
     */
    private JSONObject gameGetGameAssetByPuid(JSONArray params) throws JSONException {
        int guid = params.getInt(0);

        int musicCount = 191;
        JSONArray arr = getMusicList();
        if(arr != null) {
            musicCount = arr.length();
        }

        JSONObject myAssetInfo = new JSONObject();
        myAssetInfo.put("puid", "" + guid);
        myAssetInfo.put("puid", guid);
        myAssetInfo.put("exp", 99999);
        myAssetInfo.put("mpoint", 999999);
        myAssetInfo.put("score", 9999999);
        myAssetInfo.put("lev", 99);
        myAssetInfo.put("amt_total", 999999);
        myAssetInfo.put("amt_cash", 999999);
        myAssetInfo.put("amt_point", 999999);
        myAssetInfo.put("song_count", musicCount);
        myAssetInfo.put("migrated_yn", "Y");

        JSONObject result =  new JSONObject()
                .put("status", "SUCCESS")
                .put("code", 200)
                .put("myAssetInfo", myAssetInfo);

        return new JSONObject()
                .put("result", result)
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>game.getGameAssetForMigByToken</code>
     * @return The json object of result
     */
    private JSONObject gameGetGameAssetForMigByToken() throws JSONException {
        return new JSONObject()
                .put("result", JSONObject.NULL)
                .put("error", new JSONObject()
                        .put("code", "SVC.05001")
                        .put("message", "Error (Invalid pmangplus(member_id) user id)")
                );
    }

    /**
     * Handle request go to <code>game.getGameSettingInfo</code>
     * @return The json object of result
     */
    private JSONObject gameGetGameSettingInfo() throws JSONException {
        return new JSONObject()
                .put("result", new JSONObject()
                        .put("status", 200)
                        .put("code", 200)
                        .put("message", "FOUND THE KEY")
                        .put("info", new JSONObject()
                                .put("key", "HARDEXPERT_ALERT_LEVEL")
                                .put("value", 4)
                        )
                )
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>game.getLineScoreMyRank</code>
     * @return The json object of result
     */
    private JSONObject gameGetLineScoreMyRank() throws JSONException {
        return new JSONObject()
                .put("result", new JSONObject()
                        .put("rank", 0)
                )
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>game.getLineScoreRange</code>
     * @return The json object of result
     */
    private JSONObject gameGetLineScoreRange() throws JSONException {
        return new JSONObject()
                .put("result", new JSONObject()
                        .put("scores", new JSONArray())
                )
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>game.getLineScoreRangeWithLevel</code>
     * @return The json object of result
     */
    private JSONObject gameGetLineScoreRangeWithLevel() throws JSONException {
        return new JSONObject()
                .put("result", new JSONObject()
                        .put("scores", new JSONArray())
                )
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>game.getLineTopRankWithLevel</code>
     * @return The json object of result
     */
    private JSONObject gameGetLineTopRankWithLevel(JSONArray params) throws JSONException {
        JSONArray arr = params.getJSONArray(3);
        int guid = arr.getInt(0);
        Member user = dbService.getUserByGuid(guid);
        return new JSONObject()
                .put("result", new JSONObject()
                        .put("ranks", new JSONArray().put(
                                new JSONObject()
                                        .put("rank",1)
                                        .put("fluctuation",0)
                                        .put("guid",arr.get(0))
                                        .put("score",0)
                                        .put("slot_item1", JsonUtil.jsonNull(user.getSlotItem1()))
                                        .put("slot_item2", JsonUtil.jsonNull(user.getSlotItem2()))
                                        .put("lev",99)
                                        .put("display_name", user.getNickname())
                                        .put("profile_img","")
                        ))
                        .put("rank_day", "20170707")
                )
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>game.getLineScoreMyRankWithNext</code>
     * @return The json object of result
     */
    private JSONObject gameGetLineScoreMyRankWithNext() throws JSONException {
        return new JSONObject()
                .put("result", new JSONObject()
                        .put("scores", new JSONArray())
                )
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>game.getPatternUsePointData</code>
     * @return The json object of result
     */
    private JSONObject gameGetPatternUsePointData() throws JSONException {
        return new JSONObject()
                .put("result", new JSONObject()
                        .put("getPatternUsePointData", new JSONArray()
                                .put(new JSONObject()
                                        .put("pattern_id", 868)
                                        .put("song_id", 17)
                                        .put("point_type", 1)
                                        .put("point_value", 200)
                                )
                        ) // TODO: Read from file
                )
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>game.getLineScoreMyRankWithLevel</code>
     * @return The json object of result
     */
    private JSONObject gameGetLineScoreMyRankWithLevel() throws JSONException {
        return new JSONObject()
                .put("result", new JSONObject()
                        .put("scores", new JSONArray())
                )
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>game.getOwnAchievementList</code>
     * @return The json object of result
     */
    private JSONObject gameGetOwnAchievementList() throws JSONException {
        return new JSONObject()
                .put("result", new JSONObject()
                        .put("own_achievements", new JSONArray())
                )
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>game.getOwnItemList</code>
     * @return The json object of result
     */
    private JSONObject gameGetOwnItemList() throws JSONException{
        return new JSONObject()
                .put("result", new JSONArray())
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>game.getOwnPatternScore</code>
     * @return The json object of result
     */
    private JSONObject gameGetOwnPatternScore(JSONArray params) throws JSONException{
        int guid = params.getInt(0);
        List<Play> recordList = dbService.getRecordsByGuid(guid);
        JSONArray result = new JSONArray();
        for (int i = 0; i < recordList.size(); i++) {
            Play play = recordList.get(i);
            result.put(new JSONObject()
                    .put("guid", play.getUser_id())
                    .put("pattern_id", play.getPattern_id())
                    .put("score", play.getScore())
                    .put("judgement_name", play.getGrade())
                    .put("allcom_yn", play.getIsAllCombo())
                    .put("perfect_yn", play.getIsPerfectPlay())
                    .put("judgement", ((float) play.getJudgement()) / 10)
            );
        }

        return new JSONObject()
                .put("result", result)
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>game.getOwnQuestList</code>
     * @return The json object of result
     */
    private JSONObject gameGetOwnQuestList() throws JSONException {

        JSONObject result = new JSONObject();

        // period_quest start
        JSONObject period_quest = new JSONObject();
        period_quest.put("quest_order",1);
        period_quest.put("quest_id",148);
        period_quest.put("quest_type","E");
        period_quest.put("quest_name","REBOOT! 20주차 퀘스트");
        period_quest.put("description","매주 업데이트되는 퀘스트에 도전하세요!");
        period_quest.put("start_date","20170702000000");
        period_quest.put("end_date","20170708235959");
        period_quest.put("quest_complete_yn","N");

        JSONArray period_quest_own_quest_missions = new JSONArray();
        period_quest_own_quest_missions.put(new JSONObject()
                .put("quest_mission_id", 357)
                .put("quest_mission_complete_yn", "N")
                .put("play_count", 0)
                .put("song_id", 0)
                .put("play_special", "")
                .put("condition_random_song_yn", "N")
                .put("condition_song_id", 0)
                .put("condition_signature", 1)
                .put("condition_line", 0)
                .put("condition_difficulty", 0)
                .put("condition_type", "CLEAR")
                .put("condition_value", 1)
                .put("condition_count", 6)
                .put("condition_special", "")
                .put("description", "조건 1 : NORMAL 패턴 6회 클리어")
        );
        period_quest.put("own_quest_missions", period_quest_own_quest_missions);

        JSONArray period_quest_quest_rewards = new JSONArray();
        period_quest_quest_rewards.put(new JSONObject()
                .put("quest_reward_id", 148)
                .put("quest_id", 148)
                .put("reward_type", "MP")
                .put("reward_value", 2000)
        );

        period_quest.put("quest_rewards", period_quest_quest_rewards);
        period_quest.put("is_updated", true);
        result.put("period_quest", period_quest);
        // period_quest end


        // repeat_quest start
        JSONObject repeat_quest = new JSONObject();
        repeat_quest.put("quest_order",10);
        repeat_quest.put("quest_id",3);
        repeat_quest.put("quest_type","P");
        repeat_quest.put("quest_name","이펙터의 이해");
        repeat_quest.put("description","패턴을 고르고 나면, 이펙터를 고를 수 있어요.\\r\\n\\r\\n노트가 사라지거나, 방향이 바뀌는 등 재미있는 기능을 선보입니다.");
        repeat_quest.put("quest_complete_yn","N");

        JSONArray repeat_quest_own_quest_missions = new JSONArray();
        repeat_quest_own_quest_missions.put(new JSONObject()
                .put("quest_mission_id", 5)
                .put("quest_mission_complete_yn", "N")
                .put("play_count", 0)
                .put("song_id", 0)
                .put("play_special", "")
                .put("condition_random_song_yn", "N")
                .put("condition_song_id", 0)
                .put("condition_signature", 1)
                .put("condition_line", 0)
                .put("condition_difficulty", 0)
                .put("condition_type", "EFFECTOR")
                .put("condition_value", 1)
                .put("condition_count", 1)
                .put("condition_special", "all,no,no no,all,no no,no,all")
                .put("description", "이펙터를 장착한 상태에서 클리어 1회")
        );
        repeat_quest.put("own_quest_missions", repeat_quest_own_quest_missions);

        JSONArray repeat_quest_quest_rewards = new JSONArray();
        repeat_quest_quest_rewards.put(new JSONObject()
                .put("quest_reward_id", 3)
                .put("quest_id", 3)
                .put("reward_type", "CA")
                .put("reward_value", 10)
        );

        repeat_quest.put("quest_rewards", repeat_quest_quest_rewards);
        result.put("repeat_quest", repeat_quest);

        result.put("level_quest", JSONObject.NULL);

        // repeat_quest end
        return new JSONObject()
                .put("result", result)
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>game.getOwnSongList</code>
     * @return The json object of result
     */
    private JSONObject gameGetOwnSongList() throws JSONException {
        JSONArray ids = new JSONArray();
        for (int i = 1; i <= 191; i++) {
            ids.put(i);
        }
        return new JSONObject()
                .put("result", new JSONObject()
                        .put("song_ids", ids)
                )
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>game.getPatternUrl</code>
     * @return The json object of result
     */
    private JSONObject gameGetPatternUrl(JSONArray params) throws JSONException {

        SharedPreferences sharedPref = ctx.getSharedPreferences("SERVER_PREFERENCES", Context.MODE_PRIVATE);
        String HOST = sharedPref.getString("HOST_ADDRESS", "localhost:3456");

        int patternId = params.getInt(1);
        String earphoneMode = params.getString(2);
        String deviceType = params.getString(3);
        String URL = "http://" + HOST + "/Patterns/" + patternId;
        if(earphoneMode.equals("e")) {
            URL = URL + "_EARPHONE";
        }
        return new JSONObject()
                .put("result", URL)
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>game.getPreviewPlayInfo</code>
     * @return The json object of result
     */
    private JSONObject gameGetPreviewPlayInfo(JSONArray params) throws JSONException  {
        int guid = params.getInt(0);
        int patternId = params.getInt(1);

        Member user = dbService.getUserByGuid(guid);
        JSONObject result = new JSONObject();
        result.put("note_count",3);

        JSONObject slot_item_effect = new JSONObject();
        if(user.getSlotItem1() != null) {
            slot_item_effect.put("slot_item1", new JSONObject()
                    .put("item_id", user.getSlotItem1())
                    .put("effect_type", "N")
                    .put("effect_point", 1)
                    .put("effect_count", 1)
                    .put("effect_special", "")
            );
        } else {
            slot_item_effect.put("slot_item1", JSONObject.NULL);
        }
        if(user.getSlotItem2() != null) {
            slot_item_effect.put("slot_item2", new JSONObject()
                    .put("item_id", user.getSlotItem2())
                    .put("effect_type", "N")
                    .put("effect_point", 1)
                    .put("effect_count", 1)
                    .put("effect_special", "")
            );
        } else {
            slot_item_effect.put("slot_item2", JSONObject.NULL);
        }
        if(user.getSlotItem3() != null) {
            slot_item_effect.put("slot_item3", new JSONObject()
                    .put("item_id", user.getSlotItem3())
                    .put("effect_type", "N")
                    .put("effect_point", 1)
                    .put("effect_count", 1)
                    .put("effect_special", "")
            );
        } else {
            slot_item_effect.put("slot_item3", JSONObject.NULL);
        }
        result.put("slot_item_effect", slot_item_effect);

        result.put("in_game_item", new JSONObject()
                .put("in_game_item1", new JSONObject()
                        .put("item_type", "FP")
                        .put("item_level", 10)
                        .put("product_id", 70020)
                        .put("item_effects", new JSONArray()
                                .put(new JSONObject()
                                        .put("item_id", 70020)
                                        .put("effect_type", "F")
                                        .put("effect_point", 5)
                                        .put("effect_count", 3)
                                        .put("effect_special", "")
                                )
                        )
                )
                .put("in_game_item2", new JSONObject()
                        .put("item_type", "GR")
                        .put("item_level", 10)
                        .put("product_id", 70030)
                        .put("item_effects", new JSONArray()
                                .put(new JSONObject()
                                        .put("item_id", 70030)
                                        .put("effect_type", "G")
                                        .put("effect_point", 100)
                                        .put("effect_count", 1)
                                        .put("effect_special", "")
                                )
                        )
                )
                .put("in_game_item3", new JSONObject()
                        .put("item_type", "AB")
                        .put("item_level", 10)
                        .put("product_id", 70010)
                        .put("item_effects", new JSONArray()
                                .put(new JSONObject()
                                        .put("item_id", 70010)
                                        .put("effect_type", "A")
                                        .put("effect_point", 12)
                                        .put("effect_count", 10)
                                        .put("effect_special", "JUDGEMENT_MAX100")
                                )
                        )
                )
        );
        String hash = HashUtil.getMd5(String.valueOf(System.currentTimeMillis() / 100).getBytes());
        hash = hash + HashUtil.getMd5(String.valueOf(System.currentTimeMillis() / 100 + 1).getBytes());
        hash = hash.substring(hash.length() - 4);
        result.put("game_token", hash); // calculate : md5(time()).substr(md5(time() + 1), -4)
        result.put("freepass", false);
        return new JSONObject()
                .put("result", result)
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>game.getSongList</code>
     * @return The json object of result
     */
    private JSONObject gameGetSongList() throws JSONException  {
        JSONArray arr = getMusicList();
        return new JSONObject()
                .put("result", new JSONObject()
                        .put("songs", arr)
                )
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>game.getSongUrl</code>
     * @return The json object of result
     */
    private JSONObject gameGetSongUrl(JSONArray params) throws JSONException  {

        SharedPreferences sharedPref = ctx.getSharedPreferences("SERVER_PREFERENCES", Context.MODE_PRIVATE);
        String ASSET_HOST = sharedPref.getString("ASSET_ADDRESS", "localhost:3456");

        int songId = params.getInt(1);
        String fpk = "http://" + ASSET_HOST + "/Songs/" + songId + ".fpk";
        String webm = "http://" + ASSET_HOST + "/Songs/" + songId + ".webm";
        JSONArray urls = new JSONArray()
                .put(fpk)
                .put(webm);
        return new JSONObject()
                .put("result", new JSONObject()
                        .put("pmang", urls)
                        .put("amazon", urls)
                )
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>game.getUserAsset</code>
     * @return The json object of result
     */
    private JSONObject gameGetUserAsset(JSONArray params) throws JSONException  {
        int guid = params.getInt(0);
        Member user = dbService.getUserByGuid(guid);
        return new JSONObject()
                .put("result", new JSONObject()
                        .put("exp", 0)
                        .put("mpoint", 999999)
                        .put("score", 9999999)
                        .put("slot_item1", JsonUtil.jsonNull(user.getSlotItem1()))
                        .put("slot_item2", JsonUtil.jsonNull(user.getSlotItem2()))
                        .put("slot_item3", JsonUtil.jsonNull(user.getSlotItem3()))
                        .put("slot_item4", JsonUtil.jsonNull(user.getSlotItem4()))
                        .put("in_game_item1", 10)
                        .put("in_game_item2", 10)
                        .put("in_game_item3", 10)
                        .put("lev", 99)
                        .put("amt_total", "999999")
                        .put("amt_cash", "999999")
                        .put("amt_point", "999999")
                        .put("amt_mileage", "999999")
                )
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>game.getUserFreePass</code>
     * @return The json object of result
     */
    private JSONObject gameGetUserFreePass() throws JSONException  {
        return new JSONObject()
                .put("result", new JSONArray())
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>game.savePlayResult</code>
     * @return The json object of result
     */
    private JSONObject gameSavePlayResult(JSONArray params) throws JSONException  {
        int guid = params.getInt(0);
        int patternId = params.getInt(1);
        JSONArray judgementStat = params.getJSONArray(2);
        int maxCombo = params.getInt(3);
        int luckyBonus = params.getInt(4);
        JSONArray effector = params.getJSONArray(5);
        JSONArray ingameItem = params.getJSONArray(6);
        String gameToken = params.getString(7);

        int score = 0;
        int allComboScore = judgementStat.getInt(1) == 0 ? 15000 : 0;
        int perfectPlayScore = 0;
        if(
                judgementStat.getInt(1) == 0 &&
                        judgementStat.getInt(2) == 0 &&
                        judgementStat.getInt(3) == 0 &&
                        judgementStat.getInt(4) == 0 &&
                        judgementStat.getInt(5) == 0 &&
                        judgementStat.getInt(6) == 0 &&
                        judgementStat.getInt(7) == 0 &&
                        judgementStat.getInt(8) == 0 &&
                        judgementStat.getInt(9) == 0 &&
                        judgementStat.getInt(10) == 0 &&
                        judgementStat.getInt(11) == 0
        ) {
            perfectPlayScore = 30000;
        }
        int luckyScore = 0;
        score += judgementStat.getInt(2);
        score += judgementStat.getInt(3) * 20;
        score += judgementStat.getInt(4) * 40;
        score += judgementStat.getInt(5) * 60;
        score += judgementStat.getInt(6) * 80;
        score += judgementStat.getInt(7) * 100;
        score += judgementStat.getInt(8) * 120;
        score += judgementStat.getInt(9) * 140;
        score += judgementStat.getInt(10) * 160;
        score += judgementStat.getInt(11) * 180;
        score += judgementStat.getInt(12) * 200;

        int totalScore = score + allComboScore + perfectPlayScore + luckyScore;

        int maxPoint =
                (
                        judgementStat.getInt(1) +
                                judgementStat.getInt(2) +
                                judgementStat.getInt(3) +
                                judgementStat.getInt(4) +
                                judgementStat.getInt(5) +
                                judgementStat.getInt(6) +
                                judgementStat.getInt(7) +
                                judgementStat.getInt(8) +
                                judgementStat.getInt(9) +
                                judgementStat.getInt(10) +
                                judgementStat.getInt(11) +
                                judgementStat.getInt(12)
                ) * 100;

        int nowPoint = (score - judgementStat.getInt(2)) / 2 + judgementStat.getInt(2);
        int realPointRatio = Math.round(((float) nowPoint) / ((float) maxPoint) * 1000);
        int pointRatio = realPointRatio / 10;

        String grade = "F";
        if (pointRatio >= 98) {
            grade = "S";
        } else if (pointRatio >= 90) {
            grade = "A";
        } else if (pointRatio >= 80) {
            grade = "B";
        } else if (pointRatio >= 70) {
            grade = "C";
        } else if (pointRatio >= 60) {
            grade = "D";
        } else if (pointRatio >= 50) {
            grade = "E";
        }

        Integer lastScore = null;

        Play play = dbService.getRecordByGuidAndPatternId(guid, patternId);
        if(play == null) {
            play = new Play(
                    patternId,
                    guid
            );
        } else {
            lastScore = play.getScore();
        }
        play.setIsAllCombo(play.getIsAllCombo().equals("Y") ? "Y" : allComboScore != 0 ? "Y" : "N");
        play.setIsPerfectPlay(play.getIsPerfectPlay().equals("Y") ? "Y" : perfectPlayScore != 0 ? "Y" : "N");
        play.setScore(Math.max(score, play.getScore()));
        if(realPointRatio > play.getJudgement()) {
            play.setGrade(grade);
        }
        play.setJudgement(realPointRatio);
        if(lastScore == null) {
            dbService.insertScore(play);
        } else {
            dbService.updateScore(play);
        }

        JSONObject result = new JSONObject();
        result.put("is_success", true);
        result.put("is_first_pattern", lastScore == null);
        result.put("is_new_record", lastScore == null || lastScore < totalScore);
        result.put("judgement_name", grade);
        result.put("allcom_yn", allComboScore != 0 ? "Y" : "N");
        result.put("perfect_yn", perfectPlayScore != 0 ? "Y" : "N");
        result.put("bonus_score", allComboScore + perfectPlayScore);
        result.put("lucky_bonus_score", luckyScore);
        result.put("score", totalScore);
        result.put("total_score", totalScore);
        result.put("exp", 10);
        result.put("total_exp", 10);
        result.put("mpoint", 150);
        result.put("total_mpoint", 150);
        result.put("lev", 99);
        result.put("new_achievements", new JSONArray());
        result.put("own_quests", new JSONObject()
                .put("complete", new JSONObject()
                        .put("period_quest", JSONObject.NULL)
                        .put("level_quest", JSONObject.NULL)
                        .put("repeat_quest", JSONObject.NULL)
                )
                .put("going", new JSONObject()
                        .put("period_quest", JSONObject.NULL)
                        .put("level_quest", JSONObject.NULL)
                        .put("repeat_quest", JSONObject.NULL)
                )
        );

        return new JSONObject()
                .put("result", result)
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>game.savePlayResultFailLog</code>
     * @return The json object of result
     */
    private JSONObject gameSavePlayResultFailLog(JSONArray params) throws JSONException  {
        return new JSONObject()
                .put("result", new JSONObject()
                        .put("status", true)
                )
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>game.setSlotItem</code>
     * @return The json object of result
     */
    private JSONObject gameSetSlotItem(JSONArray params) throws JSONException  {
        int guid = params.getInt(0);
        int slotItem1 = params.getInt(1);
        int slotItem2 = params.getInt(2);
        int slotItem3 = params.getInt(3);
        int slotItem4 = params.getInt(4);
        Member member = dbService.getUserByGuid(guid);
        member.setSlotItem1(slotItem1 != 0 ? slotItem1 : null);
        member.setSlotItem2(slotItem2 != 0 ? slotItem2 : null);
        member.setSlotItem3(slotItem3 != 0 ? slotItem3 : null);
        member.setSlotItem4(slotItem4 != 0 ? slotItem4 : null);
        dbService.updateUser(member);
        return new JSONObject()
                .put("result", new JSONObject()
                        .put("slot_item1", slotItem1)
                        .put("slot_item2", slotItem2)
                        .put("slot_item3", slotItem3)
                        .put("slot_item4", slotItem4)
                )
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle all request go to <code>memo</code>
     * @param method The sub method of this request
     * @param params The request body
     * @return The json object of result
     */
    private JSONObject memoHandler(String method, JSONArray params) throws JSONException  {
        switch (method) {
            case "getMemoList": return new JSONObject()
                    .put("result", new JSONArray())
                    .put("error", JSONObject.NULL);
        }
        throw new MethodNotSupportException();
    }

    /**
     * Handle all request go to <code>shop</code>
     * @param method The sub method of this request
     * @param params The request body
     * @return The json object of result
     */
    private JSONObject shopHandler(String method, JSONArray params) throws JSONException  {
        switch (method) {
            case "buyFreeProduct": return shopBuyFreeProduct(params);
            case "buyProductByQPoint": return shopBuyProductByQPoint(params);
            case "getPackageInfoByGuid": return shopGetPackageInfoByGuid(params);
            case "getProductForUnlock": return shopGetProductForUnlock(params);
            case "getOwnItemList": return shopGetOwnItemList(params);
            case "getUnlockedProductList": return shopGetUnlockedProductList(params);
            case "upgradeInGameItem": return shopUpgradeInGameItem(params);
        }
        throw new MethodNotSupportException();
    }

    /**
     * Handle request go to <code>shop.buyFreeProduct</code>
     * @return The json object of result
     */
    private JSONObject shopBuyFreeProduct(JSONArray params) throws JSONException  {
        return new JSONObject()
                .put("result", true
                )
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>shop.buyProductByQPoint</code>
     * @return The json object of result
     */
    private JSONObject shopBuyProductByQPoint(JSONArray params) throws JSONException  {
        Object productId = params.get(1);
        return new JSONObject()
                .put("result", new JSONObject()
                        .put("status", true)
                        .put("product_id", productId)
                        .put("message", "SUCCESS")
                )
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>shop.getPackageInfoByGuid</code>
     * @return The json object of result
     */
    private JSONObject shopGetPackageInfoByGuid(JSONArray params) throws JSONException  {
        JSONObject result = new JSONObject();
        result.put("packageInfo", new JSONArray());
        result.put("packageInfoCount", 10);
        result.put("purchasedPackageInfo", new JSONArray());
        result.put("purchasedPackageInfoCount", 0);
        result.put("packageItemList", new JSONArray());

        return new JSONObject()
                .put("result", result
                )
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>shop.getProductForUnlock</code>
     * @return The json object of result
     */
    private JSONObject shopGetProductForUnlock(JSONArray params) throws JSONException  {
        JSONObject result = new JSONObject();
        result.put("product_id", 80002);
        result.put("platform_product_id", 909);
        result.put("store_product_id", "com.neowizInternet.game.dmtq.unlock2");
        result.put("product_type", "I");
        result.put("cost_game_point", 0);
        result.put("cost_game_cash", 7);
        result.put("item_id", 80002);
        result.put("item_name", "레벨 언락 2");
        result.put("img_url_1", "");
        result.put("img_url_2", "");
        result.put("summary", "레벨 언락 2");
        result.put("description", "");
        result.put("repeat_count", 0);
        result.put("item_type", "L");
        result.put("limit_minute", 0);
        result.put("status", "N");
        result.put("buy_level", 0);
        result.put("buy_limit_count", 0);
        result.put("buy_limit_type", "");
        result.put("reg_date", "20130704152633");
        result.put("sale_start_date", "");
        result.put("sale_end_date", "");
        result.put("display_order", 0);

        return new JSONObject()
                .put("result", result
                )
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>shop.getOwnItemList</code>
     * @return The json object of result
     */
    private JSONObject shopGetOwnItemList(JSONArray params) throws JSONException  {
        JSONArray result = new JSONArray();
        List<Integer> ids = new LinkedList<>();
        for(int i = 30001; i <= 30030; i ++) {
            ids.add(i);
        }
        for(int i = 40001; i <= 40004; i ++) {
            ids.add(i);
        }
        for(int i = 90001; i <= 90009; i ++) {
            ids.add(i);
        }
        for(int i = 100001; i <= 100020; i ++) {
            ids.add(i);
        }
        for(int i = 190001; i <= 190020; i ++) {
            ids.add(i);
        }
        for(int i = 0; i < ids.size(); i ++) {
            result.put(new JSONObject()
                    .put("item_id", ids.get(i))
                    .put("own_count", 99)
                    .put("repeat_count", 999)
                    .put("using_yn", "Y")
                    .put("reg_date", "20170709025007")
                    .put("end_date", "")
            );
        }

        return new JSONObject()
                .put("result", result)
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>shop.getUnlockedProductList</code>
     * @return The json object of result
     */
    private JSONObject shopGetUnlockedProductList(JSONArray params) throws JSONException  {
        JSONArray result = new JSONArray();

        return new JSONObject()
                .put("result", result
                )
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle request go to <code>shop.upgradeInGameItem</code>
     * @return The json object of result
     */
    private JSONObject shopUpgradeInGameItem(JSONArray params) throws JSONException  {
        JSONArray result = new JSONArray();

        return new JSONObject()
                .put("result", result
                )
                .put("error", JSONObject.NULL);
    }

    /**
     * Handle all request go to <code>user</code>
     * @param method The sub method of this request
     * @param params The request body
     * @return The json object of result
     */
    private JSONObject userHandler(String method, JSONArray params) throws JSONException  {
        switch (method) {
            case "getConnectUuid": return userGetConnectUuid(params);
            case "getUsersByPuid": return userGetUsersByPuid(params);
            case "loginV2": return userLoginV2(params);
            case "setNickname": return userSetNickname(params);
            case "setConnectUuid": return userSetConnectUuid(params);
        }
        throw new MethodNotSupportException();
    }

    /**
     * TODO
     * Handle request go to <code>user.getConnectUuid</code>
     * @return The json object of result
     */
    private JSONObject userGetConnectUuid(JSONArray params) throws JSONException  {
        Integer guid = (Integer) params.get(0);
        if(true) {
            return new JSONObject()
                    .put("result", new JSONObject()
                            .put("status", "SUCCESS")
                            .put("code", 200)
                            .put("uuid", "8b7b80d733")
                            .put("result_msg", "OK")
                    )
                    .put("error", JSONObject.NULL);
        } else {
            return new JSONObject()
                    .put("result", JSONObject.NULL)
                    .put("error", new JSONObject()
                            .put("code", "SVC.05001")
                            .put("message", "Error (Invalid pmangplus(member_id) user id)")
                    );
        }
    }

    /**
     * Handle request go to <code>user.getUsersByPuid</code>
     * @return The json object of result
     */
    private JSONObject userGetUsersByPuid(JSONArray params) throws JSONException  {
        int guid = (params.getJSONArray(0)).getInt(0);
        String param = (params.getJSONArray(1)).getString(0);
        Member user = dbService.getUserByGuid(guid);
        JSONArray result = new JSONArray();
        result.put(new JSONObject().put("display_name", user.getNickname()));
        return new JSONObject()
                .put("result", result
                )
                .put("error", JSONObject.NULL);

    }

    /**
     * Handle request go to <code>user.loginV2</code>
     * @return The json object of result
     */
    private JSONObject userLoginV2(JSONArray params) throws JSONException  {

        SharedPreferences sharedPref = ctx.getSharedPreferences("SERVER_PREFERENCES", Context.MODE_PRIVATE);
        String HOST = sharedPref.getString("HOST_ADDRESS", "localhost:3456");

        String[] tokens = ((String) params.get(0)).split("\\|");
        String puid = tokens[0];
        String appId = tokens[1];
        String deviceCode = tokens[2];
        String serverCode = tokens[3];
        String token = tokens[4];
        String accessTime = tokens[5];
        // Register new user
        String str = HashUtil.getMd5(token.getBytes()) + HashUtil.getMd5(token.getBytes());
        str = str.substring(str.length() - 58);
        JSONObject result = new JSONObject()
                .put("API_TOKEN", str)
                .put("SECRET_KEY", "DMQGLBlive1")
                .put("SECRET_VER", "1")
                .put("guid", puid)
                .put("recom_code", "AAAAAA")
                .put("displayName", " ")
                .put("profileImg", "")
                .put("INTRO_SERVER", "http://" + HOST + "/DMQ/rpc")
                ;
        return new JSONObject()
                .put("result", result
                )
                .put("error", JSONObject.NULL);

    }

    /**
     * Handle request go to <code>user.setNickname</code>
     * @return The json object of result
     */
    private JSONObject userSetNickname(JSONArray params) throws JSONException  {
        int guid = params.getInt(0);
        String nickName = params.getString(1);
        Member member = dbService.getUserByGuid(guid);
        member.setNickname(nickName);
        dbService.updateUser(member);
        // save to db
        return new JSONObject()
                .put("result", true)
                .put("error", JSONObject.NULL);

    }

    /**
     * Handle request go to <code>user.setConnectUuid</code>
     * @return The json object of result
     */
    private JSONObject userSetConnectUuid(JSONArray params) throws JSONException  {

        if(true) {
            return new JSONObject()
                    .put("result", new JSONObject()
                            .put("status", "SUCCESS")
                            .put("code", 200)
                            .put("result_msg", "OK")
                    )
                    .put("error", JSONObject.NULL);

        } else {
            return new JSONObject()
                    .put("result", JSONObject.NULL)
                    .put("error", new JSONObject()
                            .put("code", "SVC.05001")
                            .put("message", "Error (Invalid pmangplus(member_id) user id)")
                    );
        }
    }

    private JSONArray getMusicList() {
        try {
            File dir = ctx.getExternalFilesDir("");
            File file = new File(dir, "songList.json");
            String json = StreamUtil.readAllLine(new FileInputStream(file));
            return new JSONArray(json);
        } catch (IOException | JSONException e) {
            Log.w("DMQHandler", "songList.json not found, use default value");
            return null;
        }
    }
}
