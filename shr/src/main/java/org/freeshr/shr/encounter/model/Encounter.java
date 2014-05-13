package org.freeshr.shr.encounter.model;

public class Encounter {

    private String healthId;

    public String getHealthId() {
        return healthId;
    }

    public void setHealthId(String healthId) {
        this.healthId = healthId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Encounter encounter = (Encounter) o;

        if (!healthId.equals(encounter.healthId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return healthId.hashCode();
    }
}
