package com.kfuntak.gwt.json.serialization.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.kfuntak.gwt.json.serialization.client.domain.Address;
import com.kfuntak.gwt.json.serialization.client.domain.AddressType;
import com.kfuntak.gwt.json.serialization.client.domain.Contact;
import com.kfuntak.gwt.json.serialization.client.domain.PhoneNumber;
import com.kfuntak.gwt.json.serialization.client.domain.School;
import com.kfuntak.gwt.json.serialization.client.domain.Student;
import com.kfuntak.gwt.json.serialization.client.domain.University;

public class SerializerTest extends GWTTestCase {
    @Override
	public String getModuleName() {
        return "com.kfuntak.gwt.json.serialization.GWTProJsonSerializer";
    }

    PhoneNumber createTestPhone() {
        final PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber("123-123-1234");
        phoneNumber.setExt("123");
        phoneNumber.setListedStatus("New");
        phoneNumber.setType("Home");
        return phoneNumber;
    }

    String createTestPhoneJson() {
        final String json = "{\"number\":\"123-123-1234\", \"ext\":\"123\", \"type\":\"Home\", \"listedStatus\":\"New\", \"class\":\"com.kfuntak.gwt.json.serialization.client.domain.PhoneNumber\"}";
        return json;
    }

    @Test
    public void testTypeSerialization() {
        final Student s = new Student();
        s.school = new University();
        s.contact = new Contact();
        final Serializer serializer = (Serializer) GWT.create(Serializer.class);
        final String ser = serializer.serialize(s);
        System.out.println(ser);
        final Student s2 = (Student) serializer.deSerialize(ser);
        assertEquals(s.school, s2.school);
    }

    @Test
    public void testSerializeArbirtraryArrayList() {
        final Serializer serializer = (Serializer) GWT.create(Serializer.class);
        final ArrayList<String> list = new ArrayList<String>();
        list.add("Heath");
        list.add("Pax");
        list.add("Soren");
        list.add("Gage");
        final String names = serializer.serialize(list);
        assertEquals("[\"Heath\",\"Pax\",\"Soren\",\"Gage\"]",names);
    }

    @Test
    public void testSerializeArbitraryHashMap() {
        final Serializer serializer = (Serializer) GWT.create(Serializer.class);
        final HashMap<String, String> map = new HashMap<String, String>();
        map.put("name", "Heath");
        map.put("age", "36");
        map.put("city", "Temple");
        final String info = serializer.serialize(map);
        assertEquals("{\"age\":\"36\", \"name\":\"Heath\", \"city\":\"Temple\"}",info);
    }

    @Test
    public void testMarshallHelperSerialize()
    {
        final HashMap<String, String> map = new HashMap<String, String>();
        map.put("name", "Heath");
        map.put("age", "36");
        map.put("city", "Temple");
        final String info = Serializer.marshall(map);
        assertEquals("{\"age\":\"36\", \"name\":\"Heath\", \"city\":\"Temple\"}",info);
    }

    @Test
    public void testMarshallHelperDeserialize() {
        final String listJson = "[\"Heath\",\"Pax\",\"Soren\",\"Gage\"]";
        final ArrayList<String> elist = new ArrayList<String>();
        elist.add("Heath");
        elist.add("Pax");
        elist.add("Soren");
        elist.add("Gage");
        assertEquals(elist,Serializer.<ArrayList<String>>marshall(listJson,elist.getClass().getName()));
    }

    @Test
    public void testDeserializeArbitraryArrayList() {
        final Serializer serializer = (Serializer) GWT.create(Serializer.class);
        final String listJson = "[\"Heath\",\"Pax\",\"Soren\",\"Gage\"]";
        final ArrayList<?> list = (ArrayList<?>) serializer.deSerialize(listJson, "java.util.ArrayList");
        final ArrayList<String> elist = new ArrayList<String>();
        elist.add("Heath");
        elist.add("Pax");
        elist.add("Soren");
        elist.add("Gage");
        assertEquals(elist,list);
    }

