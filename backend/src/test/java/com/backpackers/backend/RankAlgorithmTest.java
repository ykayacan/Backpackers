package com.backpackers.backend;

import com.backpackers.android.backend.algorithm.RankAlgorithm;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class RankAlgorithmTest {

    @Before
    public void setUp() throws Exception {

    }

    // score > 0
    @Test
    public void test1() throws Exception {
        double rank = RankAlgorithm.getHotRank(1, 0, new Date(Long.parseLong("1262304000000")));

        assertEquals(2850.0, rank, 0);
    }

    // score > 0
    @Test
    public void test2() throws Exception {
        double rank = RankAlgorithm.getHotRank(1000, 500, new Date(Long.parseLong("1262304000000")));

        assertEquals(2852.69897, rank, 0);
    }

    // score < 0
    @Test
    public void test3() throws Exception {
        double rank = RankAlgorithm.getHotRank(0, 1, new Date(Long.parseLong("1262304000000")));

        assertEquals(2850, rank, 0);
    }

    // score < 0
    @Test
    public void test4() throws Exception {
        double rank = RankAlgorithm.getHotRank(1000, 1500, new Date(Long.parseLong("1262304000000")));

        assertEquals(2847.30103, rank, 0);
    }

    // score = 0
    @Test
    public void test5() throws Exception {
        double rank = RankAlgorithm.getHotRank(1000, 1000, new Date(Long.parseLong("1262304000000")));

        assertEquals(2850.0, rank, 0);
    }

    @Test
    public void test6() throws Exception {
        double rank = RankAlgorithm.getHotRank(0, 120, new Date(Long.parseLong("1474410853000")));

        System.out.println("Rank: " + rank);
    }
}