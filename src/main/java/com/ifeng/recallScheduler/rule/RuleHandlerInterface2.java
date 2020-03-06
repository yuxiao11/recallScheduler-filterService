package com.ifeng.recallScheduler.rule;

/**
 * Created by lilg1 on 2017/10/12.
 */
public interface RuleHandlerInterface2<T,P,K> {

    boolean filter(T t, P p, K k);
}