    @Test
    public void testDeserializeArbitraryHashMap() {
        final Serializer serializer = (Serializer) GWT.create(Serializer.class);
        final String mapJson = "{\"age\":\"36\", \"name\":\"Heath\", \"city\":\"Temple\"}";
        final HashMap<?, ?> map = (HashMap<?, ?>) serializer.deSerialize(mapJson, "java.util.HashMap");
        final HashMap<String, String> emap = new HashMap<String, String>();
        emap.put("name", "Heath");
        emap.put("age", "36");
        emap.put("city", "Temple");
        assertEquals(emap,map);
    }

    @Test
    public void testEnum() {
        final Serializer serializer = (Serializer) GWT.create(Serializer.class);
        final Address address = new Address();
        address.setType(AddressType.WORK);
        final String json = serializer.serialize(address);
        final Address newAddress = (Address)serializer.deSerialize(json);
        assertEquals(AddressType.WORK,newAddress.getType());
    }

    @Test
    public void testSimpleSerialization() {
        final Serializer serializer = (Serializer) GWT.create(Serializer.class);
        final PhoneNumber phoneNumber = createTestPhone();
        final String phoneJson = serializer.serialize(phoneNumber);
        final String referenceJson = createTestPhoneJson();
        assertEquals(referenceJson, phoneJson);
    }

    @Test
    public void testSimpleDeserialization() {
        final Serializer serializer = (Serializer) GWT.create(Serializer.class);
        final PhoneNumber referencePhone = createTestPhone();
        final PhoneNumber phoneNumber = (PhoneNumber)serializer.deSerialize(createTestPhoneJson(), "com.kfuntak.gwt.json.serialization.client.domain.PhoneNumber");
        assertEquals(referencePhone.toString(), phoneNumber.toString());
    }

    @Test
    public void testCollectionSerialization() {
        final Serializer serializer = (Serializer) GWT.create(Serializer.class);
        final School school = new School();
        school.setGradeLevels(Arrays.asList("1","2","3"));
        final String schoolJson = serializer.serialize(school);
        final String referenceJson = "{\"refIdKey\":null, \"refId\":null, \"schoolName\":null, \"schoolShortName\":null, \"schoolUrl\":null, \"status\":0, \"gradeLevels\":[\"1\",\"2\",\"3\"], \"contactInfo\":null, \"startDate\":null, \"class\":\"com.kfuntak.gwt.json.serialization.client.domain.School\"}";
        assertEquals(referenceJson,schoolJson);
    }

    @Test
    public void testCollectionDeserialization() {
        final Serializer serializer = (Serializer) GWT.create(Serializer.class);
        final String schoolJson = "{\"refIdKey\":null, \"refId\":null, \"schoolName\":null, \"schoolShortName\":null, \"schoolUrl\":null, \"status\":0, \"gradeLevels\":[\"1\",\"2\",\"3\"], \"startDate\":null, \"class\":\"com.kfuntak.gwt.json.serialization.client.domain.School\"}";
        final School school = (School) serializer.deSerialize(schoolJson, "com.kfuntak.gwt.json.serialization.client.domain.School");
        assertEquals(Arrays.asList("1", "2", "3"), school.getGradeLevels());
    }

    @Test
    public void testHashMapSerialization() {
        final Serializer serializer = (Serializer) GWT.create(Serializer.class);
        final Address address = new Address();
        final HashMap<String, String> phoneMap = new HashMap<String, String>();
        phoneMap.put("Home", "123-123-1234");
        phoneMap.put("Work", "521-521-5231");
        address.setPhoneNumbers(phoneMap);
        final String addressJson = serializer.serialize(address);
        final String referenceJson = "{\"line1\":null, \"line2\":null, \"city\":null, \"state\":null, \"country\":null, \"zipCode\":null, \"phoneNumbers\":{\"Home\":\"123-123-1234\", \"Work\":\"521-521-5231\"}, \"type\":\"HOME\", \"class\":\"com.kfuntak.gwt.json.serialization.client.domain.Address\"}";
        assertEquals(referenceJson,addressJson);
    }

