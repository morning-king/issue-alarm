package com.osp.issue.filter.impl;

import com.osp.issue.dto.BaseAlarmDto;
import com.osp.issue.filter.Filter;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/**
 * 根据错误消息过滤
 *
 * @author huangqiaowei
 * @since 2019-05-29 17:04
 **/
@Data
public class FilterByMsg implements Filter {

    private static List<String> ruleStr;
    /**
     * 过滤规则
     */
    private String rule;

    /**
     * 根据具体实现过滤报警邮件
     *
     * @param baseAlarmDto 报警内容
     * @return 过滤结果 满足true
     */
    @Override
    public Boolean filter(BaseAlarmDto baseAlarmDto) {
        boolean temp = false;
        for (String str : ruleStr) {
            if (Optional.ofNullable(baseAlarmDto.getErrorMessage()).orElse("").matches(str)) {
                temp = true;
                break;
            }
        }
        return temp;
    }

    /**
     * 根据配置文件初始化Filter
     *
     * @param map 属性
     */
    @Override
    public void init(LinkedHashMap map) {
        // 读取配置文件，装配appender
        Class<? extends FilterByMsg> filterByMsgClass = this.getClass();
        Field[] fields = filterByMsgClass.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.set(this, map.get(field.getName()));
            } catch (IllegalAccessException e) {
                // ignore
            }
        }
        ruleStr = Arrays.asList(Optional.ofNullable(rule).orElse("").split("、"));
    }
}
