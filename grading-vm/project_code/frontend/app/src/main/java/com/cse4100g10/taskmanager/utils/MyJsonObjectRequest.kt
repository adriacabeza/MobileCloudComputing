package com.cse4100g10.taskmanager.utils

import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonRequest
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class MyJsonObjectRequest(method:Int, url:String, jsonRequest:JSONArray,
                          listener:Response.Listener<JSONObject>, errorListener:Response.ErrorListener):JsonRequest<JSONObject>(method, url, if ((jsonRequest == null)) null else jsonRequest.toString(), listener, errorListener) {

    override fun parseNetworkResponse(response:NetworkResponse):Response<JSONObject> {
        return try {
            val jsonString = String(response.data,
                Charset.forName(HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET)))
            Response.success(JSONObject(jsonString),
                HttpHeaderParser.parseCacheHeaders(response))
        } catch (e:UnsupportedEncodingException) {
            Response.error(ParseError(e))
        } catch (je:JSONException) {
            Response.error(ParseError(je))
        }
    }
}
