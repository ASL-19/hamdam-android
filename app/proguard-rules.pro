# By default, the flags in this file are appended to flags specified
# in /usr/share/android-studio/data/sdk/tools/proguard/proguard-android.txt

# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

##---------------Begin: proguard configuration common for all Android apps ----------
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
-dump class_files.txt
-printseeds seeds.txt
-printusage unused.txt
-printmapping mapping.txt
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-allowaccessmodification
-keepattributes *Annotation*
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable, Signature
-repackageclasses ''

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-dontnote com.android.vending.licensing.ILicensingService
-dontnote android.support.**
-dontwarn au.com.bytecode.opencsv.**

#eventbus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

#aws
# Class names are needed in reflection
-keepnames class com.amazonaws.**
# Request handlers defined in request.handlers
-keep class com.amazonaws.services.**.*Handler
# The following are referenced but aren't required to run
-dontwarn com.fasterxml.jackson.**
-dontwarn org.apache.commons.logging.**
# Android 6.0 release removes support for the Apache HTTP client
-dontwarn org.apache.http.**
# The SDK has several references of Apache HTTP client
-dontwarn com.amazonaws.http.**
-dontwarn com.amazonaws.metrics.**

# showcase
-keep,includedescriptorclasses class uk.co.deanwild.materialshowcaseview.** { *; }
-dontwarn uk.co.deanwild.materialshowcaseview.*

-keep,includedescriptorclasses class com.oraclejapan.smarttablayout.** { *; }
-dontwarn com.ogaclejapan.smarttablayout.*

-keep,includedescriptorclasses class android.support.v4.** { *; }
-dontwarn android.support.v4.*

-keep,includedescriptorclasses class android.support.v7.** { *; }
-dontwarn android.support.v7.*

-keep,includedescriptorclasses class android.support.design.widget.** { *; }
-dontwarn android.support.design.widget.*

-keep,includedescriptorclasses class org.eazegraph.lib.** { *; }