    @Test
    public void testHashMapDeserialization() {
        final Serializer serializer = (Serializer) GWT.create(Serializer.class);
        final HashMap<String, String> phoneMap = new HashMap<String, String>();
        phoneMap.put("Home", "123-123-1234");
        phoneMap.put("Work", "521-521-5231");
        final String json = "{\"line1\":null, \"line2\":null, \"city\":null, \"state\":null, \"country\":null, \"zipCode\":null, \"phoneNumbers\":{\"Home\":\"123-123-1234\", \"Work\":\"521-521-5231\"}, \"type\":\"HOME\", \"class\":\"com.kfuntak.gwt.json.serialization.client.domain.Address\"}";
        final Address address = (Address) serializer.deSerialize(json, "com.kfuntak.gwt.json.serialization.client.domain.Address");
        assertEquals(phoneMap,address.getPhoneNumbers());
    }

    @Test
    public void testNestedHashMapDeSerialization() {
        final Serializer serializer = (Serializer) GWT.create(Serializer.class);
        final HashMap<String, HashMap<String, Contact>> familyMap = new HashMap<String, HashMap<String, Contact>>();
        HashMap<String, Contact> familyBranchMap = new HashMap<String, Contact>();
        familyBranchMap.put("uncle", new Contact("Bill"));
        familyBranchMap.put("aunt", new Contact("Jenny"));
        familyMap.put("maternal",familyBranchMap);
        familyBranchMap = new HashMap<String, Contact>();
        familyBranchMap.put("uncle", new Contact("John"));
        familyBranchMap.put("aunt", new Contact("Ruth"));
        familyMap.put("paternal", familyBranchMap);
        final String json = "{\"refId\":null, \"family\":{\"paternal\":{\"aunt\":{\"refId\":null, \"family\":{}, \"name\":\"Ruth\", \"class\":\"com.kfuntak.gwt.json.serialization.client.domain.Contact\"}, \"uncle\":{\"refId\":null, \"family\":{}, \"name\":\"John\", \"class\":\"com.kfuntak.gwt.json.serialization.client.domain.Contact\"}}, \"maternal\":{\"aunt\":{\"refId\":null, \"family\":{}, \"name\":\"Jenny\", \"class\":\"com.kfuntak.gwt.json.serialization.client.domain.Contact\"}, \"uncle\":{\"refId\":null, \"family\":{}, \"name\":\"Bill\", \"class\":\"com.kfuntak.gwt.json.serialization.client.domain.Contact\"}}}, \"name\":\"Mark\", \"class\":\"com.kfuntak.gwt.json.serialization.client.domain.Contact\"}";
        final Contact contact = (Contact)serializer.deSerialize(json, "com.kfuntak.gwt.json.serialization.client.domain.Contact");
        assertEquals(familyMap.toString(),contact.getFamily().toString());
    }

    @Test
    public void testNestedHashMapSerialization() {
        final Serializer serializer = (Serializer) GWT.create(Serializer.class);
        final HashMap<String, HashMap<String, Contact>> familyMap = new HashMap<String, HashMap<String, Contact>>();
        HashMap<String, Contact> familyBranchMap = new HashMap<String, Contact>();
        familyBranchMap.put("uncle", new Contact("Bill"));
        familyBranchMap.put("aunt", new Contact("Jenny"));
        familyMap.put("maternal",familyBranchMap);
        familyBranchMap = new HashMap<String, Contact>();
        familyBranchMap.put("uncle", new Contact("John"));
        familyBranchMap.put("aunt", new Contact("Ruth"));
        familyMap.put("paternal", familyBranchMap);
        final Contact contact = new Contact("Mark");
        contact.setFamily(familyMap);
        final String refJson = "{\"refId\":null, \"family\":{\"paternal\":{\"aunt\":{\"refId\":null, \"family\":{}, \"name\":\"Ruth\", \"class\":\"com.kfuntak.gwt.json.serialization.client.domain.Contact\"}, \"uncle\":{\"refId\":null, \"family\":{}, \"name\":\"John\", \"class\":\"com.kfuntak.gwt.json.serialization.client.domain.Contact\"}}, \"maternal\":{\"aunt\":{\"refId\":null, \"family\":{}, \"name\":\"Jenny\", \"class\":\"com.kfuntak.gwt.json.serialization.client.domain.Contact\"}, \"uncle\":{\"refId\":null, \"family\":{}, \"name\":\"Bill\", \"class\":\"com.kfuntak.gwt.json.serialization.client.domain.Contact\"}}}, \"name\":\"Mark\", \"class\":\"com.kfuntak.gwt.json.serialization.client.domain.Contact\"}";
        final String json = serializer.serialize(contact);
        assertEquals(refJson,json);
    }


