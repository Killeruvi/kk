#include <jni.h>
#include <string>
#include <android/log.h>
#include <curl/curl.h>
#include <openssl/evp.h>
#include <openssl/pem.h>
#include <openssl/rsa.h>
#include <openssl/err.h>
#include <openssl/md5.h>
#include <openssl/aes.h>
#include <json.hpp>
#include <obfuscate.h>
#include "oxorany.h"
#include <openssl/sha.h>
#include "StrEnc.h"
#include <zlib.h>  // For CRC32
using json = nlohmann::ordered_json;

time_t rng = 0;
std::string Enc;
static char ZENINOP[64];
static std::string exdate = oxorany("NULL");

std::string g_Token, g_Auth;

bool xConnected = false, xServerConnection = false, memek = false;


extern "C"
JNIEXPORT jstring JNICALL
Java_com_CrimsonVeilDev_MAct_exdate(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(exdate.c_str());
}
extern "C" JNIEXPORT jstring JNICALL
Java_com_CrimsonVeilDev_MAct_ZENINOP(JNIEnv *env, jobject activityObject) {
    return env->NewStringUTF(ZENINOP);
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_CrimsonVeilDev_LogAct_GetKey(JNIEnv *env, jobject thiz) {
    return env->NewStringUTF(oxorany("https://t.me/CrimsonVeilDev")); // Link Channel
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_CrimsonVeilDev_utils_Downtwo_Version(JNIEnv *env, jclass clazz) {
    const char *versionUrl = (oxorany("https://github.com/Killeruvi/Crimson/releases/download/Uvi/version.txt"));
    return env->NewStringUTF(versionUrl);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_CrimsonVeilDev_utils_Downtwo_Link(JNIEnv *env, jclass clazz) {
    const char *downloadUrl = (oxorany("https://github.com/Killeruvi/Crimson/releases/download/Uvi/CrymsonCrystal.zip"));
    return env->NewStringUTF(downloadUrl);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_CrimsonVeilDev_BoxApplication_getSdkKey(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(oxorany("SDK_BMSVCinmYtHiuSj3g4ywP2Zqrc9cEsc7oKq42XOy"));
}


const char *GetAndroidID(JNIEnv *env, jobject context) {
    jclass contextClass = env->FindClass("android/content/Context");
    jmethodID getContentResolverMethod = env->GetMethodID(contextClass,"getContentResolver","()Landroid/content/ContentResolver;");
    jclass settingSecureClass = env->FindClass("android/provider/Settings$Secure");
    jmethodID getStringMethod = env->GetStaticMethodID(settingSecureClass,"getString", "(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;");

    auto obj = env->CallObjectMethod(context, getContentResolverMethod);
    auto str = (jstring) env->CallStaticObjectMethod(settingSecureClass, getStringMethod, obj,env->NewStringUTF("android_id"));
    return env->GetStringUTFChars(str, 0);
}

const char *GetDeviceModel(JNIEnv *env) {
    jclass buildClass = env->FindClass("android/os/Build");
    jfieldID modelId = env->GetStaticFieldID(buildClass, "MODEL","Ljava/lang/String;");

    auto str = (jstring) env->GetStaticObjectField(buildClass, modelId);
    return env->GetStringUTFChars(str, 0);
}

const char *GetDeviceBrand(JNIEnv *env) {
    jclass buildClass = env->FindClass("android/os/Build");
    jfieldID modelId = env->GetStaticFieldID(buildClass, "BRAND","Ljava/lang/String;");

    auto str = (jstring) env->GetStaticObjectField(buildClass, modelId);
    return env->GetStringUTFChars(str, 0);
}

const char *GetPackageName(JNIEnv *env, jobject context) {
    jclass contextClass = env->FindClass("android/content/Context");
    jmethodID getPackageNameId = env->GetMethodID(contextClass, "getPackageName","()Ljava/lang/String;");

    auto str = (jstring) env->CallObjectMethod(context, getPackageNameId);
    return env->GetStringUTFChars(str, 0);
}

const char *GetDeviceUniqueIdentifier(JNIEnv *env, const char *uuid) {
    jclass uuidClass = env->FindClass("java/util/UUID");

    auto len = strlen(uuid);

    jbyteArray myJByteArray = env->NewByteArray(len);
    env->SetByteArrayRegion(myJByteArray, 0, len, (jbyte *) uuid);

    jmethodID nameUUIDFromBytesMethod = env->GetStaticMethodID(uuidClass,"nameUUIDFromBytes","([B)Ljava/util/UUID;");
    jmethodID toStringMethod = env->GetMethodID(uuidClass, "toString","()Ljava/lang/String;");

    auto obj = env->CallStaticObjectMethod(uuidClass, nameUUIDFromBytesMethod, myJByteArray);
    auto str = (jstring) env->CallObjectMethod(obj, toStringMethod);
    return env->GetStringUTFChars(str, 0);
}

struct MemoryStruct {
    char *memory;
    size_t size;
};

static size_t WriteMemoryCallback(void *contents, size_t size, size_t nmemb, void *userp) {
    size_t realsize = size * nmemb;
    struct MemoryStruct *mem = (struct MemoryStruct *) userp;

    char *ptr = (char *) realloc(mem->memory, mem->size + realsize + 1);
    if (ptr == NULL) {
        return 0;
    }
    mem->memory = ptr;
    memcpy(&(mem->memory[mem->size]), contents, realsize);
    mem->size += realsize;
    mem->memory[mem->size] = 0;

    return realsize;
}

std::string CalcMD5(std::string s) {
    std::string result;
    unsigned char hash[MD5_DIGEST_LENGTH];
    char tmp[4];

    MD5_CTX md5;
    MD5_Init(&md5);
    MD5_Update(&md5, s.c_str(), s.length());
    MD5_Final(hash, &md5);
    for (unsigned char i : hash) {
        sprintf(tmp, "%02x", i);
        result += tmp;
    }
    return result;
}

std::string CalcSHA256(std::string s) {
    std::string result;
    unsigned char hash[SHA256_DIGEST_LENGTH];
    char tmp[4];

    SHA256_CTX sha256;
    SHA256_Init(&sha256);
    SHA256_Update(&sha256, s.c_str(), s.length());
    SHA256_Final(hash, &sha256);
    for (unsigned char i : hash) {
        sprintf(tmp, "%02x", i);
        result += tmp;
    }
    return result;
}


extern "C"
JNIEXPORT jstring JNICALL
Java_com_CrimsonVeilDev_LogAct_Check(JNIEnv *env, jclass clazz, jobject mContext, jstring mUserKey) {
    auto user_key = env->GetStringUTFChars(mUserKey, 0);
    std::string hwid = user_key;
    hwid += GetAndroidID(env, mContext);
    hwid += GetDeviceModel(env);
    hwid += GetDeviceBrand(env);
    std::string UUID = GetDeviceUniqueIdentifier(env, hwid.c_str());
    std::string errMsg;
    struct MemoryStruct chunk{};
    chunk.memory = (char *) malloc(1);
    chunk.size = 0;

    xConnected = false;
    xServerConnection = false;
    memek = false;
    g_Auth.clear();
    g_Token.clear();

    CURL *curl;
    CURLcode res;
    curl = curl_easy_init();
    if (curl) {
        char lol[1000];
        sprintf(lol, oxorany("http://192.168.31.109:5000/apiconnect"));
        curl_easy_setopt(curl, CURLOPT_CUSTOMREQUEST, oxorany("POST"));
        curl_easy_setopt(curl, CURLOPT_URL, lol);
        curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1L);
        
        struct curl_slist *headers = NULL;
        headers = curl_slist_append(headers, oxorany("Accept: application/json"));
        headers = curl_slist_append(headers, oxorany("Content-Type: application/x-www-form-urlencoded"));
        headers = curl_slist_append(headers, oxorany("Charset: UTF-8"));
        curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
        
        // FIXED: Sending keys your Laravel ApiController's validateLicense function expects natively
        char data[4096];
        sprintf(data, oxorany("license_key=%s&package=com.example.app&signature=%s"), user_key, UUID.c_str());
        
        curl_easy_setopt(curl, CURLOPT_POST, 1L);
        curl_easy_setopt(curl, CURLOPT_POSTFIELDS, data);
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, WriteMemoryCallback);
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, (void *) &chunk);
        curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, 0L);
        curl_easy_setopt(curl, CURLOPT_SSL_VERIFYHOST, 0L);
        curl_easy_setopt(curl, CURLOPT_TIMEOUT, 30L);
        
        res = curl_easy_perform(curl);
        if (res == CURLE_OK && chunk.memory) {
            try {
                json result = json::parse(chunk.memory);
                
                // FIXED: Processes the Laravel layout strings instead of boolean evaluations
                if (result.contains(oxorany("status")) && result[oxorany("status")] == oxorany("ok")) {
                    
                    exdate = result[oxorany("expiry")].get<std::string>();
                    int days_left = result[oxorany("days_left")].get<int>();
                    
                    if (days_left >= 0) {
                        // Reproduce validation strings matching local application logic
                        std::string auth = oxorany("PUBG");
                        auth += "-";
                        auth += user_key;
                        auth += "-";
                        auth += UUID;
                        auth += "-";
                        std::string license = oxorany("Vm8Lk7Uj2JmsjCPVPVjrLa7zgfx3uz9E");
                        auth += license.c_str();
                        
                        std::string outputAuth = CalcMD5(auth);
                        g_Token = outputAuth; 
                        g_Auth = outputAuth;
                        
                        xConnected = true;
                        xServerConnection = true;
                        memek = true;
                    } else {
                        errMsg = oxorany("License expired");
                    }
                } else {
                    errMsg = result.contains(oxorany("message")) ? result[oxorany("message")].get<std::string>() : oxorany("Unknown error");
                }
            } catch (json::exception &e) {
                errMsg = e.what();
            }
        } else {
            errMsg = curl_easy_strerror(res);
        }
        curl_slist_free_all(headers);
        curl_easy_cleanup(curl);
    }
    
    if (chunk.memory) {
        free(chunk.memory);
    }
    env->ReleaseStringUTFChars(mUserKey, user_key);
    
    // Returns decrypted "OK" string replacement style if connected successfully
    return xConnected ? env->NewStringUTF(StrEnc("8q", "\x77\x3A", 2).c_str()) : env->NewStringUTF(errMsg.c_str());
}
