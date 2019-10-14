package com.osp.issue.appender;

import com.osp.issue.dto.BaseAlarmDto;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

/**
 * 附加目的地
 *
 * @author hddchange
 */
public interface Appender {

    /**
     * 附加目的地
     *
     * @param baseAlarmDto 报警信息
     */
    void append(BaseAlarmDto baseAlarmDto);

    /**
     * 根据配置文件初始化appender
     *
     * @param map 属性
     */
    default void init(LinkedHashMap map) {
        // 读取配置文件，装配appender
        Class<? extends Appender> appenderClass = this.getClass();
        Field[] fields = appenderClass.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.set(this, map.get(field.getName()));
            } catch (IllegalAccessException e) {
                // ignore
            }
        }
    }

}
