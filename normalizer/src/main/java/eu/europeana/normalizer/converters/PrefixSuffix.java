package eu.europeana.normalizer.converters;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */


public class PrefixSuffix implements Converter {
    private String prefix, suffix;

    public PrefixSuffix(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public String convertValue(String value) {
        return prefix+value+suffix;
    }
}