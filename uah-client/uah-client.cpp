/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

#define LOG_TAG "uah-client"

#include <aidl/android/hardware/power/IPower.h>
#include <android/binder_manager.h>
#include <log/log.h>
#include <unistd.h>

#include <chrono>
#include <mutex>
#include <optional>
#include <string>
#include <thread>

using aidl::android::hardware::power::Boost;
using aidl::android::hardware::power::IPower;
using aidl::android::hardware::power::Mode;

namespace {

constexpr uid_t kCameraProviderUid = 1047;  // AID_CAMERASERVER

std::mutex gLock;
std::shared_ptr<IPower> gPower;
std::optional<Mode> gMode;
int gHandle = 0;      // last handle given out
int gModeHandle = 0;  // handle that owns gMode

// Only the camera provider gets to talk to the power HAL; every other
// process that loads this library (the camera app does) gets handles
// that do nothing.
std::shared_ptr<IPower> getPower() {
    if (getuid() != kCameraProviderUid) return nullptr;
    if (gPower == nullptr) {
        const std::string name = std::string(IPower::descriptor) + "/default";
        gPower = IPower::fromBinder(ndk::SpAIBinder(AServiceManager_checkService(name.c_str())));
    }
    return gPower;
}

void setMode(std::optional<Mode> mode) {
    if (mode == gMode) return;
    auto power = getPower();
    if (power == nullptr) return;
    if (gMode && !power->setMode(*gMode, false).isOk()) gPower = nullptr;
    if (mode && !power->setMode(*mode, true).isOk()) gPower = nullptr;
    gMode = mode;
}

bool has(const std::string& s, const char* token) {
    return s.find(token) != std::string::npos;
}

// Scene names are the camera HAL's OSENSE_ACTION_CAMERA_* strings.
std::optional<Mode> modeFor(const std::string& s) {
    for (const char* t : {"UHD120", "8K", "HFR", "SLOW", "4K", "60FPS", "SUPER_STEADY"}) {
        if (has(s, t)) return Mode::CAMERA_STREAMING_HIGH;
    }
    for (const char* t : {"VIDEO", "MOVIE", "48M", "MULTI_SCENE", "STIKER_RECORD"}) {
        if (has(s, t)) return Mode::CAMERA_STREAMING_MID;
    }
    return Mode::CAMERA_STREAMING_LOW;
}

void release(int handle) {
    std::lock_guard<std::mutex> lock(gLock);
    if (handle == gModeHandle) {
        setMode(std::nullopt);
        gModeHandle = 0;
    }
}

}  // namespace

extern "C" {

int UahEventAcquireWrapper(std::string scene, std::string& action, std::string& identity,
                           int timeoutMs) {
    const std::string name = scene + " " + action;
    std::lock_guard<std::mutex> lock(gLock);
    const int handle = ++gHandle;
    ALOGV("acquire %d: '%s' from '%s' timeout %d", handle, name.c_str(), identity.c_str(),
          timeoutMs);

    if (has(name, "CAMERA_CLOSE")) {
        setMode(std::nullopt);
        gModeHandle = 0;
    } else if (has(name, "CAMERA_OPEN") || has(name, "CAMERA_CAPTURE")) {
        auto power = getPower();
        const Boost boost = has(name, "CAMERA_OPEN") ? Boost::CAMERA_LAUNCH : Boost::CAMERA_SHOT;
        // A negative duration would end the hint; 0 uses its configured length.
        const int durationMs = timeoutMs > 0 ? timeoutMs : 0;
        if (power != nullptr && !power->setBoost(boost, durationMs).isOk()) gPower = nullptr;
    } else {
        setMode(modeFor(name));
        gModeHandle = handle;
        if (timeoutMs > 0) {
            std::thread([handle, timeoutMs] {
                std::this_thread::sleep_for(std::chrono::milliseconds(timeoutMs));
                release(handle);
            }).detach();
        }
    }
    return handle;
}

int UahRelease(int handle) {
    release(handle);
    return 0;
}

int UahReleaseWapper(int handle) {
    release(handle);
    return 0;
}

// The rest of the stock library's interface. The osense client resolves
// every one of these when it loads the library; none has a power HAL
// equivalent.
int setRelatedSysInfo() { return 0; }
int uahInit() { return 0; }
int UahNotifyWapper() { return 0; }
int UahNotify() { return 0; }
int UahEventAcquire() { return 0; }
int UahEventAcquireOneWay() { return 0; }
int UahResAcquire() { return 0; }
int UahPlatformResAcquire() { return 0; }
int UahResStateRequest() { return 0; }
int UahMutiResStateRequest() { return 0; }
int UahGetHistory() { return 0; }
int UahGetPowerConsis() { return 0; }
int UahGetPMStatus() { return 0; }
int Uah_fetch_dataset() { return 0; }
int uahRuleCtl() { return 0; }
int UahResourceInfo() { return 0; }

}  // extern "C"
