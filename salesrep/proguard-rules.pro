# kotlinx-serialization-json and Ktor's OkHttp engine both ship their own consumer proguard
# rules (keeping @Serializable generated code, etc.), so nothing extra is needed here yet. Add
# rules here if a release build turns up something stripped that shouldn't be.

# WorkManager instantiates its Room-generated WorkDatabase_Impl via reflection
# (Class.getDeclaredConstructor()). Room's own consumer rule only keeps the RoomDatabase
# subclass itself, not its no-arg constructor, so R8 strips it and the app crashes on launch
# with "NoSuchMethodException: androidx.work.impl.WorkDatabase_Impl.<init> []".
-keep class * extends androidx.room.RoomDatabase {
    <init>(...);
}
