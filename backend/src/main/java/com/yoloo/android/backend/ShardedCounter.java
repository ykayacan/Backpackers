package com.yoloo.android.backend;

import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class ShardedCounter {

    /**
     * Default number of shards.
     */
    private static final int INITIAL_SHARDS = 5;

    /**
     * A random number generating, for distributing writes across shards.
     */
    private final Random generator = new Random();

    /**
     * A logger object.
     */
    private static final Logger LOG = Logger.getLogger(ShardedCounter.class
            .getName());

    public long getCount() {
        long sum = 0;

        List<LikeShard> shards = ofy().load().type(LikeShard.class).list();

        for (LikeShard shard : shards) {
            sum += shard.getCount();
        }

        return sum;
    }

    public void increment() {
        // Find how many shards are in this counter.
        int numShards = getShardCount();

        // Choose the shard randomly from the available shards.
        final int shardNum = generator.nextInt(numShards) + 1;

        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                LikeShard shard;
                long shardCount;
                try {
                    shard = ofy().load().type(LikeShard.class).id(shardNum).safe();
                    shardCount = shard.getCount() + 1;
                } catch (com.googlecode.objectify.NotFoundException e) {
                    shard = new LikeShard(shardNum);
                    shardCount = 1;
                }
                shard.setCount(shardCount);
                ofy().save().entity(shard).now();
            }
        });
    }

    private int getShardCount() {
        try {
            return getCounter("LikeCounterShard").getShardCount();
        } catch (com.googlecode.objectify.NotFoundException e) {
            return INITIAL_SHARDS;
        }
    }

    public void addShards(final int count) {
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                Counter counter;
                int value;
                try {
                    counter = getCounter("LikeCounterShard");
                    value = counter.getShardCount() + count;
                } catch (NotFoundException e) {
                    counter = new Counter("LikeCounterShard");
                    value = INITIAL_SHARDS + count;
                }
                counter.setShardCount(value);
                ofy().save().entity(counter).now();
            }
        });
    }

    private Counter getCounter(String name) {
        return ofy().load().type(Counter.class)
                .id(name).safe();
    }

    @Entity
    @Cache
    public static final class LikeShard {

        @Id
        private long shardId;

        private long count;

        private LikeShard() {
        }

        public LikeShard(long id) {
            this.shardId = id;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }

    @Entity
    @Cache(expirationSeconds = 60)
    public static final class Counter {

        @Id
        private String id;

        private int shardCount;

        private Counter() {
        }

        public Counter(String id) {
            this.id = id;
        }

        public int getShardCount() {
            return shardCount;
        }

        public void setShardCount(int shardCount) {
            this.shardCount = shardCount;
        }
    }
}
