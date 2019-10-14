package com.osp.issue.dto;

import lombok.Data;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

/**
 * 基础邮件通知类
 *
 * @author huangqiaowei
 * @since 2019-05-28 17:33
 **/
@Data
public class BaseAlarmDto {

    private static final String LOCALMACHINEIP;

    static {
        InetAddress ia = null;

        try {
            ia = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            // ignore
        }

        if (ia != null) {
            LOCALMACHINEIP = ia.getHostAddress();
        } else {
            LOCALMACHINEIP = "127.0.0.1";
        }

    }

    /**
     * 线程名
     */
    private final String threadName = Thread.currentThread().getName();

    /**
     * 发生时间
     */
    private LocalDateTime executionTime = LocalDateTime.now();

    /**
     * 地点
     */
    private String place;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 当前操作人
     */
    private String operator;

    /**
     * 错误堆栈
     */
    private Throwable cause;

    /**
     * 额外信息
     */
    private String extra;

    private String ip = LOCALMACHINEIP;

    public BaseAlarmDto() {
    }

    public BaseAlarmDto(Throwable throwable) {
        this.cause = throwable;
        this.errorMessage = this.cause.getMessage();
    }

    public BaseAlarmDto(Throwable throwable, String operator) {
        this.cause = throwable;
        this.errorMessage = this.cause.getMessage();
        this.operator = operator;
    }

    public BaseAlarmDto(Throwable throwable, String operator, String extra, String place) {
        this.cause = throwable;
        this.operator = operator;
        this.errorMessage = this.cause.getMessage();
        this.extra = extra;
        this.place = place;
    }

}
