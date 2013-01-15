package com.kfuntak.gwt.json.serialization;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.dev.javac.typemodel.JEnumType;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import com.kfuntak.gwt.json.serialization.client.AbstractObjectSerializer;
import com.kfuntak.gwt.json.serialization.client.DeserializerHelper;
import com.kfuntak.gwt.json.serialization.client.DontSerializeClass;
import com.kfuntak.gwt.json.serialization.client.IncompatibleObjectException;
import com.kfuntak.gwt.json.serialization.client.SerializerHelper;

public class SerializationGenerator extends Generator {

    private JClassType stringClass;
    private SourceWriter srcWriter;
    private String className;
    private TypeOracle typeOracle;
    private final boolean DEBUG = false;
    private JClassType MARKER_INTERFACE;
    private String indention = "";
    private int suffixIndex = 0;

    public SerializationGenerator() throws NotFoundException {
    }

    public void writeLn(final String code) {
        srcWriter.println(code);
        if (DEBUG) {
            System.out.println(indention + code);
        }
    }

    public void indent() {
        srcWriter.indent();
        indention += "    ";
    }

    public void outdent() {
        srcWriter.outdent();
        indention = indention.substring(0, indention.length() - 4);
    }

    @Override
	public String generate(final TreeLogger logger, final GeneratorContext ctx,
            final String requestedClass) throws UnableToCompleteException {
        //get the type oracle
        typeOracle = ctx.getTypeOracle();
        try {
            MARKER_INTERFACE = typeOracle.getType("com.kfuntak.gwt.json.serialization.client.JsonSerializable");
        } catch (final NotFoundException e) {
            throw new UnableToCompleteException();
        }
        assert (typeOracle != null);
        assert (MARKER_INTERFACE != null);
        stringClass = typeOracle.findType(String.class.getName());
        assert (stringClass != null);

        //get class from type oracle
        final JClassType serializeClass = typeOracle.findType(requestedClass);

        if (serializeClass == null) {
            logger.log(TreeLogger.ERROR, "Unable to find metadata for type '"
                    + requestedClass + "'", null);
            throw new UnableToCompleteException();
        }

        //create source writer
        final String packageName = serializeClass.getPackage().getName();
        className = serializeClass.getSimpleSourceName() + "_TypeSerializer";
        final PrintWriter printWriter = ctx.tryCreate(logger, packageName, className);
        if (printWriter == null) {
            return packageName + "." + className;
        }
        final ClassSourceFileComposerFactory composerFactory =
                new ClassSourceFileComposerFactory(packageName, className);
        composerFactory.setSuperclass("com.kfuntak.gwt.json.serialization.client.Serializer");

        final JClassType[] subTypes = addImports(composerFactory);

        srcWriter = composerFactory.createSourceWriter(ctx, printWriter);
        if (srcWriter == null) {
            return packageName + "." + className;
        }

        //create a serializer for each interface that supports Serializable
        final HashMap<String, JClassType> serializerMap = writeTypeSerializers(subTypes);

        //in the class constructor, add each serializer
        writeTypeSerializerConstructor(serializerMap);

        srcWriter.commit(logger);
        return packageName + "." + className;
    }

    private HashMap<String, JClassType> writeTypeSerializers(final JClassType[] subTypes) {
        final HashMap<String, JClassType> serializerMap = new HashMap<String, JClassType>();
        for (final JClassType typeToSerialize : subTypes) {
            if (typeToSerialize.isAbstract()) {
                continue;
            }
            final String serializerName = writeTypeSerializerClass(typeToSerialize);
            serializerMap.put(serializerName, typeToSerialize);
        }
        return serializerMap;
    }

    private String writeTypeSerializerClass(final JClassType typeToSerialize) {
        final String serializerName = typeToSerialize.getName().replaceAll("\\.","\\$");
        writeLn("public class " + serializerName + "_SerializableImpl extends AbstractObjectSerializer{");
        indent();

        try {
            generateTypeSerialization(typeToSerialize);
            generateTypeDeserialization(typeToSerialize.getQualifiedSourceName());

        } catch (final NotFoundException e) {
            e.printStackTrace();
        } catch (final UnableToCompleteException e) {
            e.printStackTrace();
        }
        outdent();
        writeLn("}");
        return serializerName;
    }

