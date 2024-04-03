class BinMapping {
    private final String name;
    private final String rangeFrom;
    private final String rangeTo;
    private final String type;
    private final String country;

    public BinMapping(String name, String rangeFrom, String rangeTo, String type, String country) {
        this.name = name;
        this.rangeFrom = rangeFrom;
        this.rangeTo = rangeTo;
        this.type = type;
        this.country = country;
    }

    public String getName() {
        return name;
    }

    public String getRangeFrom() {
        return rangeFrom;
    }

    public String getRangeTo() {
        return rangeTo;
    }

    public String getType() {
        return type;
    }

    public String getCountry() {
        return country;
    }
}
