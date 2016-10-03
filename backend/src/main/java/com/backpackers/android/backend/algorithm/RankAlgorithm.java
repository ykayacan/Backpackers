package com.backpackers.android.backend.algorithm;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;

public class RankAlgorithm {

    private static final DecimalFormat FORMAT = new DecimalFormat("####.#######",
            DecimalFormatSymbols.getInstance(Locale.US));

    public static RankAlgorithm newInstance() {
        return new RankAlgorithm();
    }

    private static double round(double value) {
        return Double.parseDouble(FORMAT.format(value));
    }

    private static long score(long ups, long downs) {
        return ups - downs;
    }

    private static byte sign(long score) {
        if (score > 0) {
            return 1;
        } else if (score < 0) {
            return -1;
        } else {
            return 0;
        }
    }

    public static double getHotRank(long ups, long downs, Date createdAt) {
        long score = score(ups, downs);

        double order = Math.log10(Math.max(Math.abs(score), 1));

        byte sign = sign(score);

        long seconds = (createdAt.getTime() / 1000) - 1134028003;

        double rank = sign * order + seconds / 45000;

        return round(rank);
    }
}