    private JClassType[] addImports(final ClassSourceFileComposerFactory composerFactory) throws UnableToCompleteException {
        final Set<String> importsList = new HashSet<String>();
        // Java imports
        importsList.add(Collection.class.getName());
        importsList.add(List.class.getName());
        importsList.add(ArrayList.class.getName());
        importsList.add(HashSet.class.getName());
        importsList.add(Date.class.getName());
        importsList.add(HashMap.class.getName());
        importsList.add(Map.class.getName());
//		// GWT imports
        importsList.add(com.google.gwt.core.client.GWT.class.getName());
        importsList.add(com.google.gwt.json.client.JSONNull.class.getName());
        importsList.add(com.google.gwt.json.client.JSONNumber.class.getName());
        importsList.add(com.google.gwt.json.client.JSONString.class.getName());
        importsList.add(com.google.gwt.json.client.JSONValue.class.getName());
        importsList.add(com.google.gwt.json.client.JSONObject.class.getName());
        importsList.add(com.google.gwt.json.client.JSONArray.class.getName());
        importsList.add(com.google.gwt.json.client.JSONBoolean.class.getName());
        importsList.add(com.google.gwt.json.client.JSONParser.class.getName());
        importsList.add(com.google.gwt.json.client.JSONException.class.getName());
//		// Module imports
        importsList.add(AbstractObjectSerializer.class.getName());
        importsList.add(MARKER_INTERFACE.getQualifiedSourceName());
        importsList.add(IncompatibleObjectException.class.getName());
        importsList.add(SerializerHelper.class.getName());
        importsList.add(DeserializerHelper.class.getName());

        final JClassType[] subTypes = MARKER_INTERFACE.getSubtypes();
        for (int i = 0; i < subTypes.length; ++i) {
            final String serializedType = subTypes[i].getQualifiedSourceName();
            JClassType baseType;
            try {
                baseType = typeOracle.getType(serializedType);
            } catch (final NotFoundException e) {
                throw new UnableToCompleteException();
            }
            discoverImports(importsList, baseType);
        }

        for (final String typeToImport : importsList) {
            composerFactory.addImport(typeToImport);
        }
        return subTypes;
    }

    private void writeTypeSerializerConstructor(final HashMap<String, JClassType> serializerMap) {
        writeLn("public " + className + "(){");
        indent();
        for (final Map.Entry<String, JClassType> entry : serializerMap.entrySet()) {
            addObjectSerializer(entry);
        }

        outdent();
        writeLn("}");
    }

    private void addObjectSerializer(final Map.Entry<String, JClassType> entry) {
        writeLn("addObjectSerializer(\"" + entry.getValue().getQualifiedSourceName() + "\", new " + entry.getKey() + "_SerializableImpl() );");
        if (!entry.getValue().getQualifiedSourceName().equals(entry.getValue().getQualifiedBinaryName())) {
            writeLn("addObjectSerializer(\"" + entry.getValue().getQualifiedBinaryName() + "\", new " + entry.getKey() + "_SerializableImpl() );");
        }
    }

    private void discoverImports(final Set<String> importsList, final JClassType baseType) {
        addImports(importsList, baseType);
        for (final JField field : baseType.getFields()) {
            if (!field.isStatic() && !field.isTransient()) {
                final JType fieldType = field.getType();
                if (fieldType.isClassOrInterface() != null) {
                    addImports(importsList, fieldType.isClassOrInterface());
                }
            }
        }
    }

    private void addImports(final Set<String> importsList, final JClassType baseType) {
        if (baseType.isEnum() != null) {
            importsList.add(baseType.getQualifiedSourceName());
        } else if (baseType.isParameterized() != null) {
            importsList.add(baseType.getQualifiedSourceName());
            final JParameterizedType parameterizedType = baseType.isParameterized();
            for (final JClassType typeParm : parameterizedType.getTypeArgs()) {
                if(!importsList.contains(typeParm.getQualifiedSourceName()))
                addImports(importsList, typeParm);
            }
        } else if (baseType.isAssignableTo(MARKER_INTERFACE) && !importsList.contains(baseType.getQualifiedSourceName())) {
            importsList.add(baseType.getQualifiedSourceName());
            discoverImports(importsList, baseType);
        }
    }

