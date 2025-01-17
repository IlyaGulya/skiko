
// This file has been auto generated.

#include "SkData.h"
#include "common.h"

static void deleteData(SkData* data) {
    // std::cout << "Deleting [SkData " << data << "]" << std::endl;
    data->unref();
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Data__1nGetFinalizer(){
    return reinterpret_cast<KNativePointer>((&deleteData));
}

SKIKO_EXPORT KInt org_jetbrains_skia_Data__1nSize
  (KNativePointer ptr) {
    SkData* instance = reinterpret_cast<SkData*>((ptr));
    return instance->size();
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Data__1nToByteBuffer
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Data__1nToByteBuffer");
}

#if 0
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Data__1nToByteBuffer
  (KNativePointer ptr) {
    SkData* instance = reinterpret_cast<SkData*>((ptr));
    return env->NewDirectByteBuffer(instance->writable_data(), instance->size());
}
#endif



SKIKO_EXPORT void org_jetbrains_skia_Data__1nBytes
  (KNativePointer ptr, int offset, int length, KByte* destBytes) {
    SkData* instance = reinterpret_cast<SkData*>(ptr);
    const KByte* source = reinterpret_cast<const KByte*>(instance->bytes() + offset);
    memcpy(destBytes, source, length);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Data__1nEquals
  (KNativePointer ptr, KNativePointer otherPtr) {
    SkData* instance = reinterpret_cast<SkData*>((ptr));
    SkData* other = reinterpret_cast<SkData*>((otherPtr));
    return instance->equals(other);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Data__1nMakeFromBytes
  (KByte* bytesArray, KInt offset, KInt length) {
    KByte* bytes = reinterpret_cast<KByte*>(malloc(length));
    if (!bytes) return 0;
    memcpy(bytes, bytesArray + offset, length);
    SkData* instance = SkData::MakeFromMalloc(bytes, length).release();
    return instance;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Data__1nMakeFromFileName
  (KInteropPointer pathStr) {
    TODO("implement org_jetbrains_skia_Data__1nMakeFromFileName");
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Data__1nMakeSubset
  (KNativePointer ptr, KInt offset, KInt length) {
    SkData* instance = reinterpret_cast<SkData*>((ptr));
    SkData* subset = SkData::MakeSubset(instance, offset, length).release();
    return reinterpret_cast<KNativePointer>(subset);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Data__1nMakeEmpty() {
    SkData* instance = SkData::MakeEmpty().release();
    return reinterpret_cast<KNativePointer>(instance);
}
