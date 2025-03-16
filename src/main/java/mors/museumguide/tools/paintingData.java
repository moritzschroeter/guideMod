package mors.museumguide.tools;

import java.util.List;

public class paintingData {

    public String id;

    public String name;
    public String description;
    public List<image> paintings;

    public static class image {
        public String id;
        public String name;
        public String artist;
        public String description;
        public String width; // Change to String as in JSON it's quoted
        public String height; // Change to String as in JSON it's quoted
    }
}
