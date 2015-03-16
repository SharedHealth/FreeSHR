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
        Confidentiality confidentiality = Confidentiality.Normal;
        if (code == null) {
            return confidentiality;
        }
        switch (code) {
            case "U":
                confidentiality = Confidentiality.Unrestricted;
                break;
            case "L":
                confidentiality = Confidentiality.Low;
                break;
            case "M":
                confidentiality = Confidentiality.Moderate;
                break;
            case "N":
                confidentiality = Confidentiality.Normal;
                break;
            case "R":
                confidentiality = Confidentiality.Restricted;
                break;
            case "V":
                confidentiality = Confidentiality.VeryRestricted;
        }
        return confidentiality;
    }
}