    private void generateTypeDeserialization(final String typeName) throws NotFoundException, UnableToCompleteException {

        final JClassType baseType = typeOracle.getType(typeName);
//        final String packageName = baseType.getPackage().getName();

        writeLn("public Object deSerialize(JSONValue jsonValue, String className) throws JSONException{");
        indent();
        // Return null if the given object is null
        writeLn("if(jsonValue instanceof JSONNull){");
        indent();
        writeLn("return null;");
        outdent();
        writeLn("}");

        // Throw Incompatible exception is JsonValue is not an instance of
        // JsonObject
        writeLn("if(!(jsonValue instanceof JSONObject)){");
        indent();
        writeLn("throw new IncompatibleObjectException();");
        outdent();
        writeLn("}");

        // Initialise JsonObject then
        final String baseTypeName = baseType.getSimpleSourceName();
        writeLn("JSONObject jsonObject=(JSONObject)jsonValue;");
        writeLn(baseTypeName + " mainResult=new " + baseTypeName + "();");
        writeLn("JSONArray inputJsonArray=null;");
        writeLn("int inpJsonArSize=0;");
        writeLn("JSONValue fieldJsonValue=null;");
        writeLn("JSONObject inputJsonObject=null;");

        // Start deSerialisation
        final List<JField> allFields = new ArrayList<JField>();
        JField[] fields = baseType.getFields();
        for (final JField field : fields) {
            if (!field.isStatic() && !field.isTransient()) {
                allFields.add(field);
            }
        }
        if (baseType.isAssignableTo(MARKER_INTERFACE)) {
            boolean flag = true;
            JClassType superClassType = baseType;
            while (flag) {
                superClassType = superClassType.getSuperclass();
                if (superClassType.isAssignableTo(MARKER_INTERFACE)) {
                    final JField[] subClassFields = superClassType.getFields();
                    for (final JField subClassField : subClassFields) {
                        if (!subClassField.isStatic() && !subClassField.isTransient()) {
                            allFields.add(subClassField);
                        }
                    }
                } else {
                    flag = false;
                }
            }
        }
        fields = new JField[allFields.size()];
        allFields.toArray(fields);

        for (final JField field : fields) {
            final JType fieldType = field.getType();
            final String fieldName = field.getName();
            writeLn("fieldJsonValue=jsonObject.get(\"" + fieldName + "\");");
            if (fieldType.isPrimitive() != null) {
                final JPrimitiveType fieldPrimitiveType = (JPrimitiveType) fieldType;
                final JClassType fieldBoxedType = typeOracle.getType(fieldPrimitiveType.getQualifiedBoxedSourceName());
                final String valueString = deserializeSimpleType(fieldBoxedType, "fieldJsonValue");
                setValue("mainResult", baseType, field, valueString);
            } else {
                // Return null if JSON object is null
                final JClassType fieldClassType = (JClassType) fieldType;
                final String value = deserializeValue(fieldClassType, "fieldJsonValue");
                setValue("mainResult", baseType, field, value);
            }
        }

        writeLn("return mainResult;");
        outdent();
        writeLn("}");
    }

    private void setValue(final String dest, final JClassType classType, final JField field, final String value) throws NotFoundException {
        final String fieldNameForGS = getNameForGS(field.getName());
        final Set<? extends JClassType> classes = classType.getFlattenedSupertypeHierarchy();
        final String setter = "set" + fieldNameForGS;
        final String getter = "get" + fieldNameForGS;
        for (final JClassType aClass : classes) {
            JMethod method = aClass.findMethod(setter, new JType[]{field.getType()});
            if (method != null) {
                writeLn(dest + ".set" + fieldNameForGS + "(" + value + ");");
                return;
            }
        	if (aClass.isAssignableTo(typeOracle.getType("java.util.Collection"))) {
        		method = aClass.findMethod (getter, new JType[]{field.getType()});
        		if (method != null) {
        			writeLn(dest + ".get" + fieldNameForGS + "().addAll(" + value + ");");
        			return;
        		}
        	}
        }
        writeLn(dest + "." + field.getName() + "=" + value + ";");
    }

