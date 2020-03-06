package com.ifeng.recallScheduler.timer;


import com.ifeng.recallScheduler.constant.GyConstant;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;


public class TimerEntity {

    //计时器名字，时间实体
    private Map<String, TimeBean> staticsTimePool;

    public TimerEntity() {
        staticsTimePool = new LinkedHashMap<String, TimeBean>();
    }

    public boolean addTime(String TimePosName, long startTime, long endTime) {
        if (null != TimePosName && !TimePosName.isEmpty()) {
            staticsTimePool.put(TimePosName, new TimeBean(startTime, endTime));
            return true;
        }
        return false;
    }

    public boolean addStartTime(String TimePosName) {
        if (null != TimePosName && !TimePosName.isEmpty()) {
            TimeBean temp = new TimeBean();
            temp.setStartTime(System.currentTimeMillis());
            staticsTimePool.put(TimePosName, temp);
            return true;
        }
        return false;
    }

    public boolean addEndTime(String TimePosName) {
        if (null != TimePosName && !TimePosName.isEmpty()) {
            TimeBean temp = staticsTimePool.get(TimePosName);
            if (null != temp && temp.getStartTime() != 0) {
                temp.setEndTime(System.currentTimeMillis());
                staticsTimePool.put(TimePosName, temp);
                return true;
            }
        }
        return false;
    }

    /**
     * 获取单个区块的耗时信息
     * @param TimePosName
     * @return
     */
    public TimeBean getTimeBean(String TimePosName) {
        if (null != TimePosName && !TimePosName.isEmpty()) {
            TimeBean temp = staticsTimePool.get(TimePosName);
            return temp;
        }
        return null;
    }

    /**
     * 获取总的统计信息
     * @return
     */
    public String getStaticsInfo() {
        StringBuilder sb = new StringBuilder();
        if (staticsTimePool.isEmpty()) {
            return "staticsTimePool is empty!";
        }
        sb.append("Timeout :");

        long usedtime = 0l;
        //矫正timeout 多输出 ，
        boolean first = true;

        for (Entry<String, TimeBean> entry : staticsTimePool.entrySet()) {
            if(first){
                first = false;
                usedtime = entry.getValue().getUsedTime();
                sb.append(GyConstant.Symb_blank);
                sb.append(entry.getKey());
                sb.append(GyConstant.Symb_Colon);
                sb.append(usedtime);
            }else {
                sb.append(GyConstant.Symb_Comma);
                usedtime = entry.getValue().getUsedTime();
                sb.append(GyConstant.Symb_blank);
                sb.append(entry.getKey());
                sb.append(GyConstant.Symb_Colon);
                sb.append(usedtime);
            }

        }
        sb.append(GyConstant.Symb_Comma);
        sb.append(GyConstant.Symb_blank);
        sb.append("IP");
        sb.append(GyConstant.Symb_Colon);
        sb.append(GyConstant.linuxLocalIp);
        return sb.toString();
    }

    public class TimeBean {
        private long startTime;

        private long endTime;

        public TimeBean() {
            startTime = 0l;
            endTime = -1l;
        }

        public TimeBean(long startTime, long endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }


        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        //返回-1 时间点设置失败
        public long getUsedTime() {
            return this.endTime - this.startTime;
        }


    }



    public static void main(String[] args) {
        TimerEntity instance = TimerEntityUtil.getInstance();
        instance.addStartTime("a");
        instance.addEndTime("a");
        System.out.println(instance.getStaticsInfo());
    }
}
