package com.osp.issue.filter;

import com.osp.issue.dto.BaseAlarmDto;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

/**
 * 报警过滤接口
 *
 * @author hddchange
 * @since 2018/05/29
 */
public interface Filter {

    /**
     * 根据具体实现过滤报警邮件
     *
     * @param baseAlarmDto 报警内容
     * @return 过滤结果
     */
    Boolean filter(BaseAlarmDto baseAlarmDto);

    /**
     * 根据配置文件初始化Filter
     *
     * @param map 属性
     */
    default void init(LinkedHashMap map) {
        // 读取配置文件，装配appender
        Class<? extends Filter> filterClass = this.getClass();
        Field[] fields = filterClass.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.set(this, map.get(field.getName()));
            } catch (IllegalAccessException e) {
                // ignore
            }
        }
    }

}