    @Test
    @Ignore
    public void testSerialization() {
        final Serializer serializer = (Serializer) GWT.create(Serializer.class);

        final String jsonText = "{"
                + "\"class\":\"com.kfuntak.gwt.json.serialization.client.domain.University\""
                + ",\"contactInfo\":"
                + "[{\"address\":"
                + "{\"city\":null,"
                + "\"class\":\"com.kfuntak.gwt.json.serialization.client.domain.Address\","
                + "\"country\":\"India\","
                + "\"line1\":null,"
                + "\"line2\":null,"
                + "\"state\":\"Tamilnadu\","
                + "\"zipCode\":null},"
                + "\"name\":\"Peter\","
                + "\"class\":\"com.kfuntak.gwt.json.serialization.client.domain.Contact\","
                + "\"family\":{\"paternal\":{\"aunt\":{\"refId\":null, \"family\":{}, \"name\":\"Ruth\", \"class\":\"com.kfuntak.gwt.json.serialization.client.domain.Contact\"}, \"uncle\":{\"refId\":null, \"family\":{}, \"name\":\"John\", \"class\":\"com.kfuntak.gwt.json.serialization.client.domain.Contact\"}}, \"maternal\":{\"aunt\":{\"refId\":null, \"family\":{}, \"name\":\"Jenny\", \"class\":\"com.kfuntak.gwt.json.serialization.client.domain.Contact\"}, \"uncle\":{\"refId\":null, \"family\":{}, \"name\":\"Bill\", \"class\":\"com.kfuntak.gwt.json.serialization.client.domain.Contact\"}}},"
                + "\"phoneNumber\":{\"class\":\"com.kfuntak.gwt.json.serialization.client.domain.PhoneNumber\","
                + "\"ext\":null,"
                + "\"listedStatus\":null,"
                + "\"type\":null},"
                + "\"refId\":null}],"
                + "\"forVerification\":\"Really for verification\","
                + "\"gradeLevels\":[\"12\",\"11\"],"
                + "\"refId\":\"cms\","
                + "\"refIdKey\":null,"
                + "\"schoolName\":\"CMS\","
                + "\"schoolShortName\":null,"
                + "\"schoolUrl\":\"http://cms.in\","
                + "\"startDate\":1252046885585,"
                + "\"status\":11}";
        final University university = (University) serializer.deSerialize(jsonText, "com.kfuntak.gwt.json.serialization.client.domain.University");
        final University refUniversity = createRefUniversity();
        assertEquals(refUniversity.toString(),university.toString());
    }

    @SuppressWarnings("deprecation")
	private University createRefUniversity() {
        final University university = new University();
        final Set<Contact> contactInfo = new HashSet<Contact>();
        university.setContactInfo(contactInfo);
        final Contact contact = new Contact("Peter");
        contactInfo.add(contact);
        final Address address = new Address();
        contact.setAddress(address);
        address.setState("Tamilnadu");
        address.setCountry("India");
        PhoneNumber phoneNumber = new PhoneNumber();
        contact.setPhoneNumber(phoneNumber);
        final HashMap<String, HashMap<String, Contact>> familyMap = new HashMap<String, HashMap<String, Contact>>();
        HashMap<String, Contact> familyBranchMap = new HashMap<String, Contact>();
        familyBranchMap.put("uncle", new Contact("Bill"));
        familyBranchMap.put("aunt", new Contact("Jenny"));
        familyMap.put("maternal",familyBranchMap);
        familyBranchMap = new HashMap<String, Contact>();
        familyBranchMap.put("uncle", new Contact("John"));
        familyBranchMap.put("aunt", new Contact("Ruth"));
        familyMap.put("paternal", familyBranchMap);
        contact.setFamily(familyMap);
        phoneNumber = new PhoneNumber();
        contact.setPhoneNumber(phoneNumber);
        university.setGradeLevels(Arrays.asList("12","11"));
        university.setForVerification("Really for verification");
        university.setSchoolName("CMS");
        university.setSchoolUrl("http://cms.in");
        university.setStartDate(new Date(109,8,4,1,48,5));
        university.setStatus(11);
        university.setRefId("cms");
        return university;
    }
}
