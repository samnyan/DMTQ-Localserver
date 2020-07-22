package moe.msm.dmtqserver.handler;


import moe.msm.dmtqserver.BaseHandler;
import moe.msm.dmtqserver.external.GameDatabaseService;
import moe.msm.dmtqserver.model.Member;
import moe.msm.dmtqserver.model.ServerConfig;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class NeonApiHandler implements BaseHandler {

    private final ServerConfig config;
    private final GameDatabaseService dbService;

    public NeonApiHandler(ServerConfig config, GameDatabaseService dbService) {
        this.config = config;
        this.dbService = dbService;
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session, NanoHTTPD.Method method, String uri, Map<String, String> headers, Map<String, List<String>> parms, Map<String, String> body) {

        try{
        // Just match the url here
        if(uri.startsWith("/api/accounts/v3/global/login_dmq")) {
            return handleLoginDmq(parms);
        }
        if(uri.startsWith("/api/resource/rooting_app.json?device_cd=ANDROID&local_cd=ENG")) {
            return handleRoutingApp();
        }
        if(uri.startsWith("/api/apns/576/register/")) {
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{\n" +
                    "    \"error_params\": null,\n" +
                    "    \"error_timestamp\": \"2019-02-01 13:08:20\",\n" +
                    "    \"error_host\": \"neon-api3.nwz.kr\",\n" +
                    "    \"error_message\": \"java.lang.NullPointerException\",\n" +
                    "    \"result_msg\": \"API_ERR_EXEC\",\n" +
                    "    \"result_code\": \"300\"\n" +
                    "}");
        }
        if(uri.startsWith("/api/product/app/")) {
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{\"value\": [],\"result_msg\": \"API_OK\",\"result_code\": \"000\"}");
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{\"value\":\"OK\",\"result_code\":\"000\",\"result_msg\":\"API_OK\"}");
        } catch (JSONException e) {
            throw new RuntimeException("JSON ERROR");
        }
    }

    public NanoHTTPD.Response handleLoginDmq(Map<String, List<String>> parms) throws JSONException {
        String udid = parms.get("udid").get(0);
        Member m = dbService.getUserByUdid(udid);

        long now = System.currentTimeMillis();
        JSONObject root = new JSONObject();
        JSONObject value = new JSONObject();
        value.put("access_token", m.getId() + "|576|IPHONE|KR|0f4ba353adde7d46f1fcde20b3bc66dd3680216a|" + System.currentTimeMillis());

        JSONObject member = new JSONObject();
        member.put("crt_dt", now); // time
        member.put("upd_dt", now); // time
        member.put("status_cd", "OK");
        member.put("member_id", 1);
        member.put("nickname", m.getNickname());
        member.put("profile_img_url", "");
        member.put("feeling", JSONObject.NULL);
        member.put("adult_auth_yn", "N");
        member.put("adult_auth_dt", JSONObject.NULL);
        member.put("recent_login_dt", now);
        member.put("recent_app_id", JSONObject.NULL);
        member.put("email", JSONObject.NULL);
        member.put("anonymous_yn", "N");
        member.put("reg_path", "GAMECENTER");
        member.put("recent_app_title", JSONObject.NULL);
        member.put("last_msg_dt", JSONObject.NULL);
        member.put("new_msg_yn", JSONObject.NULL);
        member.put("friend_accept_cd", "MANUAL");
        member.put("conflict_member_id", JSONObject.NULL);
        member.put("reg_ip", "127.0.0.1");
        member.put("reg_nation", "HK");
        member.put("is_guest_login", false);
        member.put("provider_display_name", "");
        member.put("pushgroup", JSONObject.NULL);
        member.put("locale", JSONObject.NULL);
        member.put("sanction", false);
        member.put("profile_img_url_raw", "");

        value.put("member",member);
        value.put("conflict_member_id", JSONObject.NULL);
        value.put("is_guest_login", false);
        value.put("old_member_id", JSONObject.NULL);
        value.put("jailbreak_yn", "N");
        value.put("unreg_status", "NO");
        value.put("callTime", 0);

        root.put("value", value);
        root.put("result_msg", "API_OK");
        root.put("result_code", "000");

        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", root.toString());
    }

    public NanoHTTPD.Response handleRoutingApp() {
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{\"value\":[],\"result_msg\":\"API_OK\",\"result_code\":\"000\"}");
    }
}
