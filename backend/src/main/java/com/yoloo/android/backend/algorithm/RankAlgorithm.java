package com.yoloo.android.backend.algorithm;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class RankAlgorithm {

    public static RankAlgorithm newInstance() {
        return new RankAlgorithm();
    }

    private long timeDiffInSeconds(Date createdAt) {
        TimeUnit.MILLISECONDS.toSeconds(createdAt.getTime());
        long createdAtSec = createdAt.getTime() / 1000;
        long currentSec = System.currentTimeMillis() / 1000;
        return currentSec - createdAtSec;
    }

    private long score(long ups, long downs) {
        return ups - downs;
    }

    private byte sign(long score) {
        if (score > 0) {
            return 1;
        } else if (score < 0) {
            return -1;
        } else {
            return 0;
        }
    }

    private long modifiedSeconds(long createdAt, long timeModifier) {
        return TimeUnit.MILLISECONDS.toSeconds(createdAt + timeModifier);
    }

    public double hot(long ups, long downs, Date createdAt, long timeModifierInMillis) {
        long score = score(ups, downs);

        double order = Math.log10(Math.max(Math.abs(score), 1));

        byte sign = sign(score);

        long seconds = (createdAt.getTime() - 1134028003);

        //return sign * order * 45000 + seconds;
        return (sign * order + seconds) / 45000;
    }
}
