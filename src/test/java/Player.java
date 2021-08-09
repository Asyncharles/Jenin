
import net.charles.annotations.DataKey;
import net.charles.annotations.Exclude;

public class Player {
    @DataKey(include = false)
    private String name;
    public int age;
    public boolean dev;
    private Skill skill;

    public Player(String name, int age, boolean dev, Skill skill) {
        this.name = name;
        this.age = age;
        this.dev = dev;
        this.skill = skill;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public boolean isDev() {
        return dev;
    }

    public Skill getSkill() {
        return skill;
    }
}
