package com.zzw.jrpc.serialize;

public interface Serializer {

    byte[] serialize(Object object);

    <T> T deSerialize(byte[] bytes, Class<T> clazz);

}
