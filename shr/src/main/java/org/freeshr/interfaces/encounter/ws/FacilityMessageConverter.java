package org.freeshr.interfaces.encounter.ws;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.patient.Address;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FacilityMessageConverter  extends AbstractHttpMessageConverter<Facility> {

    public FacilityMessageConverter() {
        super(MediaType.APPLICATION_JSON);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return Facility.class.equals(clazz);
    }

    @Override
    protected Facility readInternal(Class<? extends Facility> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return createFacility(inputMessage);
    }

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        if(mediaType == null) return true;
            return supports(clazz) && canRead(mediaType);
    }
    @Override
    protected boolean canRead(MediaType mediaType) {
        return MediaType.APPLICATION_JSON.getType().equals(mediaType.getType()) && MediaType.APPLICATION_JSON.getSubtype().equals(mediaType.getSubtype());
    }


    Facility createFacility(HttpInputMessage inputMessage) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> facilityMapper = objectMapper.readValue(inputMessage.getBody(), Map.class);
        Facility facility = new Facility();
        facility.setFacilityId((String) facilityMapper.get("id"));
        facility.setFacilityName((String) facilityMapper.get("name"));
        facility.setFacilityType((String) ((LinkedHashMap) facilityMapper.get("properties")).get("org_type"));
        String catchments = Joiner.on(",").join((List<String>) ((LinkedHashMap) facilityMapper.get("properties")).get("catchment"));
        facility.setCatchments(catchments);
        facility.setFacilityLocation(getAddress(((LinkedHashMap) ((LinkedHashMap) facilityMapper.get("properties")).get("locations"))));
        return  facility;
    }

    private Address getAddress(LinkedHashMap linkedHashMap) {
        Address address = new Address();
        address.setDivision((String)linkedHashMap.get("division_code"));
        address.setDistrict((String)linkedHashMap.get("district_code"));
        address.setUpazilla((String)linkedHashMap.get("upazila_code"));
        address.setCityCorporation((String)linkedHashMap.get("paurasava_code"));
        address.setWard((String)linkedHashMap.get("ward_code"));
        return address;
    }


    @Override
    protected void writeInternal(Facility facility, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    }
}
