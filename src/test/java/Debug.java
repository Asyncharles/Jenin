import net.charles.Jenin;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Debug {
    public static void main(String[] args) throws IllegalAccessException, InstantiationException {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(1024);
        jedisPoolConfig.setMaxWaitMillis(5000);
        Jenin jenin = new Jenin(new JedisPool(jedisPoolConfig, "redis-10357.c135.eu-central-1-1.ec2.cloud.redislabs.com", 10357, 1000, "7iqJS8BscB5BXLNb1Gyk79aTqe1ZvwTT"));

        Player player = new Player("Charles", 18, true, new Skill("Procrastination", 10000, "bad"));
        jenin.compactPush(player);
    }
}
