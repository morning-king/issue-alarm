package com.osp.issue.appender.impl;

import com.google.common.collect.Maps;
import com.osp.issue.appender.Appender;
import com.osp.issue.dto.BaseAlarmDto;
import com.osp.issue.util.ExchangeClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 报警发送
 *
 * @author huangqiaowei
 * @since 2019-05-29 09:55
 **/
@Slf4j
@Data
public class EmailAlarmAppender implements Appender {
    private static final String DEFAULT_HOST = "webmail.osp.com";
    private static List<String> recipientCcsStr;
    /**
     * 抄送人
     */
    private String recipientCcs;

    /**
     * 收件人
     */
    private String recipientTo;

    /**
     * 邮件模版
     */
    private String templateStr;

    /**
     * 邮件标题
     */
    private String subject;

    /**
     * 账号
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    private String bottom;

    /**
     * 环境 默认正式环境
     */
    private String env = "prod";

    /**
     * 单例：只能让实例一次
     */
    @Override
    public void init(LinkedHashMap map) {
        // 读取配置文件，装配appender
        Class<? extends EmailAlarmAppender> emailAlarmAppenderClass = this.getClass();
        Field[] fields = emailAlarmAppenderClass.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.set(this, map.get(field.getName()));
            } catch (IllegalAccessException e) {
                // ignore
            }
        }
        recipientCcsStr = Arrays.asList(recipientCcs.split("、"));
        String templateFileName = "Template.html";
        // 读取邮件模版文件
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(templateFileName);
        if (resourceAsStream == null) {
            throw new IllegalStateException("类路径加载模板文件失败：" + templateFileName);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
        try {
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            templateStr = builder.toString();
        } catch (IOException e) {
            throw new IllegalStateException("读取模板文件失败：" + templateFileName, e);
        }
    }

    /**
     * 附加目的地
     *
     * @param baseAlarmDto 报警信息
     */
    @Override
    public void append(BaseAlarmDto baseAlarmDto) {
        String message = makeupEmailContent(baseAlarmDto);
        ExchangeClient client = new ExchangeClient.ExchangeClientBuilder().hostname(DEFAULT_HOST)
                .exchangeVersion(ExchangeVersion.Exchange2010).username(username).password(password)
                .recipientTo(recipientTo).recipientCc(recipientCcsStr).subject(subject).message(message).build();
        client.sendExchange();
    }

    private String makeupEmailContent(BaseAlarmDto baseAlarmDto) {
        Map<String, String> map = getParamsMap(baseAlarmDto);
        // 获取文件字符串
        StringBuilder content = new StringBuilder();
        try {
            StringBuilder key = new StringBuilder();
            BufferedReader template = new BufferedReader(new StringReader(templateStr));
            boolean start = false;
            while (true) {
                int ch = template.read();
                if (ch == -1) {
                    template.close();
                    return content.toString();
                }
                if ('}' == ch) {
                    content.append(Optional.ofNullable(map.get(key.toString())).orElse(""));
                    start = false;
                    key = new StringBuilder();
                    continue;
                }
                if ('$' == ch) {
                    start = true;
                    continue;
                }
                if (start) {
                    if (' ' == ch) {
                        continue;
                    }
                    if ('{' != ch) {
                        key.append((char) ch);
                    }
                } else {
                    content.append((char) ch);
                }
            }
        } catch (Exception e) {
            log.warn("生成邮件正文失败：" + baseAlarmDto.toString(), e);
        }

        return content.toString();
    }

    /**
     * 发送邮件参数
     *
     * @param baseAlarmDto 上下文信息
     * @return 邮件内容参数map
     */
    private Map<String, String> getParamsMap(BaseAlarmDto baseAlarmDto) {
        Map<String, String> map = Maps.newHashMap();
        map.put("thread", Optional.ofNullable(baseAlarmDto.getThreadName()).orElse(""));
        map.put("env", env);
        map.put("operator", Optional.ofNullable(baseAlarmDto.getOperator()).orElse(""));
        map.put("bottom", Optional.ofNullable(bottom).orElse(""));
        map.put("time", baseAlarmDto.getExecutionTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        map.put("place", Optional.ofNullable(baseAlarmDto.getPlace()).orElse(""));
        map.put("errorMessage", Optional.ofNullable(baseAlarmDto.getErrorMessage()).orElse(""));
        map.put("ip", baseAlarmDto.getIp());
        if (Objects.nonNull(baseAlarmDto.getCause())) {
            // 堆栈信息特殊处理
            StringBuffer stackStr = new StringBuffer();
            if (baseAlarmDto.getCause() != null) {
                try (StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter)) {
                    baseAlarmDto.getCause().printStackTrace(printWriter);
                    // 逐行读取，更换颜色和空格
                    BufferedReader bf = new BufferedReader(new StringReader(stringWriter.toString()));
                    bf.lines().forEach(line -> {
                        boolean contaisosp = line.contains("osp");
                        stackStr.append("&nbsp;&nbsp;&nbsp;&nbsp;")
                            .append(contaisosp ? "<span style=\"color:#c0f\">" : "").append(line)
                            .append(contaisosp ? "</span>" : "").append("<br>");
                    });
                    bf.close();
                } catch (Exception e) {
                    log.error("堆栈轨迹获取失败", e);
                }
            }
            map.put("stash", stackStr.toString());
        }
        map.put("extra", Optional.ofNullable(baseAlarmDto.getExtra()).orElse(""));
        return map;
    }
}
