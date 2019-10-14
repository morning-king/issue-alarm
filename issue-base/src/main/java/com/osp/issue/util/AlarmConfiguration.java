package com.osp.issue.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 读取配置文件工具类
 *
 * @author huangqiaowei
 * @since 2019-05-29 14:55
 **/
@Slf4j
public class AlarmConfiguration {

    /**
     * 加载alarm工程配置文件名
     */
    private static final String BOOTSTRAP_FILE = "alarm.yml";
    private static final String DEFAULT_APPENDERS = "appenders";
    private static final String DEFAULT_FILTERS = "filters";
    private ConcurrentHashMap map;

    public AlarmConfiguration() {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(BOOTSTRAP_FILE);
        Yaml props = new Yaml();
        map = new ConcurrentHashMap<>(props.load(in));
        if (MapUtils.isEmpty(map)) {
            log.error("alarm工程初始化失败，读取配置文件为空");
            throw new NullPointerException("alarm配置文件为空");
        } else {
            log.info("初始化报警配置：{}", map);
        }
    }

    /**
     * 获取所有appender
     *
     * @return map
     */
    public LinkedHashMap getAppenders() {
        return (LinkedHashMap) map.get(DEFAULT_APPENDERS);
    }

    /**
     * 获取所有的filters
     *
     * @return map
     */
    public LinkedHashMap getFilters() {
        return (LinkedHashMap) map.get(DEFAULT_FILTERS);
    }
}
