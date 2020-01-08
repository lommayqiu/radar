package com.pgmmers.radar.util;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestRedisAndLua {

    private static Jedis jedis;        //单实例[]
    private static ShardedJedis shard;        //分片[]
    private static ShardedJedisPool pool;    //池化[apache common - pool2]


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        //单个节点
        jedis = new Jedis("f26q073786.wicp.vip", 6379);
        jedis.auth("root@chenying0224");
        //分片
        List<JedisShardInfo> shards = Arrays.asList(
                new JedisShardInfo("f26q073786.wicp.vip", 6379));

        shard = new ShardedJedis(shards);
        //池化
        GenericObjectPoolConfig goConfig = new GenericObjectPoolConfig();
        goConfig.setMaxTotal(100);
        goConfig.setMaxIdle(20);
        goConfig.setMaxWaitMillis(-1);
        goConfig.setTestOnBorrow(true);
        pool = new ShardedJedisPool(goConfig, shards);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        jedis.disconnect();
        shard.disconnect();
        pool.destroy();
    }


    /**
     * <B>方法名称：</B>Redis使用Lua执行应用程序<BR>
     * <B>概要说明：</B>
     * 我们可以使用Redis+Lua的方式，实现一个完整的事务、保证事务的原子性。
     * 如何使用Redis+Lua?
     * 我们使用scriptLoad方法，把我们写好的lua脚本加载到Redis的内存中（注意是内存，每次重启则失效）。
     * scriptLoad方法会返回一个索引Key，我们只需要通过这个索引Key就可以找到我们之前放到Redis里的脚本。
     * 调用evalsha方法，传入索引key，以及操作键、参数值。进行返回 <BR>
     */
    /**
     * lua script：
     * local t1 = redis.call('hgetall',KEYS[1]);
     * if type(t1) == 'table' then
     * return t1;
     * end;
     */
    public static final String SCRIPT =
            " redis.call(\"set\",KEYS[1],KEYS[1]) ;" +
                    "   if redis.call(\"EXISTS\",KEYS[1]) == 1 then\n" +
                    "     return redis.call(\"INCRBY\",KEYS[1],ARGV[1]) and 1 \n" +
                    "   else\n" +
                    "     return nil\n" +
                    "   end ;";
    public static final String SCRIPT2 =
            "local q = KEYS[1]   " +
                    "local q_set = KEYS[1]_set  " +
                    " local v = redis.call('SADD', q_set, ARGV[1]) " +
                    "  if v == 1  then " +
                    " return redis.call('RPUSH', q, ARGV[1]) and 1  " +
                    "  else return 0 end;";
    public static final String PUSHSCRIPT =
            " local q = KEYS[1]\n" +
                    " local q_set = KEYS[1] .. \"_set\"\n" +
                    " local v = redis.call(\"SADD\", q_set, ARGV[1])\n" +
                    " if v == 1\n" +
                    " then\n" +
                    " return redis.call('RPUSH', q, ARGV[1]) and 1\n" +
                    " else\n" +
                    "return 0\n" +
                    "end;";
    public static final String POPSCRIPT =
            " local q = KEYS[1]\n" +
                    " local q_set = KEYS[1] .. \"_set\"\n" +
                    " local v = redis.call(\"LPOP\", q)\n" +
                    "if v !=''  \n" +
                    " then\n" +
                    " redis.call(\"SREM\", q_set, v)\n" +
                    "  end\n" +
                    "return v";

    @Test
    public void testLua() {
//            String shakey = jedis.scriptLoad(SCRIPT);//加载脚本，获取sha索引
//            System.out.println("shakey: " + shakey);
//            //要获取的key值
//            List<String> keys = new ArrayList<>();
//            keys.add("myhash");
//            //传入的参数
//            List<String> args = new ArrayList<>();
//            // /usr/local/bin/redis-cli -h 192.168.1.115 -p 6379 --eval /usr/local/luadir/03.lua name age
//            List<String> ret = (List<String>)jedis.evalsha(shakey, keys, args);
//        System.out.println(jedis.scriptLoad("return 'hello moto'"));
//        System.out.println(jedis.scriptExists("232fd51614574cf0867b83d384a5e898cfd24e5a"));
//        System.out.println(new String(jedis.scriptFlush()));
//        System.out.println(jedis.scriptExists("232fd51614574cf0867b83d384a5e898cfd24e5a"));
//        System.out.println(jedis.ping());//pong则通过
//
//        String str1 = "return {KEYS[1], KEYS[2], ARGV[1], ARGV[2]}";
//        Object eval1 = jedis.eval(str1, 2, "k1", "k2", "a1", "a2");
//        System.out.println(eval1);
//        List a = (ArrayList) eval1;
//        System.out.println(a.get(1));

        //call
//        System.out.println(jedis.ping());
//        String str11 = "return redis.call('set', KEYS[1],'bar')";//设置键k1的值为bar
//        Object eval11 = jedis.eval(str11, 1,"k1");
//        System.out.println(eval11);
//        System.out.println(jedis.get("k1"));//查看执行情况
//
////pcall
//        System.out.println(jedis.ping());
//        String str12 = "return redis.pcall('set', KEYS[1],'bar')";//设置键k1的值为bar
//        Object eval12 = jedis.eval(str12, 1,"k1");
//        System.out.println(eval12);
//        System.out.println(jedis.get("k1"));//查看执行情况

        try {
            String lua = "local num = redis.call('incr', KEYS[1])\n" +
                    "if tonumber(num) == 1 then\n" +
                    " redis.call('expire', KEYS[1], ARGV[1])\n" +
                    " return 1\n" +
                    "elseif tonumber(num) > tonumber(ARGV[2]) then\n" +
                    " return 0\n" +
                    "else \n" +
                    " return 1\n" +
                    "end\n";
            Object result = jedis.evalsha(jedis.scriptLoad(lua), Arrays.asList("127.0.0.1"), Arrays.asList("10", "1"));
            System.out.println(result);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis != null){
                try {
                    jedis.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        jedis.close();
    }


}