package com.yoloo.backend;

import com.yoloo.android.backend.algorithm.RankAlgorithm;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RankAlgorithmTest {

    private RankAlgorithm a1;
    private RankAlgorithm a2;

    @Before
    public void setUp() throws Exception {
        a1 = RankAlgorithm.newInstance();
        a2 = RankAlgorithm.newInstance();
    }

    @Test
    public void testIfAwardedPostHasHigherRank() throws Exception {
        double rank1 = a1.hot(1, 0, new Date(1262304000), 0);
        double rank2 = a2.hot(1000, 500, new Date(1262304000), 0);

        System.out.println(rank1);
        System.out.println(rank2);

        assertTrue(rank1 > rank2);
    }

    @Test
    public void testIfHotRankIsSame() throws Exception {
        double rank1 = a1.hot(100, 40, new Date(1471045376), 0);
        double rank2 = a2.hot(100, 40, new Date(1471045376), 0);

        assertEquals(rank1, rank2, 0);
    }
}