package org.freeshr.utils;

public enum Confidentiality {
    Unrestricted, Low, Moderate, Normal, Restricted, VeryRestricted;

    public String getLevel() {
        switch (this) {
            case Unrestricted:
                return "U";
            case Low:
                return "L";
            case Moderate:
                return "M";
            case Normal:
                return "N";
            case Restricted:
                return "R";
            case VeryRestricted:
                return "V";
            default:
                return null;
        }
    }
}

