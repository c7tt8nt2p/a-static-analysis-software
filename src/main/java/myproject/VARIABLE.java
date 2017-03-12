package myproject;

public enum VARIABLE {
    VAR_bool("bool"),
    VAR_byte("byte"),
    VAR_sbyte("sbyte"),
    VAR_char("char"),
    VAR_decimal("decimal"),
    VAR_double("double"),
    VAR_float("float"),
    VAR_int("int"),//int32
    VAR_uint("uint"),//unsigned int32
    VAR_long("long"),//int64
    VAR_ulong("ulong"),//uinsigned int64
    VAR_object("object"),
    VAR_short("short"),//int16
    VAR_ushort("ushort"),//unsigned int16
    VAR_string("string"),
    VAR_var("var"),

    //*************************

    VAR_Boolean("Boolean"),
    VAR_Byte("Byte"),
    VAR_SByte("SByte"),
    VAR_Char("Char"),
    VAR_Decimal("Decimal"),
    VAR_Double("Double"),
    VAR_Single("Single"),
    VAR_Int32("Int32"),//int32
    VAR_UInt32("UInt32"),//unsigned int32
    VAR_Int64("Int64"),//int64
    VAR_UInt64("UInt64"),//unsigned int64
    VAR_Object("Object"),
    VAR_Int16("Int16"),//int16
    VAR_UInt16("UInt16"),//unsigned int16
    VAR_String("String");


    private String variableName;

    VARIABLE(String variableName) {
        this.variableName = variableName;
    }

    public String getVariableName() {
        return variableName;
    }
}
