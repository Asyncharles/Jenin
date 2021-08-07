public class Skill {
    private String skillName;
    private double level;
    private String description;

    public Skill(String skillName, double level, String description) {
        this.skillName = skillName;
        this.level = level;
        this.description = description;
    }

    public String getSkillName() {
        return skillName;
    }

    public double getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }
}