    private String deserializeCollection(final JClassType colType, final String inputColVar) throws NotFoundException, UnableToCompleteException {
        final String loopSuffix = getLoopVarSuffix();
        final JParameterizedType parameterizedType = (JParameterizedType) colType;
        final JClassType valueType = parameterizedType.getTypeArgs()[0];
        final String valueTypeString = createTypeString(valueType, false);
        final String colVar = "col" + loopSuffix;// Field Collection Result

        writeLn(createTypeString(colType, false) + " " + colVar + " = new " + createTypeString(colType, true) + "();");
        writeLn("DeserializerHelper.fillCollection(" + colVar + ", " + inputColVar + ", new DeserializationCallback() {");
        indent();
        writeLn("public " + valueTypeString + " deserialize(JSONValue jsonValue) {");
        indent();
        final String value = deserializeValue(valueType, "jsonValue");
        writeLn("return " + value + ";");
        outdent();
        writeLn("}");
        outdent();
        writeLn("});");
        return colVar;
    }

    private String deserializeValue(final JClassType valueType, final String valVar) throws NotFoundException, UnableToCompleteException {
        String value;
        if (valueType.isAssignableTo(MARKER_INTERFACE)) {
            value = deserializeType(valueType, valVar);
        } else if (valueType.isEnum() != null) {
            value = deserializeEnum((JEnumType) valueType, valVar);
        } else if (valueType.isAssignableTo(typeOracle.getType("java.util.Map"))) {
            value = deserializeMap(valueType, valVar);
        } else if (valueType.isAssignableTo(typeOracle.getType("java.util.Collection"))) {
            value = deserializeCollection(valueType, valVar);
        } else {
            value = deserializeSimpleType(valueType, valVar);
        }
        return value;
    }

    private String createTypeParmString(final JParameterizedType parameterizedType) throws NotFoundException {
        final StringBuilder sb = new StringBuilder();
        sb.append('<');
        boolean first = true;
        for (final JClassType type : parameterizedType.getTypeArgs()) {
            if (!first) {
                sb.append(',');
            } else {
                first = false;
            }

            sb.append(createTypeString(type, false));
        }
        sb.append('>');
        return sb.toString();
    }

    private String createTypeString(final JType fieldType, final boolean forCreation) throws NotFoundException {
        final JPrimitiveType primitiveType = fieldType.isPrimitive();
        if (primitiveType != null) {
            return createTypeString(typeOracle.getType(primitiveType.getQualifiedBoxedSourceName()),forCreation);
        }

        final JParameterizedType parameterizedType = fieldType.isParameterized();
        if (parameterizedType != null) {
            final StringBuilder sb = new StringBuilder();
            if (forCreation && parameterizedType.getName().equals("Map")) {
                sb.append("HashMap");
            } else if (forCreation && parameterizedType.getName().equals("List")) {
                sb.append("ArrayList");
            } else if (forCreation && parameterizedType.getName().equals("Set")) {
                sb.append("HashSet");
            } else {
                sb.append(parameterizedType.getName());
            }
            sb.append(createTypeParmString(parameterizedType));
            return sb.toString();
        }

        return ((JClassType) fieldType).getName();
    }

