package com.osp.issue.dubbo;

import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.osp.issue.dto.BaseAlarmDto;
import com.osp.issue.manage.AlarmManage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;

/**
 * dubbo统一异常捕获，发送邮件 <br>
 *
 * @author huangqiaowei
 * @since 2019-05-31 16:15
 **/
@Slf4j
public class AlarmExceptionFilter implements Filter {

    private AlarmManage alarmManage = AlarmManage.getInstance();

    /**
     * do invoke filter.
     * <p>
     * <code>
     * // before filter
     * Result result = invoker.invoke(invocation);alu
     * // after filter
     * return result;
     * </code>
     *
     * @param invoker    service
     * @param invocation invocation.
     * @return invoke result.
     * @see Invoker#invoke(Invocation)
     */
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        long start = System.currentTimeMillis();
        Result result = invoker.invoke(invocation);
        log.info("方法：{},方法参数：{},耗时：{}ms", invocation.getMethodName(), invocation.getArguments(), (System.currentTimeMillis() - start));

        if (Objects.nonNull(result.getException())) {
            try {
                alarmManage.notice(new BaseAlarmDto(result.getException(), null, JSON.json(invocation.getArguments()),
                        invocation.getClass().getCanonicalName() + "." + invocation.getMethodName()));
            } catch (IOException e) {
                log.warn("发送邮件失败", e);
            }
        }

        return result;
    }
}
