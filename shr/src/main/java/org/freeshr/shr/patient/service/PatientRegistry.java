package org.freeshr.shr.patient.service;

import com.google.common.util.concurrent.SettableFuture;
import org.freeshr.shr.concurrent.NotNull;
import org.freeshr.shr.concurrent.SimpleListenableFuture;
import org.freeshr.shr.patient.model.Patient;
import org.freeshr.shr.patient.repository.AllPatients;
import org.freeshr.shr.patient.wrapper.MasterClientIndexWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.ExecutionException;

@Service
public class PatientRegistry {

    private final AllPatients allPatients;
    private final MasterClientIndexWrapper masterClientIndexWrapper;

    @Autowired
    public PatientRegistry(AllPatients allPatients, MasterClientIndexWrapper masterClientIndexWrapper) {
        this.allPatients = allPatients;
        this.masterClientIndexWrapper = masterClientIndexWrapper;
    }

    public ListenableFuture<Boolean> isValid(final String healthId) {
        final SettableFuture<Boolean> future = SettableFuture.create();
        new NotNull<Patient>(allPatients.find(healthId)).addCallback(new ListenableFutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    future.set(Boolean.TRUE);
                } else {
                    masterClientIndexWrapper.isValid(healthId).addCallback(new ListenableFutureCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            future.set(result);
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            future.setException(t);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Throwable t) {
                future.setException(t);
            }
        });
        return new SimpleListenableFuture<Boolean, Boolean>(future) {
            @Override
            protected Boolean adapt(Boolean result) throws ExecutionException {
                return result;
            }
        };
    }
}
