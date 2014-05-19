package org.freeshr.shr.util;

public class ResultHolder {

    private Boolean called;

    public ResultHolder(Boolean called) {
        this.called = called;
    }

    public Boolean getCalled() {
        return called;
    }

    public void setCalled(Boolean called) {
        this.called = called;
    }
}