    private String deserializeMap(final JClassType mapType, final String inputMapVar) throws UnableToCompleteException, NotFoundException {
        final JParameterizedType parameterizedType = (JParameterizedType) mapType;
        final JClassType keyParm = parameterizedType.getTypeArgs()[0];
        final JClassType valueParm = parameterizedType.getTypeArgs()[1];
        final String valueTypeString = createTypeString(valueParm, false);
        if(!keyParm.getQualifiedSourceName().equals("java.lang.String")) {
            throw new UnableToCompleteException();
        }
        final String loopSuffix = getLoopVarSuffix();
        final String mapVar = "map" + loopSuffix;// Field Collection Result

        writeLn(createTypeString(mapType, false) + " " + mapVar + " = new " + createTypeString(mapType, true) + "();");
        writeLn("DeserializerHelper.fillMap(" + mapVar + ", " + inputMapVar + ", new DeserializationCallback<"+ valueTypeString +"> () {");
        indent();
        writeLn("public " + valueTypeString + " deserialize(JSONValue jsonValue) {");
        indent();
        final String value = deserializeValue(valueParm, "jsonValue");
        writeLn("return " + value + ";");
        outdent();
        writeLn("}");
        outdent();
        writeLn("});");
        return mapVar;
    }

    private String deserializeEnum(final JEnumType enumType, final String inputValVar) {
        writeLn("//deserializeEnum - " + enumType.toString() + " - " + inputValVar);
        final String enumVar = "enum" + getLoopVarSuffix();
//        final JEnumConstant defaultConstant = enumType.getEnumConstants()[0];
        writeLn(enumType.getSimpleSourceName() + " " + enumVar + " = null;");
        writeLn("if(" + inputValVar + " != null && " + inputValVar + ".isString() != null) {");
        indent();
        writeLn(enumVar + " = " + enumType.getSimpleSourceName() + ".valueOf(" + inputValVar + ".isString().stringValue());");
        outdent();
        writeLn("}");
        return enumVar;
    }

    private String serializeEnum(final JEnumType enumType, final String inputValVar) {
        writeLn("//Serialize Enum");
        final String enumVar = "enum" + getLoopVarSuffix();
        writeLn("JSONValue " + enumVar + " = JSONNull.getInstance();");
        writeLn("if ("+inputValVar+" != null){");
        indent();
        writeLn(enumVar + "= new JSONString((("+enumType.getSimpleSourceName()+")"+inputValVar+").name());");
        outdent();
        writeLn("}");
        return enumVar;
    }

