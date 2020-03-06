package com.ifeng.recallScheduler.timer;

/**
 * TimerEntity辅助类
 * 
 */
public class TimerEntityUtil {
	
	private static ThreadLocal<TimerEntity> instance = new ThreadLocal<TimerEntity>(){
	    public TimerEntity initialValue(){
	        return new TimerEntity();
	    }
	};
	
	public static TimerEntity getInstance(){
		return instance.get();
	}
	
	public static void remove(){
		instance.remove();
	}

}
