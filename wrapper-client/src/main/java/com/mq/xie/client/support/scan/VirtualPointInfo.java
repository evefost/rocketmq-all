package com.mq.xie.client.support.scan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xieyang on 18/7/15.
 */
public class VirtualPointInfo {

    private  Map<String/*topic*/,MethodInfo> methodInfoMap = new HashMap<String, MethodInfo>();


    private  Map<String/*topic*/,List<String>/*tag*/> topicTags = new HashMap<>();

    public Map<String, MethodInfo> getMethodInfoMap() {
        return methodInfoMap;
    }


    public MethodInfo getMethodInfo(String key) {
        return methodInfoMap.get(key);
    }

    public MethodInfo putMethodInfo(String key,MethodInfo methodInfo) {
        return methodInfoMap.put(key,methodInfo);
    }

    public void setMethodInfoMap(Map<String, MethodInfo> methodInfoMap) {
        this.methodInfoMap = methodInfoMap;
    }

    public Map<String, List<String>> getTopicTags() {
        return topicTags;
    }

    public void setTopicTags(Map<String, List<String>> topicTags) {
        this.topicTags = topicTags;
    }

    public List<String> getTags(String topic) {
      return   this.topicTags.get(topic);
    }
}
