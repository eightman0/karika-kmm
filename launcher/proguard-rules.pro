# Add project specific ProGuard rules here.

# WorkManager instantiates its Room-generated WorkDatabase_Impl via reflection
# (Class.getDeclaredConstructor()). Room's own consumer rule only keeps the RoomDatabase
# subclass itself, not its no-arg constructor, so R8 strips it and the app crashes on launch
# with "NoSuchMethodException: androidx.work.impl.WorkDatabase_Impl.<init> []".
-keep class * extends androidx.room.RoomDatabase {
    <init>(...);
}
