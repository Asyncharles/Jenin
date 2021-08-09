import net.charles.Jenin;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Debug {
    public static void main(String[] args) throws IllegalAccessException {

        //Configuration of the Jedis Pool
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(1024);
        jedisPoolConfig.setMaxWaitMillis(5000);

        /**
         * Building the {@link Jenin} object
         */
        Jenin jenin = new Jenin(new JedisPool(jedisPoolConfig, "host", 6969, 1000, "pwd"));

        /**
         * Building a random ( {@link Player} in this case )
         * The {@link net.charles.annotations.DataKey} is {@link Player#name}
         */
        Player player = new Player("Charles", 18, true, new Skill("Procrastination", 10000, "bad"));

        /**
         * Pushing it into redis
         * The object will be serialized into a json string and stored as a redis string
         */
        jenin.push(player);

        /**
         * Fetching the data into redis using the {@link net.charles.annotations.DataKey}
         */
        Player p2 = jenin.compactSearch("Charles", Player.class);

        /**
         * Fetching the {@link Player#age} value of the data
         */
        int age = Integer.parseInt(jenin.compactSearch("Charles", "age", Player.class));
    }
}
