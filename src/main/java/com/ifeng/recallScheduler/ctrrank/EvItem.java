package com.ifeng.recallScheduler.ctrrank;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by lilg1 on 2018/3/16.
 */
@Getter
@Setter
public class EvItem {

    private long t;   //文章曝光时间

    private List<EvObj> ev;

    public class EvObj {

        public EvObj(String docId,String why, boolean c){
            this.i = docId;
            this.y = why;
            this.c = c;
        }

        public String getI() {
            return i;
        }

        public void setI(String i) {
            this.i = i;
        }

        public String getY() {
            return y;
        }

        public void setY(String y) {
            this.y = y;
        }

        public boolean getC(){
            return c;
        }

        public void setC(boolean c){
            this.c = c;
        }

        private String i;  //文章docId

        private String y;  //文章召回原因

        private boolean c; //文章是否点击
    }
}
