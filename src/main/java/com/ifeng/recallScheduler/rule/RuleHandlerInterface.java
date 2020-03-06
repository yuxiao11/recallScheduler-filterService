package com.ifeng.recallScheduler.rule;

/**
 * Created by wupeng1 on 2017/5/27.
 * 规则处理器接口-用于集合文档对象过滤
 */
@FunctionalInterface
public interface RuleHandlerInterface<T,P> {

    boolean filter(T t, P p);

}
