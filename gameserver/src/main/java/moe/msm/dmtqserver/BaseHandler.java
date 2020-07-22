package moe.msm.dmtqserver;

import fi.iki.elonen.NanoHTTPD;

import java.util.List;
import java.util.Map;

public interface BaseHandler {
    NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session, NanoHTTPD.Method method, String uri, Map<String, String> headers, Map<String, List<String>> parms, Map<String, String> body);
}
