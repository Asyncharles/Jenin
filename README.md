## Jenin

Redis object mapper for Java

Still under heavy development / no official release for now. The first release of this project is planned for the upcoming weeks. 

Contact finalvoiddev@gmail.com for any requests/issues.

## Jenin Mapper

To see the different functions available, head to [Jenin Mapper](https://github.com/FinalVoid/Jenin/blob/master/src/main/java/net/charles/mapper/JeninMapper.java)
You can build a Jenin object with the constructor

```java
Jenin jenin = new Jenin(new JedisPool(jedisPoolConfig, "host", 6969, 1000, "pwd"));
```
Head to the [examples](https://github.com/FinalVoid/Jenin/tree/master/src/test/java) for further detailing. 

## Mapping objects using bytes

I would not recommend using the byte mapper yet as it is still under heavy development and does not support most of the serialization/deserialization features.
Byte mapper available [here](https://github.com/Asyncharles/Jenin/blob/object_stream/src/main/java/net/charles/mapper/JeninMapper.java) 
