package com.k09.opendatachallenge.data;

import java.util.ArrayList;

public class Railway implements OdptData {
    public class RailwayTitle {
        public String ja;
        public String en;
    }
    public static class StationOrder {
        public class StationTitle {
            public String ja;
            public String en;
        }

        public int index;
        public String station;
        public StationTitle stationTitle = new StationTitle();
    }
    public String date;
    public String sameAs;
    public String operator;
    public String title;
    public RailwayTitle railwayTitle = new RailwayTitle();
    public StationOrder[] stationOrder;

    @Override
    public String getDataType() {
        return "Railway";
    }
}
