package com.ifeng.recallScheduler.rule;

/**
 * Created by wupeng1 on 2017/6/30.
 * 规则过滤器，用于链式过滤
 */
public interface RuleHandlerInterface1<T,P> {

    T filter(T t, P p);
}
