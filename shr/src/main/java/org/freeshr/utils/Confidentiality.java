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

    public static Confidentiality getConfidentiality(String code) {
        Confidentiality confidentiality;
        if (code.equals(Confidentiality.Unrestricted.getLevel())) {
            confidentiality = Confidentiality.Unrestricted;
        } else if (code.equals(Confidentiality.Low.getLevel())) {
            confidentiality = Confidentiality.Low;
        } else if (code.equals(Confidentiality.Moderate.getLevel())) {
            confidentiality = Confidentiality.Moderate;
        } else if (code.equals(Confidentiality.Restricted.getLevel())) {
            confidentiality = Confidentiality.Restricted;
        } else if (code.equals(Confidentiality.VeryRestricted.getLevel())) {
            confidentiality = Confidentiality.VeryRestricted;
        } else {
            confidentiality = Confidentiality.Normal;
        }
        return confidentiality;
    }
}

