package Objects;

public class BinMapping {
    private final String name;
    private final Long rangeFrom;
    private final Long rangeTo;
    private final String type;
    private final String country;

    public BinMapping(String name, Long rangeFrom, Long rangeTo, String type, String country) {
        this.name = name;
        this.rangeFrom = rangeFrom;
        this.rangeTo = rangeTo;
        this.type = type;
        this.country = country;
    }

    public String getName() {
        return name;
    }

    public Long getRangeFrom() {
        return rangeFrom;
    }

    public Long getRangeTo() {
        return rangeTo;
    }

    public String getType() {
        return type;
    }

    public String getCountry() {
        return country;
    }
}
