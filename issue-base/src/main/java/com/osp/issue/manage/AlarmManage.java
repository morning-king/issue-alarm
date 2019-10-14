package com.osp.issue.manage;

import com.google.common.collect.Lists;
import com.osp.issue.appender.Appender;
import com.osp.issue.dto.BaseAlarmDto;
import com.osp.issue.filter.Filter;
import com.osp.issue.util.AlarmConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;

import javax.annotation.PreDestroy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author huangqiaowei
 * @since 2019-05-29 14:59
 **/
@Slf4j
@SuppressWarnings("unchecked")
public class AlarmManage {

    private static volatile AlarmManage alarmManage;

    private final long WAIT_TIME = 100;
    private final TimeUnit WAIT_TIME_TIME_UNIT = TimeUnit.MILLISECONDS;
    private final BlockingQueue<BaseAlarmDto> queue = new ArrayBlockingQueue<>(1000);
    private volatile boolean isRunning = true;
    public static final List<Appender> APPENDERS = Lists.newArrayList();
    public static final List<Filter> FILTERS = Lists.newArrayList();
    private static final String DEFAULT_APPENDER_PATH = "com.osp.issue.appender.impl.";
    private static final String DEFAULT_FILTERS_PATH = "com.osp.issue.filter.impl.";
    private static final String PATH = "path";

    // 读取配置文件，加载alar工程
    private void init() {
        AlarmConfiguration alarmConfiguration = new AlarmConfiguration();
        LinkedHashMap appendersMap = alarmConfiguration.getAppenders();
        if (MapUtils.isNotEmpty(appendersMap)) {
            appendersMap.forEach((k, v) -> set(k, v, DEFAULT_APPENDER_PATH));
        }
        LinkedHashMap filters = alarmConfiguration.getFilters();
        if (MapUtils.isNotEmpty(filters)) {
            filters.forEach((k, v) -> set(k, v, DEFAULT_FILTERS_PATH));
        }
    }

    private static void set(Object k, Object v, String path) {
        Class tempClass = null;
        // 根据key值加载类
        try {
            // 第一步: 本jar包下面所有appender看是否满足
            tempClass = Class.forName(path + k.toString());
        } catch (ClassNotFoundException e) {
            // 第二步: 看自定义的appdender里面是否有满足的
            Object packpackPath = ((LinkedHashMap) v).get(PATH);
            try {
                tempClass = Class.forName(packpackPath.toString() + k.toString());
            } catch (ClassNotFoundException e1) {
                log.error("配置错误，详情请阅读README.md文档");
            }
        }
        if (Objects.nonNull(tempClass)) {
            try {
                Object instance = tempClass.newInstance();
                // 初始化类
                Method init = tempClass.getMethod("init", v.getClass());
                init.invoke(instance, v);
                if (instance instanceof Appender) {
                    APPENDERS.add((Appender) instance);
                } else if (instance instanceof Filter) {
                    FILTERS.add((Filter) instance);
                } else {
                    log.error("自定义需要继承相应的接口");
                }
            } catch (NoSuchMethodException e) {
                log.error("没有init | validate 方法");
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                // ignore
            }
        }
    }

    private AlarmManage() {
        init();
        startListen();
    }

    /**
     * 单例：dubbo自定义单filter不能使用spring容器
     * 
     * @return AlarmManage
     */
    public static AlarmManage getInstance() {
        if (alarmManage == null) {
            synchronized (AlarmManage.class) {
                if (alarmManage == null) {
                    alarmManage = new AlarmManage();
                }
            }
        }
        return alarmManage;
    }

    /**
     * 初始化处理器
     */
    private void startListen() {
        Thread thread = new Thread(() -> {
            while (isRunning) {
                try {
                    BaseAlarmDto element = queue.poll(WAIT_TIME, WAIT_TIME_TIME_UNIT);

                    if (element != null) {
                        List<BaseAlarmDto> executionContexts = Lists.newArrayList(element);
                        queue.drainTo(executionContexts);
                        executionContexts.forEach(context -> {
                            // 过滤
                            boolean isNotAppenders;
                            isNotAppenders = FILTERS.stream().map(filter -> filter.filter(context))
                                .filter(temp -> temp).findFirst().orElse(false);
                            if (!isNotAppenders) {
                                // 处理任务
                                APPENDERS.forEach(appender -> appender.append(context));
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    log.warn("线程被打断，停止分发警告", e);
                    isRunning = false;
                }
            }
        }, "NOTICE-DISPATCHER");
        thread.setUncaughtExceptionHandler((t, e) -> log.error("线程" + t.getName() + "已挂掉", e));
        thread.start();
    }

    /**
     * 处理任务
     * 
     * @param context 报警信息
     */
    public void notice(BaseAlarmDto context) {
        boolean succeed = false;
        try {
            succeed = queue.offer(context, WAIT_TIME, WAIT_TIME_TIME_UNIT);
        } catch (InterruptedException e) {
            // ignore
        }
        if (!succeed) {
            log.warn("通知问题失败：" + context, context.getCause());
        }
    }

    /**
     * 释放资源
     */
    @PreDestroy
    private void destroy() {
        queue.clear();
        isRunning = false;
    }
}
