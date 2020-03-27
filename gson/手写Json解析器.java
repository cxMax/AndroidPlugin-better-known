package com.huajiao.network.Request;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * @describe : 手写json串， 暂时只支持string类型， 因为其他类型，涉及到类型判断， 就不写了，主要是思路的提供
 * @usage :
 * 参考 : https://zhuanlan.zhihu.com/p/28049617
 * <p>
 * </p>
 * Created by caixi on 2020-03-27.
 */
public class ParseJson {

    private String json = "{\"1\":\"123\",\"2\":\"234\"}";
    private int i = 0;
    private Map<String, String> result = new HashMap<>();

    public void parse() {
        parseValue();
    }

    private String parseValue() {
        // 这里判断value, 根据value的类型来判断
        if (json.charAt(i) == '{') {
            parseObject();
        }  else if (json.charAt(i) =='"') { // 字符串
            return parseString();
        }
        return null;
    }

    private void parseObject() {
        i++;
        while (json.charAt(i) != '}') {
            String key = parseString();
            i++; //todo 这里判断是不是冒号
            String value = parseValue();
            result.put(key, value);
            Log.e("info", "parseObject: key - " + key );
            Log.e("info", "parseObject: value - " + value );
            if (json.charAt(i) == ',') {
                i++;
            }
        }
    }

    private String parseString() {
        String res = "";
        i++;
        while(json.charAt(i) != '"') {
            res += json.charAt(i++);
        }
        i++;
        return res;
    }
}