    private String deserializeSimpleType(final JClassType fieldClassType, final String variable) {
        if (fieldClassType.getQualifiedSourceName().equals("java.lang.Short")) {
            return "DeserializerHelper.getShort(" + variable + ")";
        } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Byte")) {
            return "DeserializerHelper.getByte(" + variable + ")";
        } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Long")) {
            return "DeserializerHelper.getLong(" + variable + ")";
        } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Integer")) {
            return "DeserializerHelper.getInt(" + variable + ")";
        } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Float")) {
            return "DeserializerHelper.getFloat(" + variable + ")";
        } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Double")) {
            return "DeserializerHelper.getDouble(" + variable + ")";
        } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Boolean")) {
            return "DeserializerHelper.getBoolean(" + variable + ")";
        } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Character")) {
            return "DeserializerHelper.getChar(" + variable + ")";
        } else if (fieldClassType.getQualifiedSourceName().equals("java.util.Date")) {
            return "DeserializerHelper.getDate(" + variable + ")";
        } else {
            return "DeserializerHelper.getString(" + variable + ")";
        }
    }

    private String deserializeType(final JClassType fieldClassType, final String inputTypeVar) {
        writeLn("//deserializeType - " + fieldClassType + " - " + inputTypeVar);
        final String typeVar = "deserType" + getLoopVarSuffix();
        writeLn(fieldClassType.getName() + " " + typeVar + " = null;");
        writeLn("if (" + inputTypeVar + " != null && !(" + inputTypeVar + " instanceof JSONNull)){");
        indent();

        writeLn(typeVar + " = " + "(" + fieldClassType.getSimpleSourceName() + ")Serializer_TypeSerializer.this.deSerialize(" + inputTypeVar + ", \""+fieldClassType.getQualifiedSourceName()+"\");");
        outdent();
        writeLn("}");
        return typeVar;
    }

    private void generateTypeSerialization(final JClassType typeToSerialize) throws NotFoundException, UnableToCompleteException {
        final String typeName = typeToSerialize.getQualifiedSourceName();

        final JClassType baseType = typeOracle.getType(typeName);
        writeLn("public JSONValue serializeToJson(Object object){");
        indent();
        // Return JSONNull instance if object is null
        writeLn("if(object==null){");
        indent();
        writeLn("return JSONNull.getInstance();");
        outdent();
        writeLn("}");

        // Throw Incompatible Exception if object is not of the type it claims
        // to be
        writeLn("if(!(object instanceof " + baseType.getSimpleSourceName() + ")){");
        indent();
        writeLn("throw new IncompatibleObjectException();");
        outdent();
        writeLn("}");

        // Initialise result object
        writeLn("JSONObject mainResult=new JSONObject();");
        writeLn("Object fieldValue=null;");
        writeLn(baseType.getSimpleSourceName() + " mainVariable=(" + baseType.getSimpleSourceName() + ")object;");

        // Serialise fields
        final List<JField> allFields = new ArrayList<JField>();
        JField[] fields = baseType.getFields();
        for (final JField field : fields) {
            if (!field.isStatic() && !field.isTransient()) {
                allFields.add(field);
            }
        }
        if (baseType.isAssignableTo(MARKER_INTERFACE)) {
            boolean flag = true;
            JClassType superClassType = baseType;
            while (flag) {
                superClassType = superClassType.getSuperclass();
                if (superClassType.isAssignableTo(MARKER_INTERFACE)) {
                    final JField[] subClassFields = superClassType.getFields();
                    for (final JField subClassField : subClassFields) {
                        if (!subClassField.isStatic() && !subClassField.isTransient()) {
                            allFields.add(subClassField);
                        }
                    }
                } else {
                    flag = false;
                }
            }
        }
        fields = new JField[allFields.size()];
        allFields.toArray(fields);
        for (final JField field : fields) {
            final JType fieldType = field.getType();
            final String fieldName = field.getName();
            assignValue("fieldValue", baseType, field);
            final JClassType fieldClassType = boxType(fieldType);
            final String value = serializeValue(fieldClassType, "fieldValue");
            writeLn("mainResult.put(\"" + fieldName + "\"," + value + ");");
        }

        if(!typeToSerialize.isAnnotationPresent(DontSerializeClass.class)){
            // Put class type for compatibility with flex JSON [de]serialisation
            writeLn("mainResult.put(\"class\",new JSONString(\"" + baseType.getQualifiedSourceName() + "\"));");
        }

        // Return statement
        writeLn("return mainResult;");
        outdent();
        writeLn("}");
    }

    private void assignValue(final String lvalue, final JClassType classType, final JField field) throws NotFoundException {
        final String getter = getGetter(classType, field);
        if(getter != null) {
            writeLn(lvalue + "=mainVariable." + getter + "();");
        } else {
            writeLn(lvalue + "=mainVariable." + field.getName() + ";");
        }
    }

    private String getGetter(final JClassType classType, final JField field) throws NotFoundException {
        String getter = null;
        final String fieldNameForGS = getNameForGS(field.getName());
        final Set<? extends JClassType> classes = classType.getFlattenedSupertypeHierarchy();

        if (boxType(field.getType()).getQualifiedSourceName().equals("java.lang.Boolean")) {
            getter = "is" + fieldNameForGS;
            for (final JClassType aClass : classes) {
                final JMethod method =  aClass.findMethod(getter, new JType[0]);
                if (method != null) {
                    return getter;
                }
            }
        }

        getter = "get" + fieldNameForGS;
        for (final JClassType aClass : classes) {
            final JMethod method =  aClass.findMethod(getter, new JType[0]);
            if (method != null) {
                return getter;
            }
        }

        return null;
    }

    private JClassType boxType(final JType fieldType) throws NotFoundException {
        final JPrimitiveType primitiveType = fieldType.isPrimitive();
        if (primitiveType != null) {
            return typeOracle.getType(primitiveType.getQualifiedBoxedSourceName());
        }

        return (JClassType)fieldType;
    }

    private String serializeCollection(final JClassType fieldClassType, final String variable) throws NotFoundException, UnableToCompleteException {
        writeLn("//Serialize Collection");
        final JParameterizedType parameterizedType = (JParameterizedType) fieldClassType;
        JClassType typeParm = parameterizedType.getTypeArgs()[0];
        final String typeParmName = createTypeString(typeParm, false);
        final String suffix = getLoopVarSuffix();
        final String colVar = "col"+suffix;
        writeLn("JSONValue " + colVar + " = SerializerHelper.getCollection((Collection<" + typeParmName + ">)" + variable + ", new SerializationCallback() {");
        indent();
        writeLn("public JSONValue serialize(Object value) {");
        indent();
        typeParm = boxType(typeParm);
        final String value = serializeValue(typeParm, "value");
        writeLn("return " + value + ";");
        outdent();
        writeLn("}");
        outdent();
        writeLn("});");

        return colVar;
    }

    private String serializeValue(final JClassType typeParm, final String valVar) throws NotFoundException, UnableToCompleteException {
        writeLn("//Serialize Value");
        String value;
        if (typeParm.isAssignableTo(typeOracle.getType("java.util.Collection"))) {
            value = serializeCollection(typeParm, valVar);
        } else if (typeParm.isAssignableTo(typeOracle.getType("java.util.Map"))) {
            value = serializeMap(typeParm, valVar);
        } else if (typeParm.isEnum() != null) {
            value = serializeEnum((JEnumType)typeParm, valVar);
        } else {
            value = getTypedValueAssignment(typeParm, valVar);
        }
        return value;
    }

    private String serializeMap(final JClassType fieldClassType, final String variable) throws NotFoundException, UnableToCompleteException {
        writeLn("//Serialize Map");
        final JParameterizedType parameterizedType = (JParameterizedType) fieldClassType;
        final JClassType keyParm = parameterizedType.getTypeArgs()[0];
        final String keyParamName = keyParm.getQualifiedSourceName();
		if(!keyParamName.equals("java.lang.String") && !keyParamName.equals("java.lang.Long")) {
            throw new UnableToCompleteException();
        }
        JClassType valueParm = parameterizedType.getTypeArgs()[1];
        final String genericClause = createTypeParmString(parameterizedType);
        final String suffix = getLoopVarSuffix();
        final String mapVar = "map" + suffix;
        writeLn("JSONValue "+ mapVar + " = SerializerHelper.getMap((Map" + genericClause + ")" + variable + ", new SerializationCallback () {");
        indent();
        writeLn("public JSONValue serialize(Object value) {");
        indent();
        valueParm = boxType(valueParm);
        final String value = serializeValue(valueParm, "value");
        writeLn("return " + value + ";");
        outdent();
        writeLn("}");
        outdent();
        writeLn("});");
        return mapVar;
    }

    private String getTypedValueAssignment(final JClassType fieldClassType, final String variable) throws NotFoundException{
        final String fieldClassTypeString = fieldClassType.getQualifiedSourceName();
        if (fieldClassTypeString.equals("java.lang.String")) {
            return "SerializerHelper.getString((String)"+ variable +")";
        } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Boolean")) {
            return "SerializerHelper.getBoolean((Boolean)"+ variable +")";
        } else if (fieldClassTypeString.equals("java.lang.Character")) {
            return "jsonValue=SerializerHelper.getChar((Character)"+ variable +")";
        } else if (fieldClassType.isAssignableTo(typeOracle.getType("java.lang.Number"))) {
            return "SerializerHelper.getNumber((Number)"+ variable +")";
        } else if (fieldClassTypeString.equals("java.util.Date")) {
            return "SerializerHelper.getDate((Date)"+ variable +")";
        } else if (fieldClassType.isAssignableTo(MARKER_INTERFACE)) {
            return "Serializer_TypeSerializer.this.serializeToJson("+ variable +")";
        } else if (fieldClassType.isEnum() != null) {
            return variable + ".toString()";
        }

        return(variable + ".toString()");
    }

    private static String getNameForGS(final String name) {
        final StringBuilder buffer = new StringBuilder(name);
        buffer.setCharAt(0, new String(new char[]{name.charAt(0)}).toUpperCase().charAt(0));
        return buffer.toString();
    }

    private String getLoopVarSuffix() {
        suffixIndex += 1;
        return Integer.toString(suffixIndex);
    }
}