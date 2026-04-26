package com.pajamas.learning.utils;

import lombok.Data;

import java.time.Duration;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@Data
public  class DelayTask<D> implements Delayed {
    //延时时间
    private long deadlineNanos;
    //任务数据
    private D data;

    public DelayTask(D data, Duration delaytime) {
        this.data = data;
        this.deadlineNanos = System.nanoTime() + delaytime.toNanos();
    }

    //剩余延时时间
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(Math.max(0,deadlineNanos - System.nanoTime()), TimeUnit.NANOSECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        long l = getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
        if (l > 0){
            return 1;
        } else if(l < 0){
            return -1;
        } else{
            return 0;
        }
    }
}